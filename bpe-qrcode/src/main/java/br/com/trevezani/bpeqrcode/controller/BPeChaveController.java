package br.com.trevezani.bpeqrcode.controller;

import br.com.trevezani.bpeqrcode.util.Modulo11;
import br.com.trevezani.bpeqrcode.util.StringUtils;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import java.util.Random;

@ApplicationScoped
public class BPeChaveController {
    @Inject
    private StringUtils stringUtils;

    @Inject
    private Modulo11 modulo11;

    public String getChaveBPe(@NotNull String uf, @NotNull String emissao, @NotNull String documento,
                              @NotNull String modelo, @NotNull String serie, @NotNull String tipoEmissao,
                              @NotNull String numeroDocumentoFiscal, String cbp) {
        if (cbp == null) {
            cbp = String.valueOf(new Random().nextInt(99999999));
        }

        // YYYYMMDD
        String ano = emissao.substring(2, 4);
        String mes = emissao.substring(5, 7);

        StringBuilder chave = new StringBuilder();
        chave.append(uf);
        chave.append(ano);
        chave.append(mes);
        chave.append(stringUtils.lpadTo(documento.replaceAll("\\D", ""), 14, '0'));
        chave.append(modelo);
        chave.append(stringUtils.lpadTo(serie, 3, '0'));
        chave.append(stringUtils.lpadTo(String.valueOf(numeroDocumentoFiscal), 9, '0'));
        chave.append(stringUtils.lpadTo(tipoEmissao, 1, '0'));
        chave.append(stringUtils.lpadTo(cbp, 8, '0'));

        String mod = String.valueOf(modulo11.modulo11(chave.toString()));

        chave.append(mod);

        return chave.toString();
    }

}
