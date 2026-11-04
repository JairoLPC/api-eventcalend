package com.eventcalend.api.domain.evento;

import com.eventcalend.api.domain.ValidacaoException;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Table(name = "eventos")
@Entity(name = "Evento")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class Evento {

    private static final String TIPO_PADRAO = "Evento";
    private static final String STATUS_PADRAO = "Confirmado";
    private static final String PAGAMENTO_PADRAO = "PENDENTE";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String titulo;
    private String tipo;

    @Column(name = "data_evento")
    private LocalDate dataEvento;

    @Column(name = "hora_inicio")
    private String horaInicio;

    @Column(name = "hora_fim")
    private String horaFim;

    private String logradouro;
    private String numero;
    private String complemento;
    private String cep;
    private String status;

    @Column(name = "status_pagamento")
    private String statusPagamento;

    private BigDecimal valor;

    @Column(columnDefinition = "TEXT")
    private String observacoes;

    private boolean ativo;

    public Evento(DadosCadastroEvento dados) {
        validarIntervaloHorario(dados.horaInicio(), dados.horaFim());
        this.titulo = dados.titulo().trim();
        this.tipo = textoOuPadrao(dados.tipo(), TIPO_PADRAO);
        this.dataEvento = dados.dataEvento();
        this.horaInicio = dados.horaInicio().trim();
        this.horaFim = dados.horaFim().trim();
        this.logradouro = dados.logradouro().trim();
        this.numero = dados.numero() != null ? dados.numero().trim() : null;
        this.complemento = dados.complemento() != null ? dados.complemento().trim() : null;
        this.cep = normalizarCep(dados.cep());
        this.status = textoOuPadrao(dados.status(), STATUS_PADRAO);
        this.statusPagamento = textoOuPadrao(dados.statusPagamento(), PAGAMENTO_PADRAO).toUpperCase();
        this.valor = dados.valor() != null ? dados.valor() : BigDecimal.ZERO;
        this.observacoes = dados.observacoes();
        this.ativo = true;
    }

    public void atualizarInformacoes(DadosAtualizacaoEvento dados) {
        String novoInicio = dados.horaInicio() != null && !dados.horaInicio().isBlank()
                ? dados.horaInicio().trim()
                : this.horaInicio;
        String novoFim = dados.horaFim() != null && !dados.horaFim().isBlank()
                ? dados.horaFim().trim()
                : this.horaFim;
        validarIntervaloHorario(novoInicio, novoFim);

        if (dados.titulo() != null && !dados.titulo().isBlank()) {
            this.titulo = dados.titulo().trim();
        }
        if (dados.tipo() != null && !dados.tipo().isBlank()) {
            this.tipo = dados.tipo().trim();
        }
        if (dados.dataEvento() != null) {
            this.dataEvento = dados.dataEvento();
        }
        this.horaInicio = novoInicio;
        this.horaFim = novoFim;

        if (dados.logradouro() != null && !dados.logradouro().isBlank()) {
            this.logradouro = dados.logradouro().trim();
        }
        if (dados.numero() != null) {
            this.numero = dados.numero().trim();
        }
        if (dados.complemento() != null) {
            this.complemento = dados.complemento().trim();
        }
        if (dados.cep() != null) {
            this.cep = normalizarCep(dados.cep());
        }
        if (dados.status() != null && !dados.status().isBlank()) {
            this.status = dados.status().trim();
        }
        if (dados.statusPagamento() != null && !dados.statusPagamento().isBlank()) {
            this.statusPagamento = dados.statusPagamento().trim().toUpperCase();
        }
        if (dados.valor() != null) {
            this.valor = dados.valor();
        }
        if (dados.observacoes() != null) {
            this.observacoes = dados.observacoes();
        }
    }

    public void excluir() {
        this.ativo = false;
    }

    private static void validarIntervaloHorario(String inicio, String fim) {
        if (inicio == null || fim == null || inicio.isBlank() || fim.isBlank()) {
            return;
        }
        try {
            LocalTime horaInicial = LocalTime.parse(inicio.trim());
            LocalTime horaFinal = LocalTime.parse(fim.trim());
            if (horaFinal.isBefore(horaInicial)) {
                throw new ValidacaoException("O horário de término não pode ser anterior ao horário de início.");
            }
        } catch (ValidacaoException ex) {
            throw ex;
        } catch (Exception ignored) {
            // Formato validado pelo Bean Validation
        }
    }

    private static String normalizarCep(String cep) {
        if (cep == null || cep.isBlank()) {
            return null;
        }
        String digitos = cep.replaceAll("\\D", "");
        if (digitos.length() == 8) {
            return digitos.substring(0, 5) + "-" + digitos.substring(5);
        }
        return cep.trim().substring(0, Math.min(cep.trim().length(), 9));
    }

    private static String textoOuPadrao(String valor, String padrao) {
        return (valor != null && !valor.isBlank()) ? valor.trim() : padrao;
    }
}
