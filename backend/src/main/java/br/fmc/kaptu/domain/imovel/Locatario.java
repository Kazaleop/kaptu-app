package br.fmc.kaptu.domain.imovel;

import br.fmc.kaptu.domain.base.EntidadeBase;
import jakarta.persistence.*;

@Entity
public class Locatario extends EntidadeBase {

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false, unique = true)
    private String cpfCnpj;

    @Column(nullable = false)
    private String telefone;

    @Column(nullable = false)
    private String email;

    protected Locatario() {
    }

    public Locatario(
            String nome,
            String cpfCnpj,
            String telefone,
            String email) {

        this.nome = nome;
        this.cpfCnpj = cpfCnpj;
        this.telefone = telefone;
        this.email = email;
    }

    public String getNome() {
        return nome;
    }

    public String getCpfCnpj() {
        return cpfCnpj;
    }

    public String getTelefone() {
        return telefone;
    }

    public String getEmail() {
        return email;
    }
}