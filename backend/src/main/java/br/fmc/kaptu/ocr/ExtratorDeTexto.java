
package br.fmc.kaptu.ocr;

import java.io.IOException;
import java.nio.file.Path;

public interface ExtratorDeTexto {

    String extrair(Path arquivo) throws IOException;

    boolean suporta(Path arquivo);
}
