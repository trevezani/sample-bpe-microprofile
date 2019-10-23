package br.com.sample.enums;

import java.util.Arrays;

public enum Ambiente {
    Producao("1"),
    Homologacao("2");

    String codigo;

    Ambiente(String codigo) {
        this.codigo = codigo;
    }

    public String getCodigo() {
        return this.codigo;
    }

    public static Ambiente getAmbiente(String codigo) {
        Ambiente ambiente = Arrays.stream(Ambiente.values())
                .filter(e -> e.getCodigo().equals(codigo))
                .findAny()
                .orElseThrow(() -> new IllegalStateException(String.format("Unsupported type %s.", codigo)));
        return ambiente;
    }
}
