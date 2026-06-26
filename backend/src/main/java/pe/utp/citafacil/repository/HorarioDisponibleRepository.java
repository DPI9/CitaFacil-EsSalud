package pe.utp.citafacil.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pe.utp.citafacil.model.HorarioDisponible;

import java.time.LocalDate;
import java.util.List;

public interface HorarioDisponibleRepository extends JpaRepository<HorarioDisponible, Long> {

    List<HorarioDisponible> findByMedico_IdMedicoAndFecha(Long idMedico, LocalDate fecha);

    List<HorarioDisponible> findByMedico_IdMedico(Long idMedico);

    List<HorarioDisponible> findByMedico_EspecialidadIgnoreCaseAndCuposDisponiblesGreaterThan(
            String especialidad, Integer cupos);

    List<HorarioDisponible> findByCuposDisponiblesGreaterThan(Integer cupos);

    // Descuento ATOMICO de cupo (RF-06): solo descuenta si hay cupos disponibles.
    // Devuelve filas afectadas (1 = exito, 0 = sin cupos / saturado).
    @Modifying
    @Query("UPDATE HorarioDisponible h SET h.cuposDisponibles = h.cuposDisponibles - 1 " +
           "WHERE h.idHorario = :id AND h.cuposDisponibles > 0")
    int descontarCupo(@Param("id") Long id);

    // Libera un cupo (al cancelar una cita).
    @Modifying
    @Query("UPDATE HorarioDisponible h SET h.cuposDisponibles = h.cuposDisponibles + 1 " +
           "WHERE h.idHorario = :id AND h.cuposDisponibles < h.cuposTotales")
    int liberarCupo(@Param("id") Long id);
}
