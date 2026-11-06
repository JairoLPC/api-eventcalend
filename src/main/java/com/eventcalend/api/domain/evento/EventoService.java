package com.eventcalend.api.domain.evento;

import com.eventcalend.api.domain.usuario.Usuario;
import com.eventcalend.api.domain.usuario.UsuarioRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class EventoService {

    private static final String STATUS_PAGO = "PAGO";
    private static final String STATUS_ADIANTAMENTO = "ADIANTAMENTO";
    private static final String STATUS_PENDENTE = "PENDENTE";
    private static final String COMPARTILHAMENTO_COMPLETO = "COMPLETO";

    private final EventoRepository eventoRepository;
    private final EventoCompartilhamentoRepository compartilhamentoRepository;
    private final UsuarioRepository usuarioRepository;

    public EventoService(
            EventoRepository eventoRepository,
            EventoCompartilhamentoRepository compartilhamentoRepository,
            UsuarioRepository usuarioRepository
    ) {
        this.eventoRepository = eventoRepository;
        this.compartilhamentoRepository = compartilhamentoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional(readOnly = true)
    public List<DadosDetalhamentoEvento> listarAtivos() {
        return eventoRepository.findAllByAtivoTrueOrderByDataEventoAscHoraInicioAsc()
                .stream()
                .map(DadosDetalhamentoEvento::new)
                .toList();
    }

    @Transactional(readOnly = true)
    public DadosDetalhamentoEvento detalharPorId(Long id) {
        return new DadosDetalhamentoEvento(buscarEventoAtivo(id));
    }

    @Transactional
    public Evento criar(DadosCadastroEvento dados) {
        return eventoRepository.save(new Evento(dados));
    }

    @Transactional
    public DadosDetalhamentoEvento atualizar(DadosAtualizacaoEvento dados) {
        Evento evento = buscarEventoAtivo(dados.id());
        evento.atualizarInformacoes(dados);
        return new DadosDetalhamentoEvento(evento);
    }

    @Transactional
    public void excluir(Long id) {
        buscarEventoAtivo(id).excluir();
    }

    @Transactional(readOnly = true)
    public DadosResumoFinanceiro calcularResumoFinanceiro() {
        List<Evento> eventos = eventoRepository.findAllByAtivoTrueOrderByDataEventoAscHoraInicioAsc();

        BigDecimal totalPago = BigDecimal.ZERO;
        long qtdPagos = 0;

        BigDecimal totalAdiantamento = BigDecimal.ZERO;
        long qtdAdiantamentos = 0;

        BigDecimal totalAReceber = BigDecimal.ZERO;
        long qtdAReceber = 0;

        BigDecimal totalCustos = BigDecimal.ZERO;

        for (Evento evento : eventos) {
            BigDecimal valorTotal = Objects.requireNonNullElse(evento.getValor(), BigDecimal.ZERO);
            BigDecimal adiantamento = Objects.requireNonNullElse(evento.getValorAdiantamento(), BigDecimal.ZERO);
            BigDecimal custo = Objects.requireNonNullElse(evento.getCusto(), BigDecimal.ZERO);
            String statusPagamento = normalizarStatusPagamento(evento.getStatusPagamento());

            totalCustos = totalCustos.add(custo);

            switch (statusPagamento) {
                case STATUS_PAGO -> {
                    totalPago = totalPago.add(valorTotal);
                    qtdPagos++;
                }
                case STATUS_ADIANTAMENTO -> {
                    totalAdiantamento = totalAdiantamento.add(adiantamento);
                    qtdAdiantamentos++;
                    BigDecimal restante = valorTotal.subtract(adiantamento);
                    if (restante.compareTo(BigDecimal.ZERO) > 0) {
                        totalAReceber = totalAReceber.add(restante);
                        qtdAReceber++;
                    }
                }
                default -> {
                    totalAReceber = totalAReceber.add(valorTotal);
                    qtdAReceber++;
                }
            }
        }

        BigDecimal saldoPrevisto = totalPago.add(totalAdiantamento).add(totalAReceber);
        BigDecimal arrecadadoAtual = totalPago.add(totalAdiantamento);
        BigDecimal lucroLiquido = arrecadadoAtual.subtract(totalCustos);

        BigDecimal margemLucroPercentual = BigDecimal.ZERO;
        if (arrecadadoAtual.compareTo(BigDecimal.ZERO) > 0) {
            margemLucroPercentual = lucroLiquido.multiply(new BigDecimal("100"))
                    .divide(arrecadadoAtual, 2, RoundingMode.HALF_UP);
        }

        return new DadosResumoFinanceiro(
                saldoPrevisto,
                totalPago,
                qtdPagos,
                totalAdiantamento,
                qtdAdiantamentos,
                totalAReceber,
                qtdAReceber,
                totalCustos,
                lucroLiquido,
                margemLucroPercentual
        );
    }

    @Transactional
    public Map<String, Object> compartilharComContato(Long eventoId, DadosCompartilhamentoEvento dados) {
        Evento evento = buscarEventoAtivo(eventoId);
        Usuario usuario = usuarioRepository.findByIdAndAtivoTrue(dados.usuarioId())
                .orElseThrow(EntityNotFoundException::new);

        String tipoInfo = (dados.tipoInformacao() != null && !dados.tipoInformacao().isBlank())
                ? dados.tipoInformacao().trim().toUpperCase()
                : COMPARTILHAMENTO_COMPLETO;

        EventoCompartilhamento registro = compartilhamentoRepository.save(
                new EventoCompartilhamento(evento.getId(), usuario.getId(), tipoInfo)
        );

        return Map.of(
                "id", registro.getId(),
                "mensagem", "Evento compartilhado com " + usuario.getNome() + " com sucesso!",
                "resumo", formatarResumoConvite(evento, tipoInfo)
        );
    }

    private Evento buscarEventoAtivo(Long id) {
        return eventoRepository.findByIdAndAtivoTrue(id)
                .orElseThrow(EntityNotFoundException::new);
    }

    private static String normalizarStatusPagamento(String status) {
        return status != null ? status.trim().toUpperCase() : STATUS_PENDENTE;
    }

    private static String formatarResumoConvite(Evento evento, String tipoInfo) {
        if (COMPARTILHAMENTO_COMPLETO.equals(tipoInfo)) {
            String obs = (evento.getObservacoes() != null && !evento.getObservacoes().isBlank())
                    ? evento.getObservacoes()
                    : "Sem observações";
            return String.format(
                    "Evento: %s (%s) | Data: %s das %s às %s | Local: %s | Valor: R$ %s | Obs: %s",
                    evento.getTitulo(),
                    evento.getTipo(),
                    evento.getDataEvento(),
                    evento.getHoraInicio(),
                    evento.getHoraFim(),
                    evento.getLogradouro(),
                    evento.getValor(),
                    obs
            );
        }
        return String.format(
                "Evento: %s | Data: %s das %s às %s | Local: %s",
                evento.getTitulo(),
                evento.getDataEvento(),
                evento.getHoraInicio(),
                evento.getHoraFim(),
                evento.getLogradouro()
        );
    }
}
