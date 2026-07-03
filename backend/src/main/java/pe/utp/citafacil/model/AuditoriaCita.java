package pe.utp.citafacil.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * Traza de auditoria de los cambios de estado de una cita (RF-13).
 */
@Entity
@Table(name = "auditoria_cita")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AuditoriaCita {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idAuditoria;

    @Column(nullable = false)
    private Long idCita;

    private String codigoReserva;
    private String estadoAnterior;
    private String estadoNuevo;
    private String accion;   // RESERVA, CANCELACION, REPROGRAMACION
    private String actor;    // dni del asegurado o ADMIN

    @Column(nullable = false)
    private LocalDateTime fecha;

    @PrePersist
    void onCreate() {
        if (fecha == null) fecha = LocalDateTime.now();
    }
}
