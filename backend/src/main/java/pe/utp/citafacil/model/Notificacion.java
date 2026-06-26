package pe.utp.citafacil.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notificacion")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Notificacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idNotificacion;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_cita")
    private Cita cita;

    private String tipo;  // RECORDATORIO, CONFIRMACION, CANCELACION, CUPO_DISPONIBLE
    private String canal; // SMS, WHATSAPP

    private LocalDateTime fechaEnvio;
    private String estadoEnvio; // PENDIENTE, ENVIADO, FALLIDO
}
