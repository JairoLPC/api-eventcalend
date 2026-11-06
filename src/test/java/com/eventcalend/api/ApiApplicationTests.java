package com.eventcalend.api;

import com.eventcalend.api.controller.AutenticacaoController;
import com.eventcalend.api.controller.EventoController;
import com.eventcalend.api.controller.UsuarioController;
import com.eventcalend.api.domain.ValidacaoException;
import com.eventcalend.api.domain.endereco.DadosEndereco;
import com.eventcalend.api.domain.evento.*;
import com.eventcalend.api.domain.usuario.*;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@SpringBootTest
@Transactional
class ApiApplicationTests {

    @Autowired
    private EventoController eventoController;

    @Autowired
    private UsuarioController usuarioController;

    @Autowired
    private AutenticacaoController autenticacaoController;

    @Test
    void contextLoads() {
        assertNotNull(eventoController);
        assertNotNull(usuarioController);
        assertNotNull(autenticacaoController);
    }

    @Test
    void deveExecutarFluxoCompletoDeEventosNosControllers() {
        var listaInicial = eventoController.listarEventos();
        assertEquals(HttpStatus.OK, listaInicial.getStatusCode());
        assertNotNull(listaInicial.getBody());

        var novoEvento = new DadosCadastroEvento(
                "Festival Teste Integração",
                "Festival",
                LocalDate.of(2026, 11, 15),
                "14:00",
                "22:00",
                "Parque Ibirapuera",
                "S/N",
                "Portão 3",
                "04094050",
                "Confirmado",
                "ADIANTAMENTO",
                new BigDecimal("5000.00"),
                new BigDecimal("1500.00"),
                "Teste automatizado"
        );

        var respostaCriacao = eventoController.criarEvento(novoEvento, UriComponentsBuilder.newInstance());
        assertEquals(HttpStatus.CREATED, respostaCriacao.getStatusCode());
        assertNotNull(respostaCriacao.getBody());
        Long idCriado = respostaCriacao.getBody().id();
        assertNotNull(idCriado);
        assertEquals("04094-050", respostaCriacao.getBody().cep());

        var resumo = eventoController.obterResumoFinanceiro();
        assertEquals(HttpStatus.OK, resumo.getStatusCode());
        assertNotNull(resumo.getBody());

        var respostaExclusao = eventoController.excluirEvento(idCriado);
        assertEquals(HttpStatus.NO_CONTENT, respostaExclusao.getStatusCode());
    }

    @Test
    void deveExecutarFluxoCompletoDeUsuariosEAutenticacao() {
        var endereco = new DadosEndereco("Rua Teste", "123", "Sala 1", "Centro", "São Paulo", "SP", "01001-000");
        var novoUsuario = new DadosCadastroUsuario(
                "João Integracao",
                "joao.integracao.teste@eventcalend.com",
                "11988887777",
                TipoDeUsuario.MUSICO,
                endereco
        );

        var criado = usuarioController.cadastrarUsuario(novoUsuario, UriComponentsBuilder.newInstance());
        assertEquals(HttpStatus.CREATED, criado.getStatusCode());
        assertNotNull(criado.getBody());
        Long usuarioId = criado.getBody().id();

        var pagina = usuarioController.listarUsuarios(PageRequest.of(0, 10));
        assertEquals(HttpStatus.OK, pagina.getStatusCode());
        assertTrue(pagina.getBody().getTotalElements() >= 1);

        var deletado = usuarioController.deletarUsuario(usuarioId);
        assertEquals(HttpStatus.NO_CONTENT, deletado.getStatusCode());

        // Reativação ao cadastrar novamente com o mesmo e-mail
        var reativado = usuarioController.cadastrarUsuario(novoUsuario, UriComponentsBuilder.newInstance());
        assertEquals(HttpStatus.CREATED, reativado.getStatusCode());
        assertEquals(usuarioId, reativado.getBody().id());

        // Registro e login no AutenticacaoController
        var registro = new DadosRegistroCadastro(
                "Produtor Teste",
                "produtor.teste.integracao@eventcalend.com",
                "(11) 97777-6666",
                "senha123",
                "senha123",
                "Produtor Musical"
        );
        var respRegistro = autenticacaoController.registrar(registro);
        assertEquals(HttpStatus.CREATED, respRegistro.getStatusCode());
        assertEquals("Produtor Teste", respRegistro.getBody().nome());

        var respLogin = autenticacaoController.efetuarLogin(
                new DadosAutenticacao("produtor.teste.integracao@eventcalend.com", "senha123")
        );
        assertEquals(HttpStatus.OK, respLogin.getStatusCode());
        assertEquals("produtor.teste.integracao@eventcalend.com", respLogin.getBody().loguin());
    }

    @Test
    void deveCriarEAtualizarEventoCorretamente() {
        var cadastro = new DadosCadastroEvento(
                "Show Acústico",
                "Musical",
                LocalDate.of(2026, 12, 20),
                "18:00",
                "22:00",
                "Av. Paulista",
                "1000",
                "Sala 2",
                "01310100",
                "Confirmado",
                "adiantamento",
                new BigDecimal("2000.00"),
                new BigDecimal("500.00"),
                "Levar som"
        );

        Evento evento = new Evento(cadastro);
        assertEquals("Show Acústico", evento.getTitulo());
        assertEquals("01310-100", evento.getCep());
        assertEquals("ADIANTAMENTO", evento.getStatusPagamento());
        assertTrue(evento.isAtivo());

        var atualizacao = new DadosAtualizacaoEvento(
                1L,
                "Show Acústico Especial",
                null,
                null,
                "19:00",
                "23:00",
                null,
                null,
                null,
                null,
                "Concluído",
                "pago",
                new BigDecimal("2500.00"),
                new BigDecimal("500.00"),
                null
        );

        evento.atualizarInformacoes(atualizacao);
        assertEquals("Show Acústico Especial", evento.getTitulo());
        assertEquals("Musical", evento.getTipo());
        assertEquals("19:00", evento.getHoraInicio());
        assertEquals("23:00", evento.getHoraFim());
        assertEquals("PAGO", evento.getStatusPagamento());
        assertEquals(new BigDecimal("2500.00"), evento.getValor());

        evento.excluir();
        assertFalse(evento.isAtivo());
    }

    @Test
    void deveRejeitarAdiantamentoMaiorQueValorTotal() {
        var cadastroInvalido = new DadosCadastroEvento(
                "Evento Teste",
                "Show",
                LocalDate.of(2026, 12, 20),
                "10:00",
                "12:00",
                "Rua A",
                "10",
                null,
                "01310-100",
                "Confirmado",
                "ADIANTAMENTO",
                new BigDecimal("1000.00"),
                new BigDecimal("1500.00"),
                null
        );

        assertThrows(ValidacaoException.class, () -> new Evento(cadastroInvalido));
    }

    @Test
    void deveRejeitarHorarioFimAnteriorAoInicio() {
        var cadastroInvalido = new DadosCadastroEvento(
                "Evento Teste",
                "Show",
                LocalDate.of(2026, 12, 20),
                "18:00",
                "14:00",
                "Rua A",
                "10",
                null,
                "01310-100",
                "Confirmado",
                "PENDENTE",
                new BigDecimal("1000.00"),
                BigDecimal.ZERO,
                null
        );

        assertThrows(ValidacaoException.class, () -> new Evento(cadastroInvalido));
    }

    @Test
    void deveCalcularResumoFinanceiroCorretamente() {
        EventoRepository eventoRepo = Mockito.mock(EventoRepository.class);
        EventoCompartilhamentoRepository compRepo = Mockito.mock(EventoCompartilhamentoRepository.class);
        UsuarioRepository usuarioRepo = Mockito.mock(UsuarioRepository.class);

        Evento evPago = new Evento(new DadosCadastroEvento(
                "Ev 1", "Show", LocalDate.now(), "10:00", "12:00", "Rua 1", "1", null, null,
                "Confirmado", "PAGO", new BigDecimal("1000.00"), BigDecimal.ZERO, null
        ));
        Evento evAdiantamento = new Evento(new DadosCadastroEvento(
                "Ev 2", "Show", LocalDate.now(), "13:00", "15:00", "Rua 2", "2", null, null,
                "Confirmado", "ADIANTAMENTO", new BigDecimal("2000.00"), new BigDecimal("800.00"), null
        ));
        Evento evPendente = new Evento(new DadosCadastroEvento(
                "Ev 3", "Show", LocalDate.now(), "16:00", "18:00", "Rua 3", "3", null, null,
                "Confirmado", "PENDENTE", new BigDecimal("500.00"), BigDecimal.ZERO, null
        ));

        when(eventoRepo.findAllByAtivoTrueOrderByDataEventoAscHoraInicioAsc())
                .thenReturn(List.of(evPago, evAdiantamento, evPendente));

        EventoService service = new EventoService(eventoRepo, compRepo, usuarioRepo);
        DadosResumoFinanceiro resumo = service.calcularResumoFinanceiro();

        assertEquals(new BigDecimal("1000.00"), resumo.totalPago());
        assertEquals(1, resumo.quantidadePagos());
        assertEquals(new BigDecimal("800.00"), resumo.totalAdiantamento());
        assertEquals(1, resumo.quantidadeAdiantamentos());
        assertEquals(new BigDecimal("1700.00"), resumo.totalAReceber());
        assertEquals(2, resumo.quantidadeAReceber());
        assertEquals(new BigDecimal("3500.00"), resumo.saldoPrevisto());
    }

    @Test
    void deveCriarAtualizarEReativarUsuario() {
        var endereco = new DadosEndereco("Rua X", "10", "Ap 1", "Centro", "São Paulo", "SP", "01001-000");
        var cadastro = new DadosCadastroUsuario("Maria", "maria@email.com", "11999998888", TipoDeUsuario.ARTISTA, endereco);

        Usuario usuario = new Usuario(cadastro);
        assertTrue(usuario.isAtivo());
        assertEquals("Maria", usuario.getNome());
        assertEquals("São Paulo", usuario.getEndereco().getCidade());

        usuario.excluir();
        assertFalse(usuario.isAtivo());

        usuario.reativarEAtualizar(cadastro);
        assertTrue(usuario.isAtivo());

        var atualizacao = new DadosCadastroUsuarioAtualizado(1L, "Maria Silva", null, null, TipoDeUsuario.BANDA, null);
        usuario.atualizarInformacoes(atualizacao);
        assertEquals("Maria Silva", usuario.getNome());
        assertEquals("maria@email.com", usuario.getEmail());
        assertEquals(TipoDeUsuario.BANDA, usuario.getTipodeusuario());
    }
}
