package com.eventcalend.api.usuario;

import com.eventcalend.api.endereco.DadosEndereco;
import jakarta.validation.constraints.NotNull;

public record DadosCadastroUsuarioAtualizado(
        @NotNull
        long id,
        String nome,
        String email,
        String telefone,
        DadosEndereco endereco) {
}
