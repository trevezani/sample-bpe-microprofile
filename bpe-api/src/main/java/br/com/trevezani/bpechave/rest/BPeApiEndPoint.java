package br.com.trevezani.bpechave.rest;

import br.com.trevezani.bpechave.bean.ChaveBean;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.faulttolerance.Fallback;
import org.eclipse.microprofile.faulttolerance.Retry;
import org.eclipse.microprofile.faulttolerance.Timeout;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
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
    @ConfigProperty(name = "BPE_CHAVE_URL", defaultValue = "localhost")
    private String urlBPeChave;

    @POST
    @Path("/chave")
    @Produces({MediaType.APPLICATION_JSON})
    @Consumes(MediaType.APPLICATION_JSON)
    public Response doGetChaveBean(ChaveBean bean) {
        if (!bean.isValid()) {
            return Response.status(400,"Parâmetros inválidos").build();
        }

        String chave = null;

        try {
            chave = getChaveService(bean);

        } catch (Exception e) {
            return Response.serverError().entity(e.toString()).build();
        }

        JsonObjectBuilder json = Json.createObjectBuilder();
        json.add("chbpe", chave);

        return Response.ok(json.build()).build();
    }

    @Timeout(500)
    @Retry(maxRetries = 3)
    @Fallback(fallbackMethod= "fallbackForChaveService")
    private String getChaveService(ChaveBean bean) throws JsonProcessingException {
        String beanJsonString = om.writeValueAsString(bean);

        Client client = ClientBuilder.newClient();

        final Response response = client.target("http://" + urlBPeChave + ":8082")
                .path("chave")
                .path("bean")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .post(Entity.json(beanJsonString));

        JsonObject obj = response.readEntity(JsonObject.class);

        String chave = "NA";

        if (obj != null) {
            chave = obj.getString("chbpe");
        }

        return chave;
    }

    private String fallbackForChaveService() {
        return "NA";
    }
}
