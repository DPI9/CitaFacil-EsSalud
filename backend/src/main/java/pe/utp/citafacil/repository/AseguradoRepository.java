package pe.utp.citafacil.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.utp.citafacil.model.Asegurado;
import java.util.Optional;

public interface AseguradoRepository extends JpaRepository<Asegurado, Long> {
    Optional<Asegurado> findByDni(String dni);
    boolean existsByDni(String dni);
}
