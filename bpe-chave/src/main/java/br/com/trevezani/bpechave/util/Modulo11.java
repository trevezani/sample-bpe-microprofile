package br.com.trevezani.bpechave.util;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class Modulo11 {

    public int modulo11(String chave) {
        int total = 0;
        int peso = 2;

        for (int i = 0; i < chave.length(); i++) {
            total += (chave.charAt((chave.length() - 1) - i) - '0') * peso;
            peso++;
            if (peso == 10)
                peso = 2;
        }

        int resto = total % 11;

        return (resto == 0 || resto == 1) ? 0 : (11 - resto);
    }

}
