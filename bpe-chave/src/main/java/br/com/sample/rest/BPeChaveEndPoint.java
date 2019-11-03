package br.com.sample.rest;

import br.com.sample.bean.ChaveBean;
import br.com.sample.controller.ChaveController;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.metrics.annotation.Counted;
import org.eclipse.microprofile.metrics.annotation.Timed;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.opentracing.Traced;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Tag(name = "BPeChave", description = "Microserviço responsável por gerar a chave BPe")
@ApplicationScoped
@Path("chave")
@Traced
public class BPeChaveEndPoint {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Inject
    @ConfigProperty(name = "app.name")
    private String app;

    @Inject
    private ChaveController chaveController;

    @GET
    @Counted(monotonic = true, name = "bpechave-count", absolute = true)
    @Timed(name = "bpechave-time", absolute = true)
    @Operation(summary = "Gerar chave BPe")
    @APIResponse(description = "Chave BPe")
    @Path("/{uf}/{emissao}/{documento}/{modelo}/{serie}/{tipoEmissao}/{numeroDocumentoFiscal}/{cbp}")
    @Produces({MediaType.APPLICATION_JSON})
    public Response doGetChave(@PathParam("uf") String uf, @PathParam("emissao") String emissao,
                               @PathParam("documento") String documento, @PathParam("modelo") String modelo,
                               @PathParam("serie") String serie, @PathParam("tipoEmissao") String tipoEmissao,
                               @PathParam("numeroDocumentoFiscal") String numeroDocumentoFiscal, @PathParam("cbp") String cbp) {
        String chave = chaveController.getChaveBPe( uf, emissao, documento, modelo, serie, tipoEmissao, numeroDocumentoFiscal, cbp);

        JsonObjectBuilder json = Json.createObjectBuilder();
        json.add("chbpe", chave);

        return Response.ok(json.build()).build();
    }

    @POST
    @Counted(monotonic = true, name = "bpechave-count", absolute = true)
    @Timed(name = "bpechave-time", absolute = true)
    @Path("bean")
    @Operation(summary = "Gerar chave BPe via JSON")
    @APIResponse(description = "Chave BPe")
    @Produces({MediaType.APPLICATION_JSON})
    @Consumes(MediaType.APPLICATION_JSON)
    public Response doGetChaveBean(@HeaderParam("x-correlation-id") String correlationId, ChaveBean bean) {
        final JsonObjectBuilder jsonBuilder = Json.createObjectBuilder();

        if (!bean.isValid()) {
            jsonBuilder.add("correlation-id", correlationId);
            jsonBuilder.add("message", "Parâmetros inválidos");
            jsonBuilder.add("bean", bean.toString());
            jsonBuilder.add("app", app);

            JsonObject json = jsonBuilder.build();

            logger.error(json.toString());

            return Response.status(Response.Status.BAD_REQUEST).entity(json).build();
        }

        String chave = chaveController.getChaveBPe( bean.getUf(), bean.getEmissao(), bean.getDocumento(), bean.getModelo(),
                bean.getSerie(), bean.getTipoEmissao(), bean.getNumeroDocumentoFiscal(), bean.getCbp());

        JsonObjectBuilder json = Json.createObjectBuilder();
        json.add("chbpe", chave);

        final JsonObjectBuilder jsonBuilderLog = Json.createObjectBuilder();
        jsonBuilderLog.add("correlation-id", correlationId);
        jsonBuilderLog.add("message", String.format("Chave gerada [%s]", chave));
        jsonBuilderLog.add("app", app);

        logger.info(jsonBuilderLog.build().toString());

        return Response.ok(json.build()).build();
    }
}
