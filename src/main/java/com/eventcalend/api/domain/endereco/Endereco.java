package com.eventcalend.api.domain.endereco;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Objects;

@Embeddable
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Endereco {

    private String logradouro;
    private String numero;
    private String complemento;
    private String bairro;
    private String cidade;
    private String estado;
    private String cep;

    public Endereco(DadosEndereco endereco) {
        this.logradouro = endereco.logradouro();
        this.numero = endereco.numero();
        this.complemento = endereco.complemento();
        this.bairro = endereco.bairro();
        this.cidade = endereco.cidade();
        this.estado = endereco.estado();
        this.cep = endereco.cep();
    }

    public void atualizarEndereco(DadosEndereco endereco) {
        this.logradouro = textoOuAtual(endereco.logradouro(), this.logradouro);
        this.numero = Objects.requireNonNullElse(endereco.numero(), this.numero);
        this.complemento = Objects.requireNonNullElse(endereco.complemento(), this.complemento);
        this.bairro = textoOuAtual(endereco.bairro(), this.bairro);
        this.cidade = textoOuAtual(endereco.cidade(), this.cidade);
        this.estado = textoOuAtual(endereco.estado(), this.estado);
        this.cep = textoOuAtual(endereco.cep(), this.cep);
    }

    private static String textoOuAtual(String novoValor, String valorAtual) {
        return (novoValor != null && !novoValor.isBlank()) ? novoValor : valorAtual;
    }
}
