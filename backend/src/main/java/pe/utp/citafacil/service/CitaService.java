package pe.utp.citafacil.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.utp.citafacil.dto.CitaResponse;
import pe.utp.citafacil.model.*;
import pe.utp.citafacil.repository.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CitaService {

    private final CitaRepository citaRepository;
    private final HorarioDisponibleRepository horarioRepository;
    private final AseguradoRepository aseguradoRepository;
    private final ListaEsperaRepository listaEsperaRepository;
    private final NotificacionService notificacionService;

    /** Reserva una cita descontando el cupo de forma atomica (RF-06). */
    @Transactional
    public CitaResponse reservar(String dni, Long idHorario, String canal) {
        Asegurado asegurado = aseguradoRepository.findByDni(dni).orElseThrow();
        HorarioDisponible horario = horarioRepository.findById(idHorario)
                .orElseThrow(() -> new IllegalArgumentException("Horario no encontrado"));

        int afectadas = horarioRepository.descontarCupo(idHorario);
        if (afectadas == 0) {
            throw new SinCuposException("No hay cupos disponibles para este horario. Puede unirse a la lista de espera.");
        }

        Cita cita = Cita.builder()
                .asegurado(asegurado)
                .horario(horario)
                .codigoReserva("CF-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .estado(EstadoCita.CONFIRMADA)
                .canalReserva(canal != null ? canal : "WEB")
                .build();
        cita = citaRepository.save(cita);

        notificacionService.enviar(cita, "CONFIRMACION", "WHATSAPP");
        return CitaResponse.de(cita);
    }

    /** Cancela una cita, libera el cupo y reasigna al siguiente en lista de espera (RF-07/RF-10). */
    @Transactional
    public CitaResponse cancelar(String dni, Long idCita) {
        Cita cita = citaRepository.findById(idCita)
                .orElseThrow(() -> new IllegalArgumentException("Cita no encontrada"));
        if (!cita.getAsegurado().getDni().equals(dni)) {
            throw new IllegalArgumentException("La cita no pertenece a este asegurado");
        }
        if (cita.getEstado() == EstadoCita.CANCELADA) {
            throw new IllegalArgumentException("La cita ya estaba cancelada");
        }

        cita.setEstado(EstadoCita.CANCELADA);
        cita.setFechaCancelacion(LocalDateTime.now());
        citaRepository.save(cita);

        Long idHorario = cita.getHorario().getIdHorario();
        horarioRepository.liberarCupo(idHorario);
        notificacionService.enviar(cita, "CANCELACION", "WHATSAPP");

        reasignarDesdeListaEspera(idHorario);
        return CitaResponse.de(cita);
    }

    /** Notifica al siguiente asegurado en cola que se libero un cupo. */
    private void reasignarDesdeListaEspera(Long idHorario) {
        List<ListaEspera> cola = listaEsperaRepository
                .findByHorario_IdHorarioOrderByPosicionAsc(idHorario);
        cola.stream()
                .filter(e -> "EN_ESPERA".equals(e.getEstadoEspera()))
                .findFirst()
                .ifPresent(siguiente -> {
                    siguiente.setEstadoEspera("NOTIFICADO");
                    listaEsperaRepository.save(siguiente);
                    // Notificacion de cupo disponible (se asocia mediante log; no hay cita aun)
                    org.slf4j.LoggerFactory.getLogger(CitaService.class).info(
                            ">> [LISTA ESPERA] Cupo liberado en horario {}. Notificado asegurado {} (30 min para confirmar).",
                            idHorario, siguiente.getAsegurado().getDni());
                });
    }

    /** Inscribe al asegurado en la lista de espera de un horario saturado (RF-10). */
    @Transactional
    public ListaEspera unirseListaEspera(String dni, Long idHorario) {
        Asegurado asegurado = aseguradoRepository.findByDni(dni).orElseThrow();
        HorarioDisponible horario = horarioRepository.findById(idHorario)
                .orElseThrow(() -> new IllegalArgumentException("Horario no encontrado"));
        int posicion = listaEsperaRepository.findByHorario_IdHorarioOrderByPosicionAsc(idHorario).size() + 1;
        ListaEspera e = ListaEspera.builder()
                .asegurado(asegurado)
                .horario(horario)
                .posicion(posicion)
                .estadoEspera("EN_ESPERA")
                .build();
        return listaEsperaRepository.save(e);
    }

    @Transactional(readOnly = true)
    public List<CitaResponse> misCitas(String dni) {
        Asegurado asegurado = aseguradoRepository.findByDni(dni).orElseThrow();
        return citaRepository.findByAsegurado_IdAsegurado(asegurado.getIdAsegurado())
                .stream().map(CitaResponse::de).toList();
    }

    public static class SinCuposException extends RuntimeException {
        public SinCuposException(String msg) { super(msg); }
    }
}
