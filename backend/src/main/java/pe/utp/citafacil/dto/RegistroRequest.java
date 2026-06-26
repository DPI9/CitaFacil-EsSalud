package pe.utp.citafacil.dto;

import jakarta.validation.constraints.*;

public record RegistroRequest(
        @NotBlank @Pattern(regexp = "\\d{8}", message = "El DNI debe tener 8 digitos") String dni,
        @NotBlank String nombres,
        @NotBlank String apellidos,
        String telefono,
        @Email String correo,
        @NotBlank @Size(min = 6, message = "La contrasena debe tener al menos 6 caracteres") String contrasena
) {}
