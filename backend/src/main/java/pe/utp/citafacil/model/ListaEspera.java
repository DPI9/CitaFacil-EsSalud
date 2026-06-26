package pe.utp.citafacil.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "lista_espera")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ListaEspera {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idEspera;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_asegurado")
    private Asegurado asegurado;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_horario")
    private HorarioDisponible horario;

    private Integer posicion;

    @Column(nullable = false)
    private LocalDateTime fechaRegistroEspera;

    private String estadoEspera; // EN_ESPERA, NOTIFICADO, CONFIRMADO, CANCELADO

    @PrePersist
    void onCreate() {
        if (fechaRegistroEspera == null) fechaRegistroEspera = LocalDateTime.now();
        if (estadoEspera == null) estadoEspera = "EN_ESPERA";
    }
}
