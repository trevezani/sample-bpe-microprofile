package br.com.trevezani.rest;

import org.eclipse.microprofile.metrics.annotation.Counted;
import org.eclipse.microprofile.metrics.annotation.Timed;

import javax.enterprise.context.ApplicationScoped;
import javax.json.Json;
import javax.json.JsonObjectBuilder;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@ApplicationScoped
@Path("/")
public class ApiVersaoEndPoint {

    @GET
    @Counted(monotonic = true, name = "bpeapi-versao-count", absolute = true)
    @Timed(name = "bpeapi-versao-time", absolute = true)
    @Path("versao")
    @Produces({MediaType.APPLICATION_JSON})
    public Response doGetversao() {
        JsonObjectBuilder json = Json.createObjectBuilder();
        json.add("versao", "1.0.1");

        return Response.ok(json.build()).build();
    }

}