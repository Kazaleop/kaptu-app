
package br.fmc.kaptu.ocr;

import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Locale;
import javax.imageio.ImageIO;

@Component
public class TesseractExtrator implements ExtratorDeTexto {

    private final String pastaTessdata;

    public TesseractExtrator(
            @Value("${kaptu.ocr.tessdata:./tessdata}")
            String pastaTessdata) {
        this.pastaTessdata = pastaTessdata;
    }

    @Override
    public boolean suporta(Path arquivo) {
        if (arquivo == null || arquivo.getFileName() == null) {
            return false;
        }

        String nome = arquivo.getFileName()
                .toString()
                .toLowerCase(Locale.ROOT);

        return nome.endsWith(".pdf")
                || nome.endsWith(".png")
                || nome.endsWith(".jpg")
                || nome.endsWith(".jpeg")
                || nome.endsWith(".tif")
                || nome.endsWith(".tiff")
                || nome.endsWith(".bmp");
    }

    @Override
    public String extrair(Path arquivo) throws IOException {
        if (!suporta(arquivo)) {
            throw new IllegalArgumentException(
                    "Formato de arquivo não suportado pelo Tesseract."
            );
        }

        Tesseract tesseract = criarTesseract();

        try {
            String nome = arquivo.getFileName()
                    .toString()
                    .toLowerCase(Locale.ROOT);

            if (nome.endsWith(".pdf")) {
                return extrairPdfEscaneado(arquivo, tesseract);
            }

            BufferedImage imagem = ImageIO.read(arquivo.toFile());

            if (imagem == null) {
                throw new IOException(
                        "Não foi possível ler a imagem enviada."
                );
            }

            return tesseract.doOCR(imagem);

        } catch (TesseractException e) {
            throw new IOException(
                    "Erro ao executar OCR no arquivo.",
                    e
            );
        }
    }

    private Tesseract criarTesseract() {
        Tesseract tesseract = new Tesseract();

        tesseract.setDatapath(pastaTessdata);
        tesseract.setLanguage("por+eng");

        return tesseract;
    }

    private String extrairPdfEscaneado(
            Path arquivo,
            Tesseract tesseract) throws IOException, TesseractException {

        StringBuilder texto = new StringBuilder();

        try (PDDocument documento = Loader.loadPDF(arquivo.toFile())) {

            PDFRenderer renderer = new PDFRenderer(documento);

            for (int pagina = 0;
                 pagina < documento.getNumberOfPages();
                 pagina++) {

                BufferedImage imagem = renderer.renderImageWithDPI(
                        pagina,
                        200
                );

                texto.append(tesseract.doOCR(imagem));
                texto.append("\n");
            }
        }

        return texto.toString();
    }
}
