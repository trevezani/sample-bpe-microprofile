package br.com.trevezani.bpeqrcode.enums;

import java.util.Arrays;

public enum QRCodeURL {
    SVRS("23", "https://dfe-portal.svrs.rs.gov.br/bpe/qrCode", "https://dfe-portal.sefazvirtual.rs.gov.br/bpe/qrcode");

    String uf;
    String homologacao;
    String producao;

    QRCodeURL(String uf, String homologacao, String producao) {
        this.uf = uf;
        this.homologacao = homologacao;
        this.producao = producao;
    }

    public String getUf() {
        return uf;
    }

    public static QRCodeURL getQRCodeURL(String uf) {
        QRCodeURL qr = Arrays.stream(QRCodeURL.values())
                .filter(e -> e.getUf().equals(uf))
                .findAny()
                .orElseThrow(() -> new IllegalStateException(String.format("Unsupported type %s.", uf)));
        return qr;
    }

    public static String getURL(Ambiente ambiente, QRCodeURL qrCodeURL) {
        QRCodeURL qr = Arrays.stream(QRCodeURL.values())
                .filter(e -> e.name().equals(qrCodeURL.name()))
                .findAny()
                .orElseThrow(() -> new IllegalStateException(String.format("Unsupported type %s.", qrCodeURL.name())));

        if (ambiente.equals(Ambiente.Producao)) {
            return qr.producao;
        } else {
            return qr.homologacao;
        }
    }

    public static String getURL(Ambiente ambiente, String uf) {
        QRCodeURL qr = Arrays.stream(QRCodeURL.values())
                .filter(e -> e.getUf().equals(uf))
                .findAny()
                .orElseThrow(() -> new IllegalStateException(String.format("Unsupported type %s.", uf)));

        if (ambiente.equals(Ambiente.Producao)) {
            return qr.producao;
        } else {
            return qr.homologacao;
        }
    }
}
