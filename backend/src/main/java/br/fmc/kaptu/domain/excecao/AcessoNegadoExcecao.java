package br.fmc.kaptu.domain.excecao;

public class AcessoNegadoExcecao extends RuntimeException {

    public AcessoNegadoExcecao(String mensagem) {
        super(mensagem);
    }
}
