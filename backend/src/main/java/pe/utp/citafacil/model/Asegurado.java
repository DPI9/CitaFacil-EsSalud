package pe.utp.citafacil.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "asegurado", uniqueConstraints = @UniqueConstraint(columnNames = "dni"))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Asegurado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idAsegurado;

    @Column(nullable = false, length = 8)
    private String dni;

    @Column(nullable = false)
    private String nombres;

    @Column(nullable = false)
    private String apellidos;

    private LocalDate fechaNacimiento;
    private String telefono;
    private String correo;

    @Column(name = "contrasena_hash", nullable = false)
    private String contrasenaHash;

    @Column(nullable = false)
    private LocalDateTime fechaRegistro;

    @PrePersist
    void onCreate() {
        if (fechaRegistro == null) fechaRegistro = LocalDateTime.now();
    }
}
