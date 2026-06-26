package pe.utp.citafacil.dto;

import jakarta.validation.constraints.NotNull;

public record ReservaRequest(
        @NotNull Long idHorario,
        String canal // WEB / MOVIL
) {}
