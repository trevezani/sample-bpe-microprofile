package br.com.trevezani.bpechave.rest;

import br.com.trevezani.bpechave.bean.ChaveBean;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.faulttolerance.CircuitBreaker;
import org.eclipse.microprofile.faulttolerance.Fallback;
import org.eclipse.microprofile.faulttolerance.Timeout;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.ws.rs.*;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@ApplicationScoped
@Path("/")
public class BPeApiEndPoint {
    private ObjectMapper om = new ObjectMapper();

    @Inject
    @ConfigProperty(name = "url.bpechave", defaultValue = "localhost")
    private String urlBPeChave;

    @GET
    @Path("versao")
    @Produces({MediaType.APPLICATION_JSON})
    public Response doGetversao() {
        JsonObjectBuilder json = Json.createObjectBuilder();
        json.add("versao", "1.00");

        return Response.ok(json.build()).build();
    }


    @POST
    @Path("chave")
    @Produces({MediaType.APPLICATION_JSON})
    @Consumes(MediaType.APPLICATION_JSON)
    public Response doGetChaveBean(ChaveBean bean) {
        if (!bean.isValid()) {
            return Response.status(400,"Parâmetros inválidos").build();
        }

        String chave = "NA";
        String error = null;

        try {
            chave = getChaveService(bean);

        } catch (Exception e) {
            error = e.toString();
        }

        JsonObjectBuilder json = Json.createObjectBuilder();
        json.add("chbpe", chave);

        if (chave.equals("NA")) {
            json.add("url.bpechave", urlBPeChave);
        }

        if (error != null) {
            json.add("error", error);
        }

        return Response.ok(json.build()).build();
    }

    private String getChaveService(ChaveBean bean) throws Exception {
        JsonObject obj = getChaveBean(om.writeValueAsString(bean));
        return obj.getString("chbpe");
    }

    @Timeout(200)
    @CircuitBreaker
    @Fallback(fallbackMethod = "getChaveBeanFallBack")
    private JsonObject getChaveBean(final String beanJsonString) {
        Client client = ClientBuilder.newClient();

        try {
            final Response response = client.target("http://" + urlBPeChave + ":8082")
                    .path("chave")
                    .path("bean")
                    .request(MediaType.APPLICATION_JSON_TYPE)
                    .post(Entity.json(beanJsonString));

            return response.readEntity(JsonObject.class);
        } catch (Exception e) {
            return getChaveBeanFallBack(beanJsonString);
        }
    }

    private JsonObject getChaveBeanFallBack(final String beanJsonString) {
        JsonObjectBuilder json = Json.createObjectBuilder();
        json.add("chbpe", "NA");

        return json.build();
    }
}
