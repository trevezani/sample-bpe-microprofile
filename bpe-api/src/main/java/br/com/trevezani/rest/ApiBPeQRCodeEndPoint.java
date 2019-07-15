package br.com.trevezani.rest;

import br.com.trevezani.bean.QRCodeBean;
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
public class ApiBPeQRCodeEndPoint {
    private static final String RESPONSE_STRING_FORMAT = "base-api => %s\n";
    private static final ObjectMapper om = new ObjectMapper();

    private final Logger logger = LoggerFactory.getLogger(getClass());

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
        if (!bean.isValid()) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Parâmetros inválidos").build();
        }

        String beanJsonString = null;

        try {
            beanJsonString = om.writeValueAsString(bean);
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.toString()).build();
        }

        JsonObject obj = null;

        try {
            obj = getQRCodeBean(beanJsonString);

        } catch (ProcessingException ex) {
            logger.warn("Exception trying to get the response from bpe-qrcode service.", ex);

            String info = ex.toString();

            if (ex.getCause() != null) {
                info = ex.getCause().getClass().getSimpleName() + ": " + ex.getCause().getMessage();
            }

            return Response
                    .status(Response.Status.SERVICE_UNAVAILABLE)
                    .entity(String.format(RESPONSE_STRING_FORMAT, info))
                    .build();
        }

        return Response.ok(obj).build();
    }

    @Timeout(200)
    @CircuitBreaker
    @Fallback(fallbackMethod = "getQRCodeBeanFallBack")
    private JsonObject getQRCodeBean(final String beanJsonString) {
        Client client = ClientTracingRegistrar.configure(ClientBuilder.newBuilder()).build();

        final Response response = client.target(bpeqrcodeURL)
                .path("qrcode")
                .path("bean")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .post(Entity.json(beanJsonString));

        return response.readEntity(JsonObject.class);
    }

    private JsonObject getQRCodeBeanFallBack(final String beanJsonString) {
        JsonObjectBuilder json = Json.createObjectBuilder();
        json.add("qrcode", "NA");

        return json.build();
    }
}