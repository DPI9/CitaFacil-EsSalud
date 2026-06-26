package pe.utp.citafacil.dto;

import pe.utp.citafacil.model.HorarioDisponible;

import java.time.LocalDate;
import java.time.LocalTime;

public record HorarioResponse(
        Long idHorario,
        Long idMedico,
        String medico,
        String especialidad,
        String establecimiento,
        LocalDate fecha,
        LocalTime horaInicio,
        LocalTime horaFin,
        Integer cuposTotales,
        Integer cuposDisponibles,
        String estado
) {
    public static HorarioResponse de(HorarioDisponible h) {
        var m = h.getMedico();
        var est = m != null ? m.getEstablecimiento() : null;
        return new HorarioResponse(
                h.getIdHorario(),
                m != null ? m.getIdMedico() : null,
                m != null ? ("Dr(a). " + m.getNombres() + " " + m.getApellidos()) : null,
                m != null ? m.getEspecialidad() : null,
                est != null ? est.getNombre() : null,
                h.getFecha(),
                h.getHoraInicio(),
                h.getHoraFin(),
                h.getCuposTotales(),
                h.getCuposDisponibles(),
                h.getCuposDisponibles() != null && h.getCuposDisponibles() > 0 ? "DISPONIBLE" : "SATURADO"
        );
    }
}
