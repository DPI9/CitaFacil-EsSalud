package pe.utp.citafacil.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AdminReservaRequest(
        @NotBlank String dni,
        @NotNull Long idHorario
) {}
