package com.eventcalend.api.domain.evento;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EventoCompartilhamentoRepository extends JpaRepository<EventoCompartilhamento, Long> {
    List<EventoCompartilhamento> findAllByUsuarioIdOrderByDataCompartilhamentoDesc(Long usuarioId);
}
