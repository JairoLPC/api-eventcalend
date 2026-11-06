package com.eventcalend.api.infra.ai;

import com.eventcalend.api.domain.evento.DadosResultadoExtracao;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class GeminiService {

    @Value("${gemini.api.key:}")
    private String apiKey;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RestTemplate restTemplate = new RestTemplate();

    public DadosResultadoExtracao extrairDadosEvento(String textoBruto) {
        if (apiKey != null && !apiKey.trim().isEmpty()) {
            try {
                DadosResultadoExtracao resultadoAi = chamarGeminiApi(textoBruto);
                if (resultadoAi != null) {
                    return resultadoAi;
                }
            } catch (Exception e) {
                System.err.println("Falha na chamada Gemini API, utilizando extração fallback: " + e.getMessage());
            }
        }
        return extrairFallbackRegex(textoBruto);
    }

    private DadosResultadoExtracao chamarGeminiApi(String textoBruto) throws Exception {
        String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=" + apiKey.trim();

        String prompt = """
            Você é um assistente especialista em extrair dados de eventos para um aplicativo de agenda.
            Analise o seguinte texto enviado pelo usuário (geralmente copiado do WhatsApp) e retorne estritamente um objeto JSON com as seguintes chaves (sem Markdown, sem ```json, apenas JSON puro):

            {
              "titulo": "Nome curto e claro para o evento",
              "tipo": "Casamento" (ou "Aniversário", "Corporativo", "Editorial", "Show / Concerto", "Formatura"),
              "dataEvento": "YYYY-MM-DD" (se ano omitido, assuma 2026),
              "horaInicio": "HH:mm" (24h),
              "horaFim": "HH:mm" (24h),
              "logradouro": "Endereço ou local do evento",
              "valor": 0.00 (valor total numérico),
              "statusPagamento": "PENDENTE" ou "ADIANTAMENTO" ou "PAGO",
              "valorAdiantamento": 0.00 (valor adiantado numérico),
              "observacoes": "Resumo das observações adicionais"
            }

            Texto do usuário:
            \"""%s\"""
            """.formatted(textoBruto);

        Map<String, Object> contents = Map.of("parts", List.of(Map.of("text", prompt)));
        Map<String, Object> body = Map.of("contents", List.of(contents));

        String jsonResponse = restTemplate.postForObject(url, body, String.class);
        if (jsonResponse == null) return null;

        JsonNode rootNode = objectMapper.readTree(jsonResponse);
        JsonNode candidates = rootNode.path("candidates");
        if (candidates.isArray() && candidates.size() > 0) {
            String contentText = candidates.get(0).path("content").path("parts").get(0).path("text").asText();
            String cleanJson = contentText.replaceAll("```json", "").replaceAll("```", "").trim();

            JsonNode jsonResult = objectMapper.readTree(cleanJson);
            return new DadosResultadoExtracao(
                jsonResult.path("titulo").asText("Evento Importado"),
                jsonResult.path("tipo").asText("Casamento"),
                jsonResult.path("dataEvento").asText(LocalDate.now().toString()),
                jsonResult.path("horaInicio").asText("16:00"),
                jsonResult.path("horaFim").asText("22:00"),
                jsonResult.path("logradouro").asText(""),
                new BigDecimal(jsonResult.path("valor").asText("0.00")),
                jsonResult.path("statusPagamento").asText("PENDENTE").toUpperCase(),
                new BigDecimal(jsonResult.path("valorAdiantamento").asText("0.00")),
                jsonResult.path("observacoes").asText("")
            );
        }
        return null;
    }

    private DadosResultadoExtracao extrairFallbackRegex(String texto) {
        String titulo = "Evento Importado";
        String tipo = "Casamento";
        String dataEvento = LocalDate.now().toString();
        String horaInicio = "16:00";
        String horaFim = "22:00";
        String logradouro = "";
        BigDecimal valor = BigDecimal.ZERO;
        String statusPagamento = "PENDENTE";
        BigDecimal valorAdiantamento = BigDecimal.ZERO;

        String t = texto.toLowerCase();
        if (t.contains("aniversario") || t.contains("aniversário") || t.contains("niver")) tipo = "Aniversário";
        else if (t.contains("corporat") || t.contains("empresa") || t.contains("reuniao")) tipo = "Corporativo";
        else if (t.contains("editorial") || t.contains("ensaio")) tipo = "Editorial";
        else if (t.contains("show") || t.contains("concerto") || t.contains("banda")) tipo = "Show / Concerto";
        else if (t.contains("formatura") || t.contains("baile")) tipo = "Formatura";
        else if (t.contains("casamento") || t.contains("noiva")) tipo = "Casamento";

        // Busca data DD/MM/AAAA ou DD/MM
        Pattern pData = Pattern.compile("(\\d{1,2})/(\\d{1,2})(?:/(\\d{2,4}))?");
        Matcher mData = pData.matcher(texto);
        if (mData.find()) {
            int dia = Integer.parseInt(mData.group(1));
            int mes = Integer.parseInt(mData.group(2));
            int ano = mData.group(3) != null ? Integer.parseInt(mData.group(3)) : 2026;
            if (ano < 100) ano += 2000;
            dataEvento = String.format("%04d-%02d-%02d", ano, mes, dia);
        }

        // Busca valor R$ 1.200,00 ou 1200
        Pattern pValor = Pattern.compile("(?:r\\$\\s*|valor\\s*:?\\s*)(\\d+(?:[.,]\\d{2})?)", Pattern.CASE_INSENSITIVE);
        Matcher mValor = pValor.matcher(texto);
        if (mValor.find()) {
            try {
                String valStr = mValor.group(1).replace(",", ".");
                valor = new BigDecimal(valStr);
            } catch (Exception ignored) {}
        }

        // Busca adiantamento
        Pattern pAdiant = Pattern.compile("(?:adiantamento|sinal|entrada)\\s*:?\\s*(?:r\\$\\s*)?(\\d+(?:[.,]\\d{2})?)", Pattern.CASE_INSENSITIVE);
        Matcher mAdiant = pAdiant.matcher(texto);
        if (mAdiant.find()) {
            try {
                String adStr = mAdiant.group(1).replace(",", ".");
                valorAdiantamento = new BigDecimal(adStr);
                if (valorAdiantamento.compareTo(BigDecimal.ZERO) > 0) {
                    statusPagamento = "ADIANTAMENTO";
                }
            } catch (Exception ignored) {}
        }

        return new DadosResultadoExtracao(
            titulo,
            tipo,
            dataEvento,
            horaInicio,
            horaFim,
            logradouro,
            valor,
            statusPagamento,
            valorAdiantamento,
            texto
        );
    }
}
