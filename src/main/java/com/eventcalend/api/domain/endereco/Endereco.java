package com.eventcalend.api.domain.endereco;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

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
        if (endereco.logradouro() != null && !endereco.logradouro().isBlank()) {
            this.logradouro = endereco.logradouro();
        }
        if (endereco.numero() != null) {
            this.numero = endereco.numero();
        }
        if (endereco.complemento() != null) {
            this.complemento = endereco.complemento();
        }
        if (endereco.bairro() != null && !endereco.bairro().isBlank()) {
            this.bairro = endereco.bairro();
        }
        if (endereco.cidade() != null && !endereco.cidade().isBlank()) {
            this.cidade = endereco.cidade();
        }
        if (endereco.estado() != null && !endereco.estado().isBlank()) {
            this.estado = endereco.estado();
        }
        if (endereco.cep() != null && !endereco.cep().isBlank()) {
            this.cep = endereco.cep();
        }
    }
}
