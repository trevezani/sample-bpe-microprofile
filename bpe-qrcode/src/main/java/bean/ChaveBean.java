package bean;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class ChaveBean {
    private String uf;
    private String emissao;
    private String documento;
    private String modelo;
    private String serie;
    private String tipoEmissao;
    private String numeroDocumentoFiscal;
    private String cbp;

    public ChaveBean() {

    }

    public String getUf() {
        return uf;
    }

    public void setUf(String uf) {
        this.uf = uf;
    }

    public String getEmissao() {
        return emissao;
    }

    public void setEmissao(String emissao) {
        this.emissao = emissao;
    }

    public String getDocumento() {
        return documento;
    }

    public void setDocumento(String documento) {
        this.documento = documento;
    }

    public String getModelo() {
        return modelo;
    }

    public void setModelo(String modelo) {
        this.modelo = modelo;
    }

    public String getSerie() {
        return serie;
    }

    public void setSerie(String serie) {
        this.serie = serie;
    }

    public String getTipoEmissao() {
        return tipoEmissao;
    }

    public void setTipoEmissao(String tipoEmissao) {
        this.tipoEmissao = tipoEmissao;
    }

    public String getNumeroDocumentoFiscal() {
        return numeroDocumentoFiscal;
    }

    public void setNumeroDocumentoFiscal(String numeroDocumentoFiscal) {
        this.numeroDocumentoFiscal = numeroDocumentoFiscal;
    }

    public String getCbp() {
        return cbp;
    }

    public void setCbp(String cbp) {
        this.cbp = cbp;
    }

    @JsonIgnore
    public boolean isValid() {
        return (uf != null
                && emissao != null
                && documento != null
                && modelo != null
                && serie != null
                && tipoEmissao != null
                && numeroDocumentoFiscal != null);
    }

    @Override
    public String toString() {
        return "ChaveBean{" +
                "uf='" + uf + '\'' +
                ", emissao='" + emissao + '\'' +
                ", documento='" + documento + '\'' +
                ", modelo='" + modelo + '\'' +
                ", serie='" + serie + '\'' +
                ", tipoEmissao='" + tipoEmissao + '\'' +
                ", numeroDocumentoFiscal='" + numeroDocumentoFiscal + '\'' +
                ", cbp='" + cbp + '\'' +
                '}';
    }
}
