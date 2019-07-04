package br.com.trevezani.rest;

import br.com.trevezani.bean.ChaveBean;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.faulttolerance.CircuitBreaker;
import org.eclipse.microprofile.faulttolerance.Fallback;
import org.eclipse.microprofile.faulttolerance.Timeout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
public class ApiBPeQRCodeEndPoint {
    private static final String RESPONSE_STRING_FORMAT = "base-api => %s\n";
    private static final ObjectMapper om = new ObjectMapper();

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Inject
    @ConfigProperty(name = "bpeqrcode.api.url", defaultValue = "http://bpe-qrcode:8080/")
    private String bpeqrcodeURL;

    @POST
    @Path("qrcode")
    @Produces({MediaType.APPLICATION_JSON})
    @Consumes(MediaType.APPLICATION_JSON)
    public Response doGetQRCode(ChaveBean bean) {
        if (!bean.isValid()) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Parâmetros inválidos").build();
        }

        String chave = "NA";
        String beanJsonString = null;

        try {
            beanJsonString = om.writeValueAsString(bean);
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.toString()).build();
        }

        try {
            JsonObject obj = getChaveBean(beanJsonString);
            chave = obj.getString("chbpe");

        } catch (ProcessingException ex) {
            logger.warn("Exception trying to get the response from bpe-chave service.", ex);

            return Response
                    .status(Response.Status.SERVICE_UNAVAILABLE)
                    .entity(String.format(RESPONSE_STRING_FORMAT, ex.getCause().getClass().getSimpleName() + ": " + ex.getCause().getMessage()))
                    .build();
        }

        JsonObjectBuilder json = Json.createObjectBuilder();
        json.add("chbpe", chave);

        if (chave.equals("NA")) {
            json.add("url.bpechave", bpeqrcodeURL);
        }

        return Response.ok(json.build()).build();
    }

    @Timeout(200)
    @CircuitBreaker
    @Fallback(fallbackMethod = "getChaveBeanFallBack")
    private JsonObject getChaveBean(final String beanJsonString) {
        Client client = ClientBuilder.newClient();

        final Response response = client.target(bpeqrcodeURL)
                .path("chave")
                .path("bean")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .post(Entity.json(beanJsonString));

        return response.readEntity(JsonObject.class);
    }

    private JsonObject getChaveBeanFallBack(final String beanJsonString) {
        JsonObjectBuilder json = Json.createObjectBuilder();
        json.add("chbpe", "NA");

        return json.build();
    }
}