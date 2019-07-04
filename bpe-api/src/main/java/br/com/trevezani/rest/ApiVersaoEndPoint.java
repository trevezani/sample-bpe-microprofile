package br.com.trevezani.rest;

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
    @Path("versao")
    @Produces({MediaType.APPLICATION_JSON})
    public Response doGetversao() {
        JsonObjectBuilder json = Json.createObjectBuilder();
        json.add("versao", "1.00");

        return Response.ok(json.build()).build();
    }

}