package pe.utp.citafacil.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "establecimiento")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Establecimiento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idEstablecimiento;

    @Column(nullable = false)
    private String nombre;

    private String direccion;
    private String distrito;
    private String tipo; // HOSPITAL, POLICLINICO, CAP, etc.
}
