package br.com.trevezani.rest;

import br.com.trevezani.bean.ChaveBean;
import br.com.trevezani.controller.BPeChaveController;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonObjectBuilder;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Tag(name = "BPeChave", description = "Microserviço responsável por gerar a chave BPe")
@ApplicationScoped
@Path("/chave")
public class BPeChaveEndPoint {
    @Inject
    private BPeChaveController controller;

    @GET
    @Operation(summary = "Gerar chave BPe")
    @APIResponse(description = "Chave BPe")
    @Path("/{uf}/{emissao}/{documento}/{modelo}/{serie}/{tipoEmissao}/{numeroDocumentoFiscal}/{cbp}")
    @Produces({MediaType.APPLICATION_JSON})
    public Response doGetChave(@PathParam("uf") String uf, @PathParam("emissao") String emissao,
                               @PathParam("documento") String documento, @PathParam("modelo") String modelo,
                               @PathParam("serie") String serie, @PathParam("tipoEmissao") String tipoEmissao,
                               @PathParam("numeroDocumentoFiscal") String numeroDocumentoFiscal, @PathParam("cbp") String cbp) {
        String chave = controller.getChaveBPe( uf, emissao, documento, modelo, serie, tipoEmissao, numeroDocumentoFiscal, cbp);

        JsonObjectBuilder json = Json.createObjectBuilder();
        json.add("chbpe", chave);

        return Response.ok(json.build()).build();
    }

    @POST
    @Path("/bean")
    @Operation(summary = "Gerar chave BPe via JSON")
    @APIResponse(description = "Chave BPe")
    @Produces({MediaType.APPLICATION_JSON})
    @Consumes(MediaType.APPLICATION_JSON)
    public Response doGetChaveBean(ChaveBean bean) {
        if (!bean.isValid()) {
            return Response.status(400,"Parâmetros inválidos").build();
        }

        String chave = controller.getChaveBPe( bean.getUf(), bean.getEmissao(), bean.getDocumento(), bean.getModelo(),
                bean.getSerie(), bean.getTipoEmissao(), bean.getNumeroDocumentoFiscal(), bean.getCbp());

        JsonObjectBuilder json = Json.createObjectBuilder();
        json.add("chbpe", chave);

        return Response.ok(json.build()).build();
    }
}
