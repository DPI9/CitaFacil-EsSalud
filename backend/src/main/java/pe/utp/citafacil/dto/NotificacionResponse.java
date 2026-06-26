package pe.utp.citafacil.dto;

import pe.utp.citafacil.model.Notificacion;

import java.time.LocalDateTime;

public record NotificacionResponse(
        Long idNotificacion,
        String tipo,
        String canal,
        String especialidad,
        String codigoReserva,
        LocalDateTime fechaEnvio,
        String estadoEnvio
) {
    public static NotificacionResponse de(Notificacion n) {
        var cita = n.getCita();
        var h = cita != null ? cita.getHorario() : null;
        var m = h != null ? h.getMedico() : null;
        return new NotificacionResponse(
                n.getIdNotificacion(),
                n.getTipo(),
                n.getCanal(),
                m != null ? m.getEspecialidad() : null,
                cita != null ? cita.getCodigoReserva() : null,
                n.getFechaEnvio(),
                n.getEstadoEnvio()
        );
    }
}
