package br.com.trevezani.rest;

import br.com.trevezani.bean.QRCodeBean;
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
import org.wildfly.swarm.spi.runtime.annotations.ConfigurationValue;

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
public class ApiBPeQRCodeEndPoint {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private static final String servicename = "bpe-api";
    private static final ObjectMapper om = new ObjectMapper();

    @Inject
    private CorrelationUtils correlationUtils;

    @Inject
    @ConfigProperty(name = "bpeqrcode.api.url", defaultValue = "http://bpe-qrcode:8080/")
    private String bpeqrcodeURL;

    @POST
    @Counted(monotonic = true, name = "bpeapi-bpeqrcode-count", absolute = true)
    @Timed(name = "bpeapi-bpe-time", absolute = true)
    @Path("qrcode")
    @Produces({MediaType.APPLICATION_JSON})
    @Consumes(MediaType.APPLICATION_JSON)
    public Response doGetQRCode(QRCodeBean bean) {
        String correlationId = correlationUtils.getCorrelationId();

        final JsonObjectBuilder jsonBuilder = Json.createObjectBuilder();

        JsonObject obj = null;

        if (!bean.isValid()) {
            jsonBuilder.add("app", servicename);
            jsonBuilder.add("correlation-id", correlationId);
            jsonBuilder.add("message", "Parâmetros inválidos");
            jsonBuilder.add("bean", bean.toString());

            obj = jsonBuilder.build();

            logger.error(obj.toString());

            return Response.status(Response.Status.BAD_REQUEST).entity(obj).build();
        }

        String beanJsonString = null;

        try {
            beanJsonString = om.writeValueAsString(bean);
        } catch (Exception e) {
            jsonBuilder.add("app", servicename);
            jsonBuilder.add("correlation-id", correlationId);
            jsonBuilder.add("exception", e.toString());
            jsonBuilder.add("bean", bean.toString());

            obj = jsonBuilder.build();

            logger.error(obj.toString());

            return Response.status(Response.Status.BAD_REQUEST).entity(obj).build();
        }

        try {
            obj = getQRCodeBean(correlationId, beanJsonString);

            obj.forEach(jsonBuilder::add);
            jsonBuilder.add("correlation-id", correlationId);
            obj = jsonBuilder.build();

        } catch (ProcessingException ex) {
            String info = ex.toString();

            if (ex.getCause() != null) {
                info = ex.getCause().getClass().getSimpleName() + ": " + ex.getCause().getMessage();
            }

            jsonBuilder.add("app", servicename);
            jsonBuilder.add("correlation-id", correlationId);
            jsonBuilder.add("message", "Exception trying to get the response from bpe-qrcode service");
            jsonBuilder.add("exception", info);
            jsonBuilder.add("bean", bean.toString());

            obj = jsonBuilder.build();

            logger.warn(obj.toString(), ex);

            return Response
                    .status(Response.Status.SERVICE_UNAVAILABLE)
                    .entity(obj)
                    .build();
        }

        return Response.ok(obj).build();
    }

    @Timeout(200)
    @CircuitBreaker
    @Fallback(fallbackMethod = "getQRCodeBeanFallBack")
    private JsonObject getQRCodeBean(final String correlationId, final String beanJsonString) {
        final JsonObjectBuilder jsonBuilder = Json.createObjectBuilder();
        jsonBuilder.add("app", servicename);
        jsonBuilder.add("correlation-id", correlationId);
        jsonBuilder.add("message", String.format("Calling %s", bpeqrcodeURL));
        jsonBuilder.add("bean", beanJsonString);

        logger.info(jsonBuilder.build().toString());

        Client client = ClientTracingRegistrar.configure(ClientBuilder.newBuilder()).build();

        final Response response = client.target(bpeqrcodeURL)
                .path("qrcode")
                .path("bean")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .header("x-correlation-id", correlationId)
                .post(Entity.json(beanJsonString));

        return response.readEntity(JsonObject.class);
    }

    private JsonObject getQRCodeBeanFallBack(final String correlationId, final String beanJsonString) {
        JsonObjectBuilder json = Json.createObjectBuilder();
        json.add("correlation-id", correlationId);
        json.add("qrcode", "NA");

        return json.build();
    }
}