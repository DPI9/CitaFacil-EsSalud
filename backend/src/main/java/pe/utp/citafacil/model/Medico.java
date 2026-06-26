package pe.utp.citafacil.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "medico")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Medico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idMedico;

    @Column(nullable = false)
    private String nombres;

    @Column(nullable = false)
    private String apellidos;

    @Column(nullable = false)
    private String especialidad;

    private String cmp; // Colegio Medico del Peru

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_establecimiento")
    private Establecimiento establecimiento;
}
