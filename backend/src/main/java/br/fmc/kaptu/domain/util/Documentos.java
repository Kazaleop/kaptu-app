package br.fmc.kaptu.domain.util;

public final class Documentos {

    private Documentos() {
    }

    public static String somenteDigitos(String valor) {
        if (valor == null) {
            return null;
        }

        return valor.replaceAll("\\D", "");
    }

    public static boolean cpfValido(String cpf) {
        String digitos = somenteDigitos(cpf);

        if (digitos == null || digitos.length() != 11) {
            return false;
        }

        if (digitos.chars().distinct().count() == 1) {
            return false;
        }

        int soma = 0;

        for (int i = 0; i < 9; i++) {
            soma += (digitos.charAt(i) - '0') * (10 - i);
        }

        int resto = soma % 11;
        int digito1 = resto < 2 ? 0 : 11 - resto;

        if (digito1 != (digitos.charAt(9) - '0')) {
            return false;
        }

        soma = 0;

        for (int i = 0; i < 10; i++) {
            soma += (digitos.charAt(i) - '0') * (11 - i);
        }

        resto = soma % 11;
        int digito2 = resto < 2 ? 0 : 11 - resto;

        return digito2 == (digitos.charAt(10) - '0');
    }

    public static boolean cnpjValido(String cnpj) {
        String digitos = somenteDigitos(cnpj);

        if (digitos == null || digitos.length() != 14) {
            return false;
        }

        if (digitos.chars().distinct().count() == 1) {
            return false;
        }

        int[] pesos1 = { 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2 };
        int[] pesos2 = { 6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2 };

        int soma = 0;

        for (int i = 0; i < 12; i++) {
            soma += (digitos.charAt(i) - '0') * pesos1[i];
        }

        int resto = soma % 11;
        int digito1 = resto < 2 ? 0 : 11 - resto;

        if (digito1 != (digitos.charAt(12) - '0')) {
            return false;
        }

        soma = 0;

        for (int i = 0; i < 13; i++) {
            soma += (digitos.charAt(i) - '0') * pesos2[i];
        }

        resto = soma % 11;
        int digito2 = resto < 2 ? 0 : 11 - resto;

        return digito2 == (digitos.charAt(13) - '0');
    }

    public static boolean telefoneValido(String telefone) {
        String digitos = somenteDigitos(telefone);

        if (digitos == null
                || (digitos.length() != 10
                && digitos.length() != 11)) {
            return false;
        }

        int ddd = Integer.parseInt(digitos.substring(0, 2));

        if (ddd < 11 || ddd > 99) {
            return false;
        }

        if (digitos.length() == 11
                && digitos.charAt(2) != '9') {
            return false;
        }

        return true;
    }
}
