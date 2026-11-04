package com.eventcalend.api.domain.evento;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Table(name = "evento_compartilhamentos")
@Entity(name = "EventoCompartilhamento")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class EventoCompartilhamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "evento_id", nullable = false)
    private Long eventoId;

    @Column(name = "usuario_id", nullable = false)
    private Long usuarioId;

    @Column(name = "tipo_informacao", nullable = false)
    private String tipoInformacao;

    @Column(name = "data_compartilhamento", nullable = false)
    private LocalDateTime dataCompartilhamento;

    public EventoCompartilhamento(Long eventoId, Long usuarioId, String tipoInformacao) {
        this.eventoId = eventoId;
        this.usuarioId = usuarioId;
        this.tipoInformacao = tipoInformacao != null && !tipoInformacao.isBlank() ? tipoInformacao : "COMPLETO";
        this.dataCompartilhamento = LocalDateTime.now();
    }
}
