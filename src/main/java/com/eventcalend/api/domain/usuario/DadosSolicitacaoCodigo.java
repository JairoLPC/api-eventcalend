package com.eventcalend.api.domain.usuario;

import jakarta.validation.constraints.NotBlank;

public record DadosSolicitacaoCodigo(
        @NotBlank
        String loguin
) {}
