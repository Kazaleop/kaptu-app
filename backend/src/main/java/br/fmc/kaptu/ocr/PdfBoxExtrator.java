
package br.fmc.kaptu.ocr;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Locale;

@Component
public class PdfBoxExtrator implements ExtratorDeTexto {

    @Override
    public boolean suporta(Path arquivo) {
        if (arquivo == null || arquivo.getFileName() == null) {
            return false;
        }

        return arquivo.getFileName()
                .toString()
                .toLowerCase(Locale.ROOT)
                .endsWith(".pdf");
    }

    @Override
    public String extrair(Path arquivo) throws IOException {
        if (!suporta(arquivo)) {
            throw new IllegalArgumentException(
                    "O arquivo informado não é um PDF."
            );
        }

        try (PDDocument documento = Loader.loadPDF(arquivo.toFile())) {
            PDFTextStripper leitor = new PDFTextStripper();

            return leitor.getText(documento);
        }
    }
}
