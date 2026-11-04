package com.eventcalend.api.domain.usuario;

import com.eventcalend.api.domain.endereco.DadosEndereco;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record DadosCadastroUsuarioAtualizado(
        @NotNull
        Long id,
        String nome,
        @Email(message = "Formato de e-mail inválido")
        String email,
        @Pattern(regexp = "\\d{10,11}", message = "O telefone deve conter apenas números e ter entre 10 e 11 dígitos")
        String telefone,
        TipoDeUsuario tipodeusuario,
        @Valid
        DadosEndereco endereco
) {
}
