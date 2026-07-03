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
    private final AuditoriaCitaRepository auditoriaRepository;

    private static final long HORAS_MINIMAS = 2; // RF-07: cancelar/reprogramar hasta 2h antes

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
        auditar(cita, "RESERVA", null, "CONFIRMADA", canal != null && canal.equals("ADMIN") ? "ADMIN" : dni);
        return CitaResponse.de(cita);
    }

    /** Cancela una cita, libera el cupo y reasigna al siguiente en lista de espera (RF-07/RF-10). */
    @Transactional
    public CitaResponse cancelar(String dni, Long idCita) {
        Cita cita = obtenerCitaDelAsegurado(dni, idCita);
        if (cita.getEstado() == EstadoCita.CANCELADA) {
            throw new IllegalArgumentException("La cita ya estaba cancelada");
        }
        validarAnticipacion(cita);

        cita.setEstado(EstadoCita.CANCELADA);
        cita.setFechaCancelacion(LocalDateTime.now());
        citaRepository.save(cita);

        Long idHorario = cita.getHorario().getIdHorario();
        horarioRepository.liberarCupo(idHorario);
        notificacionService.enviar(cita, "CANCELACION", "WHATSAPP");
        auditar(cita, "CANCELACION", "CONFIRMADA", "CANCELADA", dni);

        reasignarDesdeListaEspera(idHorario);
        return CitaResponse.de(cita);
    }

    /** Reprograma una cita a un nuevo horario (RF-07). */
    @Transactional
    public CitaResponse reprogramar(String dni, Long idCita, Long nuevoIdHorario) {
        Cita cita = obtenerCitaDelAsegurado(dni, idCita);
        if (cita.getEstado() != EstadoCita.CONFIRMADA) {
            throw new IllegalArgumentException("Solo se pueden reprogramar citas confirmadas");
        }
        validarAnticipacion(cita);

        HorarioDisponible nuevo = horarioRepository.findById(nuevoIdHorario)
                .orElseThrow(() -> new IllegalArgumentException("Nuevo horario no encontrado"));
        Long anteriorIdHorario = cita.getHorario().getIdHorario();
        if (anteriorIdHorario.equals(nuevoIdHorario)) {
            throw new IllegalArgumentException("El nuevo horario es el mismo que el actual");
        }

        // Toma el cupo del nuevo horario de forma atomica
        int afectadas = horarioRepository.descontarCupo(nuevoIdHorario);
        if (afectadas == 0) {
            throw new SinCuposException("El nuevo horario no tiene cupos disponibles.");
        }
        // Libera el cupo del horario anterior
        horarioRepository.liberarCupo(anteriorIdHorario);

        cita.setHorario(nuevo);
        citaRepository.save(cita);

        notificacionService.enviar(cita, "CONFIRMACION", "WHATSAPP");
        auditar(cita, "REPROGRAMACION", "CONFIRMADA", "CONFIRMADA", dni);
        reasignarDesdeListaEspera(anteriorIdHorario);
        return CitaResponse.de(cita);
    }

    /** Valida que falten al menos 2 horas para la cita (RF-07). */
    private void validarAnticipacion(Cita cita) {
        var h = cita.getHorario();
        if (h != null && h.getFecha() != null && h.getHoraInicio() != null) {
            LocalDateTime inicio = LocalDateTime.of(h.getFecha(), h.getHoraInicio());
            if (LocalDateTime.now().plusHours(HORAS_MINIMAS).isAfter(inicio)) {
                throw new IllegalArgumentException(
                        "Solo se puede cancelar o reprogramar hasta " + HORAS_MINIMAS + " horas antes de la cita.");
            }
        }
    }

    private Cita obtenerCitaDelAsegurado(String dni, Long idCita) {
        Cita cita = citaRepository.findById(idCita)
                .orElseThrow(() -> new IllegalArgumentException("Cita no encontrada"));
        if (!cita.getAsegurado().getDni().equals(dni)) {
            throw new IllegalArgumentException("La cita no pertenece a este asegurado");
        }
        return cita;
    }

    private void auditar(Cita cita, String accion, String estadoAnterior, String estadoNuevo, String actor) {
        auditoriaRepository.save(AuditoriaCita.builder()
                .idCita(cita.getIdCita())
                .codigoReserva(cita.getCodigoReserva())
                .estadoAnterior(estadoAnterior)
                .estadoNuevo(estadoNuevo)
                .accion(accion)
                .actor(actor)
                .build());
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
