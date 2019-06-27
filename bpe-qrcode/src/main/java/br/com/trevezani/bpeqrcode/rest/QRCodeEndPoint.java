package br.com.trevezani.bpeqrcode.rest;

import br.com.trevezani.bpeqrcode.controller.BPeQRCodeController;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonObjectBuilder;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

@ApplicationScoped
@Path("/qrcode")
public class QRCodeEndPoint {
//    @Inject
//    private BPeChaveController chaveController;

    @Inject
    private BPeQRCodeController bpeQRCodeController;

    @GET
    @Path("/{ambiente}/{uf}/{emissao}/{documento}/{modelo}/{serie}/{tipoEmissao}/{numeroDocumentoFiscal}/{cbp}")
    @Produces({"application/json"})
    public Response doGetQRCode(@PathParam("ambiente") String ambiente, @PathParam("uf") String uf,
                                @PathParam("emissao") String emissao, @PathParam("documento") String documento,
                                @PathParam("modelo") String modelo, @PathParam("serie") String serie,
                                @PathParam("tipoEmissao") String tipoEmissao, @PathParam("numeroDocumentoFiscal") String numeroDocumentoFiscal,
                                @PathParam("cbp") String cbp) {
        String chave = "ND"; //chaveController.getChaveBPe( uf, emissao, documento, modelo, serie, tipoEmissao, numeroDocumentoFiscal, cbp);
        String url = bpeQRCodeController.getURL(ambiente, uf);

        StringBuilder retorno = new StringBuilder();
        retorno.append(url);
        retorno.append("?").append("chbpe").append("=").append(chave);
        retorno.append("&").append("tpamb").append("=").append(ambiente);

        JsonObjectBuilder json = Json.createObjectBuilder();
        json.add("qrcode", retorno.toString());

        return Response.ok(json.build()).build();
    }
}
