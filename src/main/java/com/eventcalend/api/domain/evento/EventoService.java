package com.eventcalend.api.domain.evento;

import com.eventcalend.api.domain.usuario.Usuario;
import com.eventcalend.api.domain.usuario.UsuarioRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

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
        Evento evento = buscarEventoAtivo(id);
        return new DadosDetalhamentoEvento(evento);
    }

    @Transactional
    public Evento criar(DadosCadastroEvento dados) {
        Evento evento = new Evento(dados);
        return eventoRepository.save(evento);
    }

    @Transactional
    public DadosDetalhamentoEvento atualizar(DadosAtualizacaoEvento dados) {
        Evento evento = buscarEventoAtivo(dados.id());
        evento.atualizarInformacoes(dados);
        return new DadosDetalhamentoEvento(evento);
    }

    @Transactional
    public void excluir(Long id) {
        Evento evento = buscarEventoAtivo(id);
        evento.excluir();
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

        for (Evento evento : eventos) {
            BigDecimal valorTotal = evento.getValor() != null ? evento.getValor() : BigDecimal.ZERO;
            BigDecimal adiantamento = evento.getValorAdiantamento() != null ? evento.getValorAdiantamento() : BigDecimal.ZERO;
            String statusPagamento = normalizarStatusPagamento(evento.getStatusPagamento());

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
                    }
                }
                default -> {
                    totalAReceber = totalAReceber.add(valorTotal);
                    qtdAReceber++;
                }
            }
        }

        BigDecimal saldoPrevisto = totalPago.add(totalAdiantamento).add(totalAReceber);
        return new DadosResumoFinanceiro(
                saldoPrevisto,
                totalPago,
                qtdPagos,
                totalAdiantamento,
                qtdAdiantamentos,
                totalAReceber,
                qtdAReceber
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

        EventoCompartilhamento registro = new EventoCompartilhamento(evento.getId(), usuario.getId(), tipoInfo);
        compartilhamentoRepository.save(registro);

        String resumoConvite = formatarResumoConvite(evento, tipoInfo);
        return Map.of(
                "id", registro.getId(),
                "mensagem", "Evento compartilhado com " + usuario.getNome() + " com sucesso!",
                "resumo", resumoConvite
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
            return String.format(
                    "Evento: %s (%s) | Data: %s das %s às %s | Local: %s | Valor: R$ %s | Obs: %s",
                    evento.getTitulo(),
                    evento.getTipo(),
                    evento.getDataEvento(),
                    evento.getHoraInicio(),
                    evento.getHoraFim(),
                    evento.getLogradouro(),
                    evento.getValor(),
                    evento.getObservacoes() != null && !evento.getObservacoes().isBlank()
                            ? evento.getObservacoes()
                            : "Sem observações"
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
