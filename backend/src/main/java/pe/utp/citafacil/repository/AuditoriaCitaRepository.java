package pe.utp.citafacil.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.utp.citafacil.model.AuditoriaCita;
import java.util.List;

public interface AuditoriaCitaRepository extends JpaRepository<AuditoriaCita, Long> {
    List<AuditoriaCita> findTop100ByOrderByFechaDesc();
    List<AuditoriaCita> findByIdCitaOrderByFechaDesc(Long idCita);
}
