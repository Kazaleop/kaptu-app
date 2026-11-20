
package br.fmc.kaptu.service;

import br.fmc.kaptu.domain.financeiro.TipoTransacao;
import br.fmc.kaptu.domain.financeiro.TransacaoFinanceira;
import br.fmc.kaptu.repository.TransacaoRepository;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;

import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.Normalizer;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class RelatorioService {

    private static final float MARGEM = 42;
    private static final float LARGURA = PDRectangle.A4.getWidth() - (MARGEM * 2);

    private static final Color VERDE_ESCURO = new Color(25, 75, 58);
    private static final Color VERDE_CLARO = new Color(225, 239, 231);
    private static final Color CINZA = new Color(245, 246, 247);
    private static final Color CINZA_TEXTO = new Color(90, 90, 90);
    private static final Color PRETO = new Color(35, 35, 35);
    private static final Color BRANCO = Color.WHITE;

    private static final DateTimeFormatter FORMATO_DATA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final TransacaoRepository transacaoRepository;

    public RelatorioService(TransacaoRepository transacaoRepository) {
        this.transacaoRepository = transacaoRepository;
    }

    public byte[] gerarRelatorioMpse(LocalDate inicio, LocalDate fim) {

        if (inicio == null || fim == null) {
            throw new IllegalArgumentException(
                    "As datas inicial e final são obrigatórias."
            );
        }

        if (inicio.isAfter(fim)) {
            throw new IllegalArgumentException(
                    "A data inicial não pode ser posterior à data final."
            );
        }

        List<TransacaoFinanceira> transacoes =
                transacaoRepository.findAll()
                        .stream()
                        .filter(t ->
                                !t.getDataTransacao().isBefore(inicio)
                                        && !t.getDataTransacao().isAfter(fim)
                        )
                        .sorted((a, b) ->
                                a.getDataTransacao().compareTo(b.getDataTransacao())
                        )
                        .toList();

        BigDecimal entradas = somar(transacoes, TipoTransacao.ENTRADA);
        BigDecimal saidas = somar(transacoes, TipoTransacao.SAIDA);
        BigDecimal saldo = entradas.subtract(saidas);

        long quantidadeEntradas = contar(transacoes, TipoTransacao.ENTRADA);
        long quantidadeSaidas = contar(transacoes, TipoTransacao.SAIDA);

        long pendentes = transacoes.stream()
                .filter(t -> t.getStatusConciliacao() != null
                        && t.getStatusConciliacao().name().equals("PENDENTE"))
                .count();

        try (PDDocument documento = new PDDocument();
             ByteArrayOutputStream saida = new ByteArrayOutputStream()) {

            LayoutPdf layout = new LayoutPdf(documento);

            desenharCabecalho(layout, inicio, fim);
            desenharResumo(
                    layout,
                    entradas,
                    saidas,
                    saldo,
                    transacoes.size(),
                    pendentes
            );

            desenharSecao(layout, "RESUMO DAS MOVIMENTACOES");

            desenharIndicador(
                    layout,
                    "Entradas",
                    quantidadeEntradas + " movimentacoes",
                    formatarMoeda(entradas),
                    VERDE_CLARO
            );

            desenharIndicador(
                    layout,
                    "Saidas",
                    quantidadeSaidas + " movimentacoes",
                    formatarMoeda(saidas),
                    CINZA
            );

            desenharSecao(layout, "DETALHAMENTO DAS TRANSACOES");

            desenharCabecalhoTabela(layout);

            if (transacoes.isEmpty()) {
                layout.garantirEspaco(35);
                layout.texto(
                        "Nenhuma transacao encontrada no periodo informado.",
                        MARGEM,
                        layout.getY() - 15,
                        9,
                        CINZA_TEXTO
                );
                layout.descer(35);
            } else {
                for (TransacaoFinanceira transacao : transacoes) {
                    if (layout.getY() < 90) {
                        layout.novaPagina();
                        desenharCabecalhoTabela(layout);
                    }

                    desenharLinhaTransacao(layout, transacao);
                }
            }

            desenharRodape(layout);

            layout.fechar();

            documento.save(saida);

            return saida.toByteArray();

        } catch (IOException e) {
            throw new RuntimeException(
                    "Erro ao gerar o relatorio financeiro em PDF.",
                    e
            );
        }
    }

    // Método alternativo para compatibilidade com chamadas anteriores.
    public byte[] gerarRelatorio(LocalDate inicio, LocalDate fim) {
        return gerarRelatorioMpse(inicio, fim);
    }

    private BigDecimal somar(
            List<TransacaoFinanceira> transacoes,
            TipoTransacao tipo
    ) {
        return transacoes.stream()
                .filter(t -> t.getTipoTransacao() == tipo)
                .map(TransacaoFinanceira::getValor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private long contar(
            List<TransacaoFinanceira> transacoes,
            TipoTransacao tipo
    ) {
        return transacoes.stream()
                .filter(t -> t.getTipoTransacao() == tipo)
                .count();
    }

    private void desenharCabecalho(
            LayoutPdf layout,
            LocalDate inicio,
            LocalDate fim
    ) throws IOException {

        float y = layout.getY();

        layout.retangulo(
                MARGEM,
                y - 82,
                LARGURA,
                82,
                VERDE_ESCURO
        );

        layout.texto(
                "KAPTU",
                MARGEM + 18,
                y - 28,
                21,
                BRANCO,
                true
        );

        layout.texto(
                "RELATORIO FINANCEIRO",
                MARGEM + 18,
                y - 49,
                11,
                BRANCO,
                true
        );

        layout.texto(
                "Relatorio de movimentacoes financeiras",
                MARGEM + 18,
                y - 66,
                8,
                BRANCO
        );

        layout.texto(
                "Periodo: " + inicio.format(FORMATO_DATA)
                        + " ate " + fim.format(FORMATO_DATA),
                MARGEM + LARGURA - 210,
                y - 28,
                8,
                BRANCO
        );

        layout.descer(105);
    }

    private void desenharResumo(
            LayoutPdf layout,
            BigDecimal entradas,
            BigDecimal saidas,
            BigDecimal saldo,
            int totalTransacoes,
            long pendentes
    ) throws IOException {

        desenharSecao(layout, "INDICADORES DO PERIODO");

        float espacamento = 10;
        float larguraCard = (LARGURA - (espacamento * 2)) / 3;
        float alturaCard = 72;
        float y = layout.getY();

        desenharCard(
                layout,
                MARGEM,
                y,
                larguraCard,
                alturaCard,
                "ENTRADAS",
                formatarMoeda(entradas),
                VERDE_CLARO
        );

        desenharCard(
                layout,
                MARGEM + larguraCard + espacamento,
                y,
                larguraCard,
                alturaCard,
                "SAIDAS",
                formatarMoeda(saidas),
                CINZA
        );

        desenharCard(
                layout,
                MARGEM + (larguraCard + espacamento) * 2,
                y,
                larguraCard,
                alturaCard,
                "SALDO",
                formatarMoeda(saldo),
                VERDE_CLARO
        );

        layout.descer(alturaCard + 18);

        layout.texto(
                "Total de transacoes: " + totalTransacoes,
                MARGEM,
                layout.getY(),
                9,
                PRETO
        );

        layout.texto(
                "Transacoes pendentes: " + pendentes,
                MARGEM + 210,
                layout.getY(),
                9,
                PRETO
        );

        layout.descer(25);
    }

    private void desenharCard(
            LayoutPdf layout,
            float x,
            float y,
            float largura,
            float altura,
            String titulo,
            String valor,
            Color fundo
    ) throws IOException {

        layout.retangulo(
                x,
                y - altura,
                largura,
                altura,
                fundo
        );

        layout.texto(
                titulo,
                x + 10,
                y - 20,
                8,
                CINZA_TEXTO,
                true
        );

        layout.texto(
                valor,
                x + 10,
                y - 47,
                12,
                VERDE_ESCURO,
                true
        );
    }

    private void desenharSecao(
            LayoutPdf layout,
            String titulo
    ) throws IOException {

        layout.garantirEspaco(35);

        float y = layout.getY();

        layout.texto(
                titulo,
                MARGEM,
                y,
                10,
                VERDE_ESCURO,
                true
        );

        layout.linha(
                MARGEM,
                y - 7,
                MARGEM + LARGURA,
                y - 7,
                VERDE_ESCURO
        );

        layout.descer(22);
    }

    private void desenharIndicador(
            LayoutPdf layout,
            String titulo,
            String quantidade,
            String valor,
            Color fundo
    ) throws IOException {

        layout.garantirEspaco(48);

        float y = layout.getY();

        layout.retangulo(
                MARGEM,
                y - 38,
                LARGURA,
                38,
                fundo
        );

        layout.texto(
                titulo,
                MARGEM + 10,
                y - 15,
                9,
                PRETO,
                true
        );

        layout.texto(
                quantidade,
                MARGEM + 130,
                y - 15,
                8,
                CINZA_TEXTO
        );

        layout.texto(
                valor,
                MARGEM + LARGURA - 115,
                y - 15,
                9,
                VERDE_ESCURO,
                true
        );

        layout.descer(48);
    }

    private void desenharCabecalhoTabela(
            LayoutPdf layout
    ) throws IOException {

        float y = layout.getY();

        layout.retangulo(
                MARGEM,
                y - 25,
                LARGURA,
                25,
                VERDE_ESCURO
        );

        layout.texto("DATA", MARGEM + 7, y - 16, 7, BRANCO, true);
        layout.texto("DESCRICAO", MARGEM + 78, y - 16, 7, BRANCO, true);
        layout.texto("TIPO", MARGEM + 290, y - 16, 7, BRANCO, true);
        layout.texto("VALOR", MARGEM + 365, y - 16, 7, BRANCO, true);
        layout.texto("STATUS", MARGEM + 440, y - 16, 7, BRANCO, true);

        layout.descer(25);
    }

    private void desenharLinhaTransacao(
            LayoutPdf layout,
            TransacaoFinanceira transacao
    ) throws IOException {

        float y = layout.getY();

        String data = transacao.getDataTransacao() == null
                ? "-"
                : transacao.getDataTransacao().format(FORMATO_DATA);

        String descricao = transacao.getDescricao() == null
                ? "-"
                : limitar(transacao.getDescricao(), 34);

        String tipo = transacao.getTipoTransacao() == TipoTransacao.ENTRADA
                ? "Entrada"
                : "Saida";

        String status = transacao.getStatusConciliacao() == null
                ? "-"
                : transacao.getStatusConciliacao().name();

        Color fundo = ((int) (y / 20) % 2 == 0) ? CINZA : BRANCO;

        layout.retangulo(
                MARGEM,
                y - 24,
                LARGURA,
                24,
                fundo
        );

        layout.texto(data, MARGEM + 7, y - 15, 7, PRETO);
        layout.texto(descricao, MARGEM + 78, y - 15, 7, PRETO);
        layout.texto(tipo, MARGEM + 290, y - 15, 7, PRETO);

        layout.texto(
                formatarMoeda(transacao.getValor()),
                MARGEM + 365,
                y - 15,
                7,
                PRETO
        );

        layout.texto(status, MARGEM + 440, y - 15, 7, PRETO);

        layout.descer(24);
    }

    private void desenharRodape(LayoutPdf layout) throws IOException {

        layout.garantirEspaco(35);

        layout.linha(
                MARGEM,
                45,
                MARGEM + LARGURA,
                45,
                CINZA_TEXTO
        );

        layout.texto(
                "Kaptu - Relatorio gerado automaticamente pelo sistema.",
                MARGEM,
                32,
                7,
                CINZA_TEXTO
        );

        layout.texto(
                "Documento para consulta e acompanhamento financeiro.",
                MARGEM + 245,
                32,
                7,
                CINZA_TEXTO
        );
    }

    private String formatarMoeda(BigDecimal valor) {
        if (valor == null) {
            valor = BigDecimal.ZERO;
        }

        return "R$ " + valor.setScale(
                2,
                java.math.RoundingMode.HALF_UP
        ).toString().replace(".", ",");
    }

    private String limitar(String texto, int tamanho) {
        if (texto == null) {
            return "-";
        }

        if (texto.length() <= tamanho) {
            return texto;
        }

        return texto.substring(0, tamanho - 3) + "...";
    }

    private static String normalizar(String texto) {
        if (texto == null) {
            return "";
        }

        String normalizado = Normalizer.normalize(
                texto,
                Normalizer.Form.NFD
        );

        return normalizado
                .replaceAll("\\p{M}", "")
                .replace("–", "-")
                .replace("—", "-")
                .replace("“", "\"")
                .replace("”", "\"")
                .replace("’", "'")
                .replace("º", "o")
                .replace("ª", "a");
    }

    private static class LayoutPdf {

        private final PDDocument documento;
        private PDPage pagina;
        private PDPageContentStream conteudo;
        private float y;

        private LayoutPdf(PDDocument documento) throws IOException {
            this.documento = documento;
            novaPagina();
        }

        private void novaPagina() throws IOException {
            fecharConteudo();

            pagina = new PDPage(PDRectangle.A4);
            documento.addPage(pagina);

            conteudo = new PDPageContentStream(documento, pagina);
            y = PDRectangle.A4.getHeight() - 42;

            texto(
                    "KAPTU - RELATORIO FINANCEIRO",
                    MARGEM,
                    y,
                    8,
                    VERDE_ESCURO,
                    true
            );

            y -= 22;
        }

        private void garantirEspaco(float altura) throws IOException {
            if (y - altura < 65) {
                novaPagina();
            }
        }

        private void texto(
                String texto,
                float x,
                float y,
                float tamanho,
                Color cor
        ) throws IOException {
            texto(texto, x, y, tamanho, cor, false);
        }

        private void texto(
                String texto,
                float x,
                float y,
                float tamanho,
                Color cor,
                boolean negrito
        ) throws IOException {

            PDType1Font fonte = new PDType1Font(
                    negrito
                            ? Standard14Fonts.FontName.HELVETICA_BOLD
                            : Standard14Fonts.FontName.HELVETICA
            );

            conteudo.beginText();
            conteudo.setFont(fonte, tamanho);
            conteudo.setNonStrokingColor(cor);
            conteudo.newLineAtOffset(x, y);
            conteudo.showText(normalizar(texto));
            conteudo.endText();
        }

        private void retangulo(
                float x,
                float y,
                float largura,
                float altura,
                Color cor
        ) throws IOException {

            conteudo.setNonStrokingColor(cor);
            conteudo.addRect(x, y, largura, altura);
            conteudo.fill();
        }

        private void linha(
                float x1,
                float y1,
                float x2,
                float y2,
                Color cor
        ) throws IOException {

            conteudo.setStrokingColor(cor);
            conteudo.setLineWidth(0.6f);
            conteudo.moveTo(x1, y1);
            conteudo.lineTo(x2, y2);
            conteudo.stroke();
        }

        private float getY() {
            return y;
        }

        private void descer(float distancia) {
            y -= distancia;
        }

        private void fecharConteudo() throws IOException {
            if (conteudo != null) {
                conteudo.close();
                conteudo = null;
            }
        }

        private void fechar() throws IOException {
            fecharConteudo();
        }
    }
}
