package br.com.sample.controller;

import br.com.sample.enums.Ambiente;
import br.com.sample.enums.QRCodeURL;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class BPeQRCodeController {

    public String getURL(final String ambienteId, final String ufId) {
        Ambiente ambiente = Ambiente.getAmbiente(ambienteId);
        String url = QRCodeURL.getURL(ambiente, ufId);

        return url;
    }

}
