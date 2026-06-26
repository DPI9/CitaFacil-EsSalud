package pe.utp.citafacil.dto;

import pe.utp.citafacil.model.Cita;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public record CitaResponse(
        Long idCita,
        String codigoReserva,
        String estado,
        LocalDateTime fechaReserva,
        String canalReserva,
        String especialidad,
        String medico,
        String establecimiento,
        LocalDate fecha,
        LocalTime horaInicio,
        LocalTime horaFin
) {
    public static CitaResponse de(Cita c) {
        var h = c.getHorario();
        var m = h != null ? h.getMedico() : null;
        var est = m != null ? m.getEstablecimiento() : null;
        return new CitaResponse(
                c.getIdCita(),
                c.getCodigoReserva(),
                c.getEstado() != null ? c.getEstado().name() : null,
                c.getFechaReserva(),
                c.getCanalReserva(),
                m != null ? m.getEspecialidad() : null,
                m != null ? ("Dr(a). " + m.getNombres() + " " + m.getApellidos()) : null,
                est != null ? est.getNombre() : null,
                h != null ? h.getFecha() : null,
                h != null ? h.getHoraInicio() : null,
                h != null ? h.getHoraFin() : null
        );
    }
}
