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
import java.util.Objects;

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

    @Column(name = "valor_adiantamento")
    private BigDecimal valorAdiantamento;

    @Column(columnDefinition = "TEXT")
    private String observacoes;

    private boolean ativo;

    public Evento(DadosCadastroEvento dados) {
        BigDecimal valorTotal = Objects.requireNonNullElse(dados.valor(), BigDecimal.ZERO);
        BigDecimal adiantamento = Objects.requireNonNullElse(dados.valorAdiantamento(), BigDecimal.ZERO);
        validarIntervaloHorario(dados.horaInicio(), dados.horaFim());
        validarValores(valorTotal, adiantamento);

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
        this.valor = valorTotal;
        this.valorAdiantamento = adiantamento;
        this.observacoes = dados.observacoes();
        this.ativo = true;
    }

    public void atualizarInformacoes(DadosAtualizacaoEvento dados) {
        String novoInicio = textoOuPadrao(dados.horaInicio(), this.horaInicio);
        String novoFim = textoOuPadrao(dados.horaFim(), this.horaFim);
        BigDecimal novoValor = Objects.requireNonNullElse(dados.valor(), this.valor);
        BigDecimal novoAdiantamento = Objects.requireNonNullElse(dados.valorAdiantamento(), this.valorAdiantamento);

        validarIntervaloHorario(novoInicio, novoFim);
        validarValores(novoValor, novoAdiantamento);

        this.titulo = textoOuPadrao(dados.titulo(), this.titulo);
        this.tipo = textoOuPadrao(dados.tipo(), this.tipo);
        this.dataEvento = Objects.requireNonNullElse(dados.dataEvento(), this.dataEvento);
        this.horaInicio = novoInicio;
        this.horaFim = novoFim;
        this.logradouro = textoOuPadrao(dados.logradouro(), this.logradouro);
        this.numero = dados.numero() != null ? dados.numero().trim() : this.numero;
        this.complemento = dados.complemento() != null ? dados.complemento().trim() : this.complemento;
        this.cep = dados.cep() != null ? normalizarCep(dados.cep()) : this.cep;
        this.status = textoOuPadrao(dados.status(), this.status);
        this.statusPagamento = textoOuPadrao(dados.statusPagamento(), this.statusPagamento).toUpperCase();
        this.valor = novoValor;
        this.valorAdiantamento = novoAdiantamento;
        this.observacoes = Objects.requireNonNullElse(dados.observacoes(), this.observacoes);
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

    private static void validarValores(BigDecimal valorTotal, BigDecimal adiantamento) {
        if (valorTotal != null && adiantamento != null && adiantamento.compareTo(valorTotal) > 0) {
            throw new ValidacaoException("O valor do adiantamento não pode ser maior que o valor total do evento.");
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
