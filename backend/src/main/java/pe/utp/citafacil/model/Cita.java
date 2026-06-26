package pe.utp.citafacil.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "cita")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Cita {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idCita;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_asegurado")
    private Asegurado asegurado;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_horario")
    private HorarioDisponible horario;

    @Column(nullable = false, unique = true)
    private String codigoReserva;

    @Column(nullable = false)
    private LocalDateTime fechaReserva;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoCita estado;

    private String canalReserva; // WEB, MOVIL
    private LocalDateTime fechaCancelacion;

    @PrePersist
    void onCreate() {
        if (fechaReserva == null) fechaReserva = LocalDateTime.now();
        if (estado == null) estado = EstadoCita.CONFIRMADA;
    }
}
