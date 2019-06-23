package br.com.trevezani.bpeqrcode.rest;

import br.com.trevezani.bpeqrcode.controller.BPeChaveController;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

@ApplicationScoped
@Path("/chave")
public class BPeChaveEndPoint {
    @Inject
    private BPeChaveController controller;

    @GET
    @Path("/{uf}/{emissao}/{documento}/{modelo}/{serie}/{tipoEmissao}/{numeroDocumentoFiscal}/{cbp}")
    @Produces({"application/json"})
    public Response doGetChave(@PathParam("uf") String uf, @PathParam("emissao") String emissao,
                               @PathParam("documento") String documento, @PathParam("modelo") String modelo,
                               @PathParam("serie") String serie, @PathParam("tipoEmissao") String tipoEmissao,
                               @PathParam("numeroDocumentoFiscal") String numeroDocumentoFiscal, @PathParam("cbp") String cbp) {
        return Response.ok(controller.getChaveBPe( uf, emissao, documento, modelo, serie, tipoEmissao, numeroDocumentoFiscal, cbp)).build();
    }
}
