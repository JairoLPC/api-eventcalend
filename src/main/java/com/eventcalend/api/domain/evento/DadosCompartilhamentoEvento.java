package com.eventcalend.api.domain.evento;

import jakarta.validation.constraints.NotNull;

public record DadosCompartilhamentoEvento(
        @NotNull(message = "O ID do contato é obrigatório")
        Long usuarioId,
        String tipoInformacao
) {
}
