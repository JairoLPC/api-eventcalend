package com.eventcalend.api.domain.evento;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

public record DadosDetalhamentoEvento(
        Long id,
        String titulo,
        String tipo,
        LocalDate dataEvento,
        int dia,
        int mes,
        int ano,
        String horaInicio,
        String horaFim,
        String horarioFormatado,
        String logradouro,
        String numero,
        String complemento,
        String cep,
        String localFormatado,
        String status,
        String statusPagamento,
        BigDecimal valor,
        BigDecimal valorAdiantamento,
        BigDecimal custo,
        String observacoes
) {
    public DadosDetalhamentoEvento(Evento evento) {
        this(evento, Objects.requireNonNullElseGet(evento.getDataEvento(), LocalDate::now));
    }

    private DadosDetalhamentoEvento(Evento evento, LocalDate dataRef) {
        this(
                evento.getId(),
                evento.getTitulo(),
                evento.getTipo(),
                evento.getDataEvento(),
                dataRef.getDayOfMonth(),
                dataRef.getMonthValue(),
                dataRef.getYear(),
                evento.getHoraInicio(),
                evento.getHoraFim(),
                evento.getHoraInicio() + " - " + evento.getHoraFim(),
                evento.getLogradouro(),
                evento.getNumero(),
                evento.getComplemento(),
                evento.getCep(),
                montarLocal(evento),
                evento.getStatus(),
                evento.getStatusPagamento(),
                Objects.requireNonNullElse(evento.getValor(), BigDecimal.ZERO),
                Objects.requireNonNullElse(evento.getValorAdiantamento(), BigDecimal.ZERO),
                Objects.requireNonNullElse(evento.getCusto(), BigDecimal.ZERO),
                evento.getObservacoes()
        );
    }

    private static String montarLocal(Evento evento) {
        StringBuilder sb = new StringBuilder(Objects.requireNonNullElse(evento.getLogradouro(), ""));
        if (evento.getNumero() != null && !evento.getNumero().isBlank()) {
            sb.append(", ").append(evento.getNumero());
        }
        if (evento.getComplemento() != null && !evento.getComplemento().isBlank()) {
            sb.append(" (").append(evento.getComplemento()).append(")");
        }
        return sb.toString();
    }
}
