package com.eventcalend.api.usuario;

public record DadosListagemUsuario(Long id, String nome, String email, String telefone,TipoDeUsuario tipodeusuario) {
    public DadosListagemUsuario(Usuario usuario) {
        this( usuario.getId(), usuario.getNome(), usuario.getEmail(), usuario.getTelefone(), usuario.getTipodeusuario());
    }
}
