package com.eventcalend.api.domain.usuario;

import com.eventcalend.api.domain.endereco.Endereco;

public record DadosListagemUsuarioDetalhado(Long id, String nome, String email, Endereco endereco, String telefone, TipoDeUsuario tipodeusuario    ) {

    public DadosListagemUsuarioDetalhado(Usuario usuario) {
        this(usuario.getId(), usuario.getNome(), usuario.getEmail(), usuario.getEndereco(),
                usuario.getTelefone(), usuario.getTipodeusuario()
        );
    }
}
