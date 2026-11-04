package com.eventcalend.api.domain.evento;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EventoRepository extends JpaRepository<Evento, Long> {
    List<Evento> findAllByAtivoTrueOrderByDataEventoAscHoraInicioAsc();

    Optional<Evento> findByIdAndAtivoTrue(Long id);
}
