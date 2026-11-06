package com.eventcalend.api.controller;

import com.eventcalend.api.domain.evento.*;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/eventos")
public class EventoController {

    private final EventoService eventoService;

    public EventoController(EventoService eventoService) {
        this.eventoService = eventoService;
    }

    @GetMapping
    public ResponseEntity<List<DadosDetalhamentoEvento>> listarEventos() {
        return ResponseEntity.ok(eventoService.listarAtivos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<DadosDetalhamentoEvento> detalharEvento(@PathVariable Long id) {
        return ResponseEntity.ok(eventoService.detalharPorId(id));
    }

    @PostMapping
    public ResponseEntity<DadosDetalhamentoEvento> criarEvento(
            @RequestBody @Valid DadosCadastroEvento dados,
            UriComponentsBuilder uriBuilder
    ) {
        Evento evento = eventoService.criar(dados);
        var uri = uriBuilder.path("/eventos/{id}").buildAndExpand(evento.getId()).toUri();
        return ResponseEntity.created(uri).body(new DadosDetalhamentoEvento(evento));
    }

    @PutMapping
    public ResponseEntity<DadosDetalhamentoEvento> atualizarEvento(
            @RequestBody @Valid DadosAtualizacaoEvento dados
    ) {
        return ResponseEntity.ok(eventoService.atualizar(dados));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluirEvento(@PathVariable Long id) {
        eventoService.excluir(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/resumo-financeiro")
    public ResponseEntity<DadosResumoFinanceiro> obterResumoFinanceiro() {
        return ResponseEntity.ok(eventoService.calcularResumoFinanceiro());
    }

    @PostMapping("/{id}/compartilhar")
    public ResponseEntity<Map<String, Object>> compartilharEvento(
            @PathVariable Long id,
            @RequestBody @Valid DadosCompartilhamentoEvento dados
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(eventoService.compartilharComContato(id, dados));
    }
}
