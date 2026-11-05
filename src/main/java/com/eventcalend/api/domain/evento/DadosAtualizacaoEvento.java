package com.eventcalend.api.domain.evento;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record DadosAtualizacaoEvento(
        @NotNull(message = "O ID do evento é obrigatório")
        Long id,
        @Size(max = 150, message = "O nome do evento deve ter no máximo 150 caracteres")
        String titulo,
        @Size(max = 60, message = "O tipo deve ter no máximo 60 caracteres")
        String tipo,
        LocalDate dataEvento,
        @Pattern(regexp = "^([01]\\d|2[0-3]):[0-5]\\d$", message = "Horário de início inválido (use HH:mm)")
        String horaInicio,
        @Pattern(regexp = "^([01]\\d|2[0-3]):[0-5]\\d$", message = "Horário de término inválido (use HH:mm)")
        String horaFim,
        @Size(max = 150, message = "O logradouro deve ter no máximo 150 caracteres")
        String logradouro,
        @Size(max = 20, message = "O número deve ter no máximo 20 caracteres")
        String numero,
        @Size(max = 100, message = "O complemento deve ter no máximo 100 caracteres")
        String complemento,
        @Pattern(regexp = "^$|\\d{5}-?\\d{3}", message = "O CEP deve conter 8 dígitos (ex.: 01310-100)")
        String cep,
        String status,
        @Pattern(regexp = "(?i)^(PAGO|ADIANTAMENTO|PENDENTE)$", message = "Status de pagamento inválido")
        String statusPagamento,
        @PositiveOrZero(message = "O valor não pode ser negativo")
        BigDecimal valor,
        @PositiveOrZero(message = "O valor do adiantamento não pode ser negativo")
        BigDecimal valorAdiantamento,
        String observacoes
) {
}
