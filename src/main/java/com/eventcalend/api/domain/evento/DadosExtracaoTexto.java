package com.eventcalend.api.domain.evento;

import jakarta.validation.constraints.NotBlank;

public record DadosExtracaoTexto(
    @NotBlank(message = "O texto para extração é obrigatório")
    String texto
) {}
