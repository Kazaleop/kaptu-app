package br.fmc.kaptu.domain.excecao;

public class RegraDeNegocioExcecao extends RuntimeException {

    public RegraDeNegocioExcecao(String mensagem) {
        super(mensagem);
    }
}