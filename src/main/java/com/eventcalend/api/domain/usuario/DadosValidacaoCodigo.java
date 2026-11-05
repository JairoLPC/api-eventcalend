package com.eventcalend.api.domain.usuario;

import jakarta.validation.constraints.NotBlank;

public record DadosValidacaoCodigo(
        @NotBlank
        String loguin,

        @NotBlank
        String codigo
) {}
