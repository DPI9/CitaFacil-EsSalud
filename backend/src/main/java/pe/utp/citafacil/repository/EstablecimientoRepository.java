package pe.utp.citafacil.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.utp.citafacil.model.Establecimiento;

public interface EstablecimientoRepository extends JpaRepository<Establecimiento, Long> {
}
