package pe.utp.citafacil.service;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import pe.utp.citafacil.model.Cita;
import pe.utp.citafacil.model.Notificacion;
import pe.utp.citafacil.repository.NotificacionRepository;

import java.time.LocalDateTime;

/**
 * Servicio de notificaciones (RF-08).
 * En esta version el envio por WhatsApp/SMS esta SIMULADO (se registra en BD y log).
 * Para produccion se integraria con Twilio o Meta Business API.
 */
@Service
@RequiredArgsConstructor
public class NotificacionService {

    private static final Logger log = LoggerFactory.getLogger(NotificacionService.class);
    private final NotificacionRepository notificacionRepository;

    public Notificacion enviar(Cita cita, String tipo, String canal) {
        Notificacion n = Notificacion.builder()
                .cita(cita)
                .tipo(tipo)
                .canal(canal)
                .fechaEnvio(LocalDateTime.now())
                .estadoEnvio("ENVIADO") // simulado
                .build();
        n = notificacionRepository.save(n);
        log.info(">> [NOTIFICACION {}] {} via {} -> cita {} (asegurado {})",
                tipo, canal, canal, cita.getCodigoReserva(),
                cita.getAsegurado() != null ? cita.getAsegurado().getDni() : "?");
        return n;
    }
}
