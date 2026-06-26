package pe.utp.citafacil.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.utp.citafacil.model.Cita;
import java.util.List;

public interface CitaRepository extends JpaRepository<Cita, Long> {
    List<Cita> findByAsegurado_IdAsegurado(Long idAsegurado);
}
