package br.com.trevezani.rest;

import br.com.trevezani.bean.ChaveBean;
import br.com.trevezani.util.CorrelationUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.faulttolerance.CircuitBreaker;
import org.eclipse.microprofile.faulttolerance.Fallback;
import org.eclipse.microprofile.faulttolerance.Timeout;
import org.eclipse.microprofile.metrics.annotation.Counted;
import org.eclipse.microprofile.metrics.annotation.Timed;
import org.eclipse.microprofile.opentracing.ClientTracingRegistrar;
import org.eclipse.microprofile.opentracing.Traced;
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
@Traced
public class ApiBPeChaveEndPoint {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private static final String servicename = "bpe-api";
    private static final ObjectMapper om = new ObjectMapper();

    @Inject
    private CorrelationUtils correlationUtils;

    @Inject
    @ConfigProperty(name = "bpechave.api.url", defaultValue = "http://bpe-chave:8080/")
    private String bpechaveURL;

    @POST
    @Counted(monotonic = true, name = "bpeapi-bpechave-count", absolute = true)
    @Timed(name = "bpeapi-bpechave-time", absolute = true)
    @Path("chave")
    @Produces({MediaType.APPLICATION_JSON})
    @Consumes(MediaType.APPLICATION_JSON)
    public Response doGetChaveBean(ChaveBean bean) {
        String correlationId = correlationUtils.getCorrelationId();

        final JsonObjectBuilder jsonBuilder = Json.createObjectBuilder();

        if (!bean.isValid()) {
            jsonBuilder.add("app", servicename);
            jsonBuilder.add("correlation-id", correlationId);
            jsonBuilder.add("info", "Parâmetros inválidos");
            jsonBuilder.add("bean", bean.toString());

            logger.error(jsonBuilder.toString());

            return Response.status(Response.Status.BAD_REQUEST).entity(jsonBuilder.build()).build();
        }

        String chave = "NA";
        String beanJsonString = null;

        try {
            beanJsonString = om.writeValueAsString(bean);
        } catch (Exception e) {
            jsonBuilder.add("app", servicename);
            jsonBuilder.add("correlation-id", correlationId);
            jsonBuilder.add("exception", e.toString());
            jsonBuilder.add("bean", bean.toString());

            logger.error(jsonBuilder.toString());

            return Response.status(Response.Status.BAD_REQUEST).entity(jsonBuilder.build()).build();
        }

        try {
            JsonObject obj = getChaveBean(correlationId, beanJsonString);
            chave = obj.getString("chbpe");

        } catch (ProcessingException ex) {
            String info = ex.toString();

            if (ex.getCause() != null) {
                info = ex.getCause().getClass().getSimpleName() + ": " + ex.getCause().getMessage();
            }

            jsonBuilder.add("app", servicename);
            jsonBuilder.add("correlation-id", correlationId);
            jsonBuilder.add("info", "Exception trying to get the response from bpe-chave service");
            jsonBuilder.add("exception", info);
            jsonBuilder.add("bean", bean.toString());

            logger.warn(jsonBuilder.toString(), ex);

            return Response
                    .status(Response.Status.SERVICE_UNAVAILABLE)
                    .entity(jsonBuilder.build())
                    .build();
        }

        JsonObjectBuilder json = Json.createObjectBuilder();
        json.add("chbpe", chave);
        json.add("correlation-id", correlationId);

        if (chave.equals("NA")) {
            json.add("url.bpechave", bpechaveURL);
        }

        return Response.ok(json.build()).build();
    }

    @Timeout(200)
    @CircuitBreaker
    @Fallback(fallbackMethod = "getChaveBeanFallBack")
    private JsonObject getChaveBean(final String correlationId, final String beanJsonString) {
        logger.info(String.format("[%s] Calling %s JSON %s", correlationId, bpechaveURL, beanJsonString));

        Client client = ClientTracingRegistrar.configure(ClientBuilder.newBuilder()).build();

        final Response response = client.target(bpechaveURL)
                .path("chave")
                .path("bean")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .header("x-correlation-id", correlationUtils.getCorrelationId())
                .post(Entity.json(beanJsonString));

        return response.readEntity(JsonObject.class);
    }

    private JsonObject getChaveBeanFallBack(final String correlationId, final String beanJsonString) {
        JsonObjectBuilder json = Json.createObjectBuilder();
        json.add("correlation-id", correlationId);
        json.add("chbpe", "NA");

        return json.build();
    }
}