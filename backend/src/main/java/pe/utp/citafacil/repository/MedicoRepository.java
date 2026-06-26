package pe.utp.citafacil.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import pe.utp.citafacil.model.Medico;
import java.util.List;

public interface MedicoRepository extends JpaRepository<Medico, Long> {
    List<Medico> findByEspecialidadIgnoreCase(String especialidad);

    @Query("SELECT DISTINCT m.especialidad FROM Medico m ORDER BY m.especialidad")
    List<String> findEspecialidades();
}
