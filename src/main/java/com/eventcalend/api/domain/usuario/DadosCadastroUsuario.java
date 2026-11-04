package com.eventcalend.api.domain.usuario;

import com.eventcalend.api.domain.endereco.DadosEndereco;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record DadosCadastroUsuario(
        @NotBlank
        String nome,
        @NotBlank
        @Email
        String email,
        @NotBlank
        @Pattern(regexp = "\\d{10,11}", message = "O telefone deve conter apenas números e ter entre 10 e 11 dígitos")
        String telefone,
        @NotNull
        TipoDeUsuario tipodeusuario,
        @NotNull
        @Valid
        DadosEndereco endereco
) {
}
