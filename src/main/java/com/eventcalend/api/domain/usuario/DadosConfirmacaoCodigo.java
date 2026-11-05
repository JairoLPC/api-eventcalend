package com.eventcalend.api.domain.usuario;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DadosConfirmacaoCodigo(
        @NotBlank
        String loguin,

        @NotBlank
        String codigo,

        @NotBlank
        @Size(min = 6)
        String novaSenha
) {}
