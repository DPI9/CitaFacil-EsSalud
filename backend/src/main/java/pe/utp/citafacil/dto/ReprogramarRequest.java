package pe.utp.citafacil.dto;

import jakarta.validation.constraints.NotNull;

public record ReprogramarRequest(
        @NotNull Long idHorario
) {}
