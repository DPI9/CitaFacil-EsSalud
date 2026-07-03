package pe.utp.citafacil.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.utp.citafacil.model.Notificacion;
import java.util.List;

public interface NotificacionRepository extends JpaRepository<Notificacion, Long> {
    List<Notificacion> findByCita_IdCita(Long idCita);
    List<Notificacion> findByCita_Asegurado_IdAseguradoOrderByFechaEnvioDesc(Long idAsegurado);
    boolean existsByCita_IdCitaAndTipo(Long idCita, String tipo);
}
