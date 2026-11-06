package com.eventcalend.api.domain.evento;

import java.math.BigDecimal;

public record DadosResultadoExtracao(
    String titulo,
    String tipo,
    String dataEvento,
    String horaInicio,
    String horaFim,
    String logradouro,
    BigDecimal valor,
    String statusPagamento,
    BigDecimal valorAdiantamento,
    String observacoes
) {}
