package com.eventcalend.api.domain.usuario;

import com.eventcalend.api.domain.endereco.Endereco;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Objects;

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
        reativarEAtualizar(dados);
    }

    public void atualizarInformacoes(DadosCadastroUsuarioAtualizado dados) {
        this.nome = textoOuAtual(dados.nome(), this.nome);
        this.email = textoOuAtual(dados.email(), this.email);
        this.telefone = textoOuAtual(dados.telefone(), this.telefone);
        this.tipodeusuario = Objects.requireNonNullElse(dados.tipodeusuario(), this.tipodeusuario);
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

    private static String textoOuAtual(String novoValor, String valorAtual) {
        return (novoValor != null && !novoValor.isBlank()) ? novoValor : valorAtual;
    }
}
