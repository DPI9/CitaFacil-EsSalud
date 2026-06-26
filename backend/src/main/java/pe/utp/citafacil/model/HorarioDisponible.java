package pe.utp.citafacil.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "horario_disponible")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class HorarioDisponible {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idHorario;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_medico")
    private Medico medico;

    @Column(nullable = false)
    private LocalDate fecha;

    private LocalTime horaInicio;
    private LocalTime horaFin;

    @Column(nullable = false)
    private Integer cuposTotales;

    @Column(nullable = false)
    private Integer cuposDisponibles;

    private String estado; // DISPONIBLE, SATURADO, CERRADO
}
