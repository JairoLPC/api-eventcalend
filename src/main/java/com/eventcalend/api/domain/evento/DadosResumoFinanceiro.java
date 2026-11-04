package com.eventcalend.api.domain.evento;

import java.math.BigDecimal;

public record DadosResumoFinanceiro(
        BigDecimal saldoPrevisto,
        BigDecimal totalPago,
        long quantidadePagos,
        BigDecimal totalAdiantamento,
        long quantidadeAdiantamentos,
        BigDecimal totalAReceber,
        long quantidadeAReceber
) {
}
