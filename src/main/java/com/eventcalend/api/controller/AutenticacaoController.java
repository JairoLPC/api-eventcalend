package com.eventcalend.api.controller;

import com.eventcalend.api.domain.ValidacaoException;
import com.eventcalend.api.domain.usuario.Cadastro;
import com.eventcalend.api.domain.usuario.CadastroRepository;
import com.eventcalend.api.domain.usuario.DadosAtualizacaoPerfil;
import com.eventcalend.api.domain.usuario.DadosAutenticacao;
import com.eventcalend.api.domain.usuario.DadosRedefinicaoSenha;
import com.eventcalend.api.domain.usuario.DadosRegistroCadastro;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/login")
@CrossOrigin(origins = "*")
public class AutenticacaoController {

    private static final String TELEFONE_PADRAO = "(11) 98765-4321";
    private static final String PROFISSAO_PADRAO = "Produtor(a) de eventos";

    private final AuthenticationManager manager;
    private final CadastroRepository repository;
    private final PasswordEncoder passwordEncoder;

    public AutenticacaoController(
            AuthenticationManager manager,
            CadastroRepository repository,
            PasswordEncoder passwordEncoder
    ) {
        this.manager = manager;
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> efetuarLogin(@RequestBody @Valid DadosAutenticacao dados) {
        var token = new UsernamePasswordAuthenticationToken(dados.loguin(), dados.senha());
        var authentication = manager.authenticate(token);
        var usuarioLogado = (Cadastro) authentication.getPrincipal();
        return ResponseEntity.ok(montarRespostaPerfil(usuarioLogado));
    }

    @GetMapping("/buscar")
    @Transactional(readOnly = true)
    public ResponseEntity<List<Map<String, Object>>> buscarUsuariosDoApp(
            @RequestParam(name = "termo", required = false, defaultValue = "") String termo
    ) {
        List<Cadastro> encontrados = repository.buscarPorNomeOuEmail(termo.trim());
        List<Map<String, Object>> resultado = encontrados.stream()
                .map(this::montarRespostaPerfil)
                .toList();
        return ResponseEntity.ok(resultado);
    }

    @PostMapping("/registrar")
    @Transactional
    public ResponseEntity<Map<String, Object>> registrar(@RequestBody @Valid DadosRegistroCadastro dados) {
        if (!dados.senha().equals(dados.confirmacaoSenha())) {
            throw new ValidacaoException("A senha e a confirmação de senha não conferem.");
        }
        if (repository.findByLoguin(dados.loguin().trim()) != null) {
            throw new ValidacaoException("Já existe uma conta com este e-mail.");
        }
        var senhaCriptografada = passwordEncoder.encode(dados.senha());
        var novoCadastro = new Cadastro(
                null,
                dados.loguin().trim(),
                senhaCriptografada,
                dados.nome().trim(),
                formatarTelefonePadrao(dados.telefone()),
                textoOuPadrao(dados.profissao(), PROFISSAO_PADRAO)
        );
        repository.save(novoCadastro);
        return ResponseEntity.status(HttpStatus.CREATED).body(montarRespostaPerfil(novoCadastro));
    }

    @PutMapping("/perfil")
    @Transactional
    public ResponseEntity<Map<String, Object>> atualizarPerfil(@RequestBody @Valid DadosAtualizacaoPerfil dados) {
        Cadastro cadastro = localizarCadastroParaAtualizacao(dados);

        if (repository.existsByLoguinAndIdNot(dados.loguin(), cadastro.getId())) {
            throw new ValidacaoException("Já existe outra conta utilizando este e-mail.");
        }

        cadastro.atualizarPerfil(dados.nome(), dados.loguin(), dados.telefone(), dados.profissao());
        return ResponseEntity.ok(montarRespostaPerfil(cadastro));
    }

    @PutMapping("/redefinir-senha")
    @Transactional
    public ResponseEntity<Map<String, String>> redefinirSenha(@RequestBody @Valid DadosRedefinicaoSenha dados) {
        Cadastro cadastro = repository.findCadastroByLoguin(dados.loguin().trim())
                .orElseThrow(() -> new ValidacaoException("Nenhuma conta encontrada com este e-mail."));
        cadastro.atualizarSenha(passwordEncoder.encode(dados.novaSenha()));
        return ResponseEntity.ok(Map.of("mensagem", "Senha redefinida com sucesso! Você já pode fazer login."));
    }

    private Cadastro localizarCadastroParaAtualizacao(DadosAtualizacaoPerfil dados) {
        if (dados.id() != null) {
            return repository.findById(dados.id()).orElseThrow(EntityNotFoundException::new);
        }
        if (dados.loguinAtual() != null && !dados.loguinAtual().isBlank()) {
            return repository.findCadastroByLoguin(dados.loguinAtual())
                    .orElseThrow(EntityNotFoundException::new);
        }
        return repository.findCadastroByLoguin(dados.loguin())
                .orElseThrow(EntityNotFoundException::new);
    }

    private Map<String, Object> montarRespostaPerfil(Cadastro cadastro) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("id", cadastro.getId());
        body.put("loguin", cadastro.getUsername());
        body.put("nome", textoOuPadrao(cadastro.getNome(), extrairNomePadrao(cadastro.getUsername())));
        body.put("telefone", textoOuPadrao(cadastro.getTelefone(), TELEFONE_PADRAO));
        body.put("profissao", textoOuPadrao(cadastro.getProfissao(), PROFISSAO_PADRAO));
        return body;
    }

    private static String formatarTelefonePadrao(String telefone) {
        if (telefone == null || telefone.isBlank()) {
            return TELEFONE_PADRAO;
        }
        String digitos = telefone.replaceAll("\\D", "");
        if (digitos.length() == 11) {
            return String.format("(%s) %s-%s", digitos.substring(0, 2), digitos.substring(2, 7), digitos.substring(7));
        }
        if (digitos.length() == 10) {
            return String.format("(%s) %s-%s", digitos.substring(0, 2), digitos.substring(2, 6), digitos.substring(6));
        }
        return telefone.trim();
    }

    private static String textoOuPadrao(String valor, String padrao) {
        return (valor != null && !valor.isBlank()) ? valor.trim() : padrao;
    }

    private static String extrairNomePadrao(String email) {
        if (email == null || email.isBlank()) {
            return "Usuário";
        }
        String base = email.split("@")[0].replace('.', ' ').replace('_', ' ').trim();
        if (base.isEmpty()) {
            return "Usuário";
        }
        return Character.toUpperCase(base.charAt(0)) + base.substring(1);
    }
}
