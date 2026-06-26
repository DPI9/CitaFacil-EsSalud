package pe.utp.citafacil.dto;

public record AuthResponse(
        String token,
        Long idAsegurado,
        String dni,
        String nombres,
        String apellidos
) {}
