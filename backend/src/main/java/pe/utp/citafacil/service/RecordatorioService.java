package pe.utp.citafacil.service;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.utp.citafacil.model.Cita;
import pe.utp.citafacil.model.EstadoCita;
import pe.utp.citafacil.repository.CitaRepository;
import pe.utp.citafacil.repository.NotificacionRepository;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * Recordatorios automaticos de citas (RF-09).
 * Envia recordatorio 24 horas y 2 horas antes de cada cita confirmada.
 * El envio real por WhatsApp/SMS esta simulado (NotificacionService).
 */
@Service
@RequiredArgsConstructor
public class RecordatorioService {

    private static final Logger log = LoggerFactory.getLogger(RecordatorioService.class);

    private final CitaRepository citaRepository;
    private final NotificacionRepository notificacionRepository;
    private final NotificacionService notificacionService;

    /** Se ejecuta cada 5 minutos y evalua las ventanas de 24h y 2h. */
    @Scheduled(fixedRate = 300_000)
    @Transactional
    public void enviarRecordatoriosProgramados() {
        LocalDateTime ahora = LocalDateTime.now();
        int enviados = 0;
        for (Cita c : citaRepository.findAll()) {
            if (c.getEstado() != EstadoCita.CONFIRMADA || c.getHorario() == null
                    || c.getHorario().getFecha() == null || c.getHorario().getHoraInicio() == null) continue;

            LocalDateTime inicio = LocalDateTime.of(c.getHorario().getFecha(), c.getHorario().getHoraInicio());
            long horas = Duration.between(ahora, inicio).toHours();
            if (horas < 0) continue;

            if (horas <= 24 && horas >= 23 && marcarSiNuevo(c, "RECORDATORIO_24H")) enviados++;
            if (horas <= 2 && horas >= 1 && marcarSiNuevo(c, "RECORDATORIO_2H")) enviados++;
        }
        if (enviados > 0) log.info(">> [RECORDATORIOS] {} recordatorios automaticos enviados.", enviados);
    }

    /** Disparo manual desde el panel: envia recordatorio a toda cita futura confirmada sin recordatorio. */
    @Transactional
    public int ejecutarManual() {
        LocalDateTime ahora = LocalDateTime.now();
        int enviados = 0;
        for (Cita c : citaRepository.findAll()) {
            if (c.getEstado() != EstadoCita.CONFIRMADA || c.getHorario() == null
                    || c.getHorario().getFecha() == null || c.getHorario().getHoraInicio() == null) continue;
            LocalDateTime inicio = LocalDateTime.of(c.getHorario().getFecha(), c.getHorario().getHoraInicio());
            if (inicio.isAfter(ahora) && marcarSiNuevo(c, "RECORDATORIO")) enviados++;
        }
        log.info(">> [RECORDATORIOS] Disparo manual: {} recordatorios enviados.", enviados);
        return enviados;
    }

    private boolean marcarSiNuevo(Cita c, String tipo) {
        if (notificacionRepository.existsByCita_IdCitaAndTipo(c.getIdCita(), tipo)) return false;
        notificacionService.enviar(c, tipo, "WHATSAPP");
        return true;
    }
}
