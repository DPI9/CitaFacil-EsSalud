package pe.utp.citafacil.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.utp.citafacil.model.ListaEspera;
import java.util.List;

public interface ListaEsperaRepository extends JpaRepository<ListaEspera, Long> {
    List<ListaEspera> findByHorario_IdHorarioOrderByPosicionAsc(Long idHorario);
}
