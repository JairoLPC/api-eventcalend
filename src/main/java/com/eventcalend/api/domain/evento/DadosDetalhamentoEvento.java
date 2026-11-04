package com.eventcalend.api.domain.evento;

import java.math.BigDecimal;
import java.time.LocalDate;

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
        String observacoes
) {
    public DadosDetalhamentoEvento(Evento evento) {
        this(
                evento.getId(),
                evento.getTitulo(),
                evento.getTipo(),
                evento.getDataEvento(),
                evento.getDataEvento() != null ? evento.getDataEvento().getDayOfMonth() : 1,
                evento.getDataEvento() != null ? evento.getDataEvento().getMonthValue() : 5,
                evento.getDataEvento() != null ? evento.getDataEvento().getYear() : 2025,
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
                evento.getValor() != null ? evento.getValor() : BigDecimal.ZERO,
                evento.getObservacoes()
        );
    }

    private static String montarLocal(Evento evento) {
        StringBuilder sb = new StringBuilder(evento.getLogradouro() != null ? evento.getLogradouro() : "");
        if (evento.getNumero() != null && !evento.getNumero().isBlank()) {
            sb.append(", ").append(evento.getNumero());
        }
        if (evento.getComplemento() != null && !evento.getComplemento().isBlank()) {
            sb.append(" (").append(evento.getComplemento()).append(")");
        }
        return sb.toString();
    }
}
