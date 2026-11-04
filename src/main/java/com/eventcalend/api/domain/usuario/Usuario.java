package com.eventcalend.api.domain.usuario;

import com.eventcalend.api.domain.endereco.Endereco;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Table(name = "usuarios")
@Entity(name = "usuarios")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nome;
    private String email;
    private String telefone;

    @Enumerated(EnumType.STRING)
    private TipoDeUsuario tipodeusuario;

    @Embedded
    private Endereco endereco;
    private boolean ativo;

    public Usuario(DadosCadastroUsuario dados) {
        this.ativo = true;
        this.nome = dados.nome();
        this.email = dados.email();
        this.telefone = dados.telefone();
        this.tipodeusuario = dados.tipodeusuario();
        this.endereco = new Endereco(dados.endereco());
    }

    public void atualizarInformacoes(DadosCadastroUsuarioAtualizado dados) {
        if (dados.nome() != null && !dados.nome().isBlank()) {
            this.nome = dados.nome();
        }
        if (dados.email() != null && !dados.email().isBlank()) {
            this.email = dados.email();
        }
        if (dados.telefone() != null && !dados.telefone().isBlank()) {
            this.telefone = dados.telefone();
        }
        if (dados.tipodeusuario() != null) {
            this.tipodeusuario = dados.tipodeusuario();
        }
        if (dados.endereco() != null) {
            if (this.endereco == null) {
                this.endereco = new Endereco(dados.endereco());
            } else {
                this.endereco.atualizarEndereco(dados.endereco());
            }
        }
    }

    public void reativarEAtualizar(DadosCadastroUsuario dados) {
        this.ativo = true;
        this.nome = dados.nome();
        this.email = dados.email();
        this.telefone = dados.telefone();
        this.tipodeusuario = dados.tipodeusuario();
        this.endereco = new Endereco(dados.endereco());
    }

    public void excluir() {
        this.ativo = false;
    }
}
