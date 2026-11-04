package com.eventcalend.api.domain.usuario;

import jakarta.validation.constraints.NotBlank;

public record DadosAutenticacao(
        @NotBlank
        String loguin,
        @NotBlank
        String senha
) {
}
