package com.eventcalend.api.controller;

import com.eventcalend.api.domain.ValidacaoException;
import com.eventcalend.api.domain.usuario.*;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@RequestMapping("/usuarios")
@CrossOrigin(origins = "*")
public class UsuarioController {

    private final UsuarioRepository repository;

    public UsuarioController(UsuarioRepository repository) {
        this.repository = repository;
    }

    @PostMapping
    @Transactional
    public ResponseEntity<DadosListagemUsuarioDetalhado> cadastrarUsuario(
            @RequestBody @Valid DadosCadastroUsuario dados,
            UriComponentsBuilder uriBuilder
    ) {
        var existente = repository.findByEmail(dados.email().trim());
        if (existente.isPresent()) {
            Usuario usuarioExistente = existente.get();
            if (usuarioExistente.isAtivo()) {
                throw new ValidacaoException("Já existe um contato cadastrado com este e-mail.");
            }
            usuarioExistente.reativarEAtualizar(dados);
            var uri = uriBuilder.path("/usuarios/{id}").buildAndExpand(usuarioExistente.getId()).toUri();
            return ResponseEntity.created(uri).body(new DadosListagemUsuarioDetalhado(usuarioExistente));
        }

        var usuario = new Usuario(dados);
        repository.save(usuario);
        var uri = uriBuilder.path("/usuarios/{id}").buildAndExpand(usuario.getId()).toUri();
        return ResponseEntity.created(uri).body(new DadosListagemUsuarioDetalhado(usuario));
    }

    @GetMapping
    public ResponseEntity<Page<DadosListagemUsuario>> listarUsuarios(
            @PageableDefault(size = 10, sort = {"nome"}) Pageable paginacao
    ) {
        var page = repository.findAllByAtivoTrue(paginacao).map(DadosListagemUsuario::new);
        return ResponseEntity.ok(page);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DadosListagemUsuarioDetalhado> detalharUsuario(@PathVariable Long id) {
        var usuario = repository.findByIdAndAtivoTrue(id)
                .orElseThrow(EntityNotFoundException::new);
        return ResponseEntity.ok(new DadosListagemUsuarioDetalhado(usuario));
    }

    @PutMapping
    @Transactional
    public ResponseEntity<DadosListagemUsuarioDetalhado> atualizarUsuario(
            @RequestBody @Valid DadosCadastroUsuarioAtualizado dados
    ) {
        var usuario = repository.findByIdAndAtivoTrue(dados.id())
                .orElseThrow(EntityNotFoundException::new);
        if (dados.email() != null && !dados.email().isBlank()
                && repository.existsByEmailAndIdNot(dados.email(), usuario.getId())) {
            throw new ValidacaoException("Já existe outro contato cadastrado com este e-mail.");
        }
        usuario.atualizarInformacoes(dados);
        return ResponseEntity.ok(new DadosListagemUsuarioDetalhado(usuario));
    }

    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<Void> deletarUsuario(@PathVariable Long id) {
        var usuario = repository.findByIdAndAtivoTrue(id)
                .orElseThrow(EntityNotFoundException::new);
        usuario.excluir();
        return ResponseEntity.noContent().build();
    }
}
