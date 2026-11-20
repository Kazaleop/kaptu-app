package br.fmc.kaptu.domain.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DocumentosTest {

    @Test
    void cpfValido() {
        assertTrue(Documentos.cpfValido("52998224725"));
        assertFalse(Documentos.cpfValido("11111111111"));
        assertFalse(Documentos.cpfValido("52998224726"));
    }

    @Test
    void cnpjValido() {
        assertTrue(Documentos.cnpjValido("11222333000181"));
        assertFalse(Documentos.cnpjValido("11111111111111"));
        assertFalse(Documentos.cnpjValido("11222333000182"));
    }

    @Test
    void telefoneValido() {
        assertTrue(Documentos.telefoneValido("11987654321"));
        assertTrue(Documentos.telefoneValido("1132654321"));
        assertFalse(Documentos.telefoneValido("09987654321"));
        assertFalse(Documentos.telefoneValido("119876543"));
    }
}
