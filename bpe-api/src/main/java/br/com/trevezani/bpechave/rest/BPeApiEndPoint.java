package br.com.trevezani.bpechave.rest;

import br.com.trevezani.bpechave.bean.ChaveBean;
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

@ApplicationScoped
@Path("/")
public class BPeApiEndPoint {

    @POST
    @Path("/chave")
    @Produces({MediaType.APPLICATION_JSON})
    @Consumes(MediaType.APPLICATION_JSON)
    public Response doGetChaveBean(ChaveBean bean) {
        if (!bean.isValid()) {
            return Response.status(400,"Parâmetros inválidos").build();
        }

        String chave = "NA";

        JsonObjectBuilder json = Json.createObjectBuilder();
        json.add("chbpe", chave);

        return Response.ok(json.build()).build();
    }

}
