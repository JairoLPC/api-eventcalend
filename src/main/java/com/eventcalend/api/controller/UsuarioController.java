package com.eventcalend.api.controller;

import com.eventcalend.api.usuario.*;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/usuarios")

public class UsuarioController {

    @Autowired
    private UsuarioRepository repository;

    @PostMapping
    @Transactional
    public String criarUsuario(@RequestBody @Valid DadosCadastroUsuario dados) {
        repository.save(new Usuario(dados));

        return "";
    }
    @GetMapping
    public Page<DadosListagemUsuario> listarUsuarios(Pageable pageable) {
        return repository.findAllByAtivoTrue(pageable).map(DadosListagemUsuario::new);
    }

    @PutMapping
    @Transactional
    public void atualizarUsuario(@RequestBody @Valid DadosCadastroUsuarioAtualizado dados) {
        var usuario = repository.getReferenceById(dados.id());
        usuario.atualizarInformacoes(dados);
    }

    @DeleteMapping("/{id}")
    @Transactional
    public void excluirUsuario(@PathVariable Long id) {
        var usuario = repository.getReferenceById(id);
        usuario.excluir();
    }


}
