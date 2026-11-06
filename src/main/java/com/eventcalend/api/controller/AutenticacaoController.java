package com.eventcalend.api.controller;

import com.eventcalend.api.domain.ValidacaoException;
import com.eventcalend.api.domain.usuario.*;
import com.eventcalend.api.infra.email.EmailService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.security.SecureRandom;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/login")
public class AutenticacaoController {

    private static final String TELEFONE_PADRAO = "(11) 98765-4321";
    private static final String PROFISSAO_PADRAO = "Produtor(a) de eventos";
    private static final long TEMPO_EXPIRACAO_TOKEN_MS = 15 * 60 * 1000; // 15 minutos
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private static final Map<String, TokenRecuperacao> TOKENS_RECUPERACAO = new ConcurrentHashMap<>();

    public record DadosPerfilUsuario(Long id, String loguin, String nome, String telefone, String profissao) {
    }

    private record TokenRecuperacao(String codigo, long expiracaoMs, boolean verificado) {
        boolean estaExpirado() {
            return System.currentTimeMillis() > expiracaoMs;
        }

        TokenRecuperacao comVerificado(boolean novoStatus) {
            return new TokenRecuperacao(this.codigo, this.expiracaoMs, novoStatus);
        }
    }

    private final AuthenticationManager manager;
    private final CadastroRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    public AutenticacaoController(
            AuthenticationManager manager,
            CadastroRepository repository,
            PasswordEncoder passwordEncoder,
            EmailService emailService
    ) {
        this.manager = manager;
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }

    @PostMapping
    public ResponseEntity<DadosPerfilUsuario> efetuarLogin(@RequestBody @Valid DadosAutenticacao dados) {
        var token = new UsernamePasswordAuthenticationToken(dados.loguin(), dados.senha());
        var authentication = manager.authenticate(token);
        var usuarioLogado = (Cadastro) authentication.getPrincipal();
        return ResponseEntity.ok(montarRespostaPerfil(usuarioLogado));
    }

    @GetMapping("/buscar")
    @Transactional(readOnly = true)
    public ResponseEntity<List<DadosPerfilUsuario>> buscarUsuariosDoApp(
            @RequestParam(name = "termo", required = false, defaultValue = "") String termo
    ) {
        String busca = termo.trim();
        if (busca.isEmpty()) {
            return ResponseEntity.ok(List.of());
        }
        List<DadosPerfilUsuario> resultado = repository.buscarPorNomeOuEmail(busca).stream()
                .map(this::montarRespostaPerfil)
                .toList();
        return ResponseEntity.ok(resultado);
    }

    @PostMapping("/registrar")
    @Transactional
    public ResponseEntity<DadosPerfilUsuario> registrar(@RequestBody @Valid DadosRegistroCadastro dados) {
        if (!dados.senha().equals(dados.confirmacaoSenha())) {
            throw new ValidacaoException("A senha e a confirmação de senha não conferem.");
        }
        String email = dados.loguin().trim();
        if (repository.findByLoguin(email).isPresent()) {
            throw new ValidacaoException("Já existe uma conta com este e-mail.");
        }
        var novoCadastro = repository.save(new Cadastro(
                null,
                email,
                passwordEncoder.encode(dados.senha()),
                dados.nome().trim(),
                formatarTelefonePadrao(dados.telefone()),
                textoOuPadrao(dados.profissao(), PROFISSAO_PADRAO)
        ));
        return ResponseEntity.status(HttpStatus.CREATED).body(montarRespostaPerfil(novoCadastro));
    }

    @PutMapping("/perfil")
    @Transactional
    public ResponseEntity<DadosPerfilUsuario> atualizarPerfil(@RequestBody @Valid DadosAtualizacaoPerfil dados) {
        Cadastro cadastro = localizarCadastroParaAtualizacao(dados);

        if (repository.existsByLoguinAndIdNot(dados.loguin(), cadastro.getId())) {
            throw new ValidacaoException("Já existe outra conta utilizando este e-mail.");
        }

        cadastro.atualizarPerfil(dados.nome(), dados.loguin(), dados.telefone(), dados.profissao());
        return ResponseEntity.ok(montarRespostaPerfil(cadastro));
    }

    @PostMapping("/esqueci-senha")
    @Transactional(readOnly = true)
    public ResponseEntity<Map<String, String>> solicitarCodigoRecuperacao(@RequestBody @Valid DadosSolicitacaoCodigo dados) {
        String email = dados.loguin().trim();
        repository.findByLoguin(email)
                .orElseThrow(() -> new ValidacaoException("Nenhuma conta encontrada com o e-mail: " + email));

        String codigo = String.format("%06d", SECURE_RANDOM.nextInt(900000) + 100000);
        long expiracaoMs = System.currentTimeMillis() + TEMPO_EXPIRACAO_TOKEN_MS;

        TOKENS_RECUPERACAO.put(email.toLowerCase(), new TokenRecuperacao(codigo, expiracaoMs, false));
        emailService.enviarCodigoRecuperacao(email, codigo);

        String mensagem = "Enviamos o código de verificação para " + email + ". Confira a caixa de entrada ou spam do seu e-mail.";
        return ResponseEntity.ok(Map.of("mensagem", mensagem, "email", email));
    }

    @PostMapping("/validar-codigo")
    public ResponseEntity<Map<String, String>> validarCodigoRecuperacao(@RequestBody @Valid DadosValidacaoCodigo dados) {
        String email = dados.loguin().trim();
        TokenRecuperacao token = obterTokenValido(email);

        if (!token.codigo().equals(dados.codigo().trim())) {
            throw new ValidacaoException("Código de verificação incorreto. Verifique o e-mail recebido.");
        }

        TOKENS_RECUPERACAO.put(email.toLowerCase(), token.comVerificado(true));
        return ResponseEntity.ok(Map.of("mensagem", "Código verificado com sucesso! Agora defina a sua nova senha."));
    }

    @PostMapping("/confirmar-recuperacao")
    @Transactional
    public ResponseEntity<Map<String, String>> confirmarRecuperacao(@RequestBody @Valid DadosConfirmacaoCodigo dados) {
        String email = dados.loguin().trim();
        TokenRecuperacao token = obterTokenValido(email);

        if (!token.verificado() || !token.codigo().equals(dados.codigo().trim())) {
            throw new ValidacaoException("Validação pendente ou código incorreto. Valide seu código de verificação primeiro.");
        }

        Cadastro cadastro = repository.findByLoguin(email)
                .orElseThrow(() -> new ValidacaoException("Conta não encontrada."));

        cadastro.atualizarSenha(passwordEncoder.encode(dados.novaSenha()));
        TOKENS_RECUPERACAO.remove(email.toLowerCase());

        return ResponseEntity.ok(Map.of("mensagem", "Senha redefinida com sucesso! Você já pode fazer login com sua nova senha."));
    }

    @PutMapping("/redefinir-senha")
    @Transactional
    public ResponseEntity<Map<String, String>> redefinirSenha(@RequestBody @Valid DadosRedefinicaoSenha dados) {
        Cadastro cadastro = repository.findByLoguin(dados.loguin().trim())
                .orElseThrow(() -> new ValidacaoException("Nenhuma conta encontrada com este e-mail."));
        cadastro.atualizarSenha(passwordEncoder.encode(dados.novaSenha()));
        return ResponseEntity.ok(Map.of("mensagem", "Senha redefinida com sucesso! Você já pode fazer login."));
    }

    private static TokenRecuperacao obterTokenValido(String email) {
        String chave = email.toLowerCase();
        TokenRecuperacao token = TOKENS_RECUPERACAO.get(chave);
        if (token == null) {
            throw new ValidacaoException("Nenhuma solicitação de código encontrada para este e-mail. Solicite um novo código.");
        }
        if (token.estaExpirado()) {
            TOKENS_RECUPERACAO.remove(chave);
            throw new ValidacaoException("O código de verificação expirou. Por favor, solicite um novo código.");
        }
        return token;
    }

    private Cadastro localizarCadastroParaAtualizacao(DadosAtualizacaoPerfil dados) {
        if (dados.id() != null) {
            return repository.findById(dados.id()).orElseThrow(EntityNotFoundException::new);
        }
        String loginBusca = (dados.loguinAtual() != null && !dados.loguinAtual().isBlank())
                ? dados.loguinAtual()
                : dados.loguin();
        return repository.findByLoguin(loginBusca)
                .orElseThrow(EntityNotFoundException::new);
    }

    private DadosPerfilUsuario montarRespostaPerfil(Cadastro cadastro) {
        return new DadosPerfilUsuario(
                cadastro.getId(),
                cadastro.getUsername(),
                textoOuPadrao(cadastro.getNome(), extrairNomePadrao(cadastro.getUsername())),
                textoOuPadrao(cadastro.getTelefone(), TELEFONE_PADRAO),
                textoOuPadrao(cadastro.getProfissao(), PROFISSAO_PADRAO)
        );
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
