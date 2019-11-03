package br.com.sample.rest;

import br.com.sample.bean.QRCodeBean;
import br.com.sample.controller.QRCodeController;
import br.com.sample.util.CorrelationUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.metrics.annotation.Counted;
import org.eclipse.microprofile.metrics.annotation.Timed;
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

@ApplicationScoped
@Path("/")
public class ApiBPeQRCodeEndPoint {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private static final ObjectMapper om = new ObjectMapper();

    @Inject
    private QRCodeController qrcodeController;

    @Inject
    private CorrelationUtils correlationUtils;

    @Inject
    @ConfigProperty(name = "bpeqrcode.api.url", defaultValue = "http://bpe-qrcode:8080/")
    private String bpeqrcodeURL;

    @Inject
    @ConfigProperty(name = "app.name")
    private String app;

    @POST
    @Counted(monotonic = true, name = "bpeapi-bpeqrcode-count", absolute = true)
    @Timed(name = "bpeapi-bpeqrcode-time", absolute = true)
    @Path("qrcode")
    @Produces({MediaType.APPLICATION_JSON})
    @Consumes(MediaType.APPLICATION_JSON)
    public Response doGetQRCode(QRCodeBean bean) {
        String correlationId = correlationUtils.getCorrelationId();

        final JsonObjectBuilder jsonBuilder = Json.createObjectBuilder();

        JsonObject obj = null;

        if (!bean.isValid()) {
            jsonBuilder.add("correlation-id", correlationId);
            jsonBuilder.add("message", "Parâmetros inválidos");
            jsonBuilder.add("bean", bean.toString());
            jsonBuilder.add("app", app);

            obj = jsonBuilder.build();

            logger.error(obj.toString());

            return Response.status(Response.Status.BAD_REQUEST).entity(obj).build();
        }

        String beanJsonString = null;

        try {
            beanJsonString = om.writeValueAsString(bean);
        } catch (Exception e) {
            jsonBuilder.add("correlation-id", correlationId);
            jsonBuilder.add("exception", e.toString());
            jsonBuilder.add("bean", bean.toString());
            jsonBuilder.add("app", app);

            obj = jsonBuilder.build();

            logger.error(obj.toString());

            return Response.status(Response.Status.BAD_REQUEST).entity(obj).build();
        }

        try {
            obj = qrcodeController.getQRCodeBean(correlationId, beanJsonString);

            obj.forEach(jsonBuilder::add);
            jsonBuilder.add("correlation-id", correlationId);
            obj = jsonBuilder.build();

        } catch (ProcessingException ex) {
            String info = ex.toString();

            if (ex.getCause() != null) {
                info = ex.getCause().getClass().getSimpleName() + ": " + ex.getCause().getMessage();
            }

            jsonBuilder.add("correlation-id", correlationId);
            jsonBuilder.add("message", "Exception trying to get the response from ".concat(bpeqrcodeURL));
            jsonBuilder.add("exception", info);
            jsonBuilder.add("bean", bean.toString());
            jsonBuilder.add("app", app);

            obj = jsonBuilder.build();

            logger.warn(obj.toString(), ex);

            return Response
                    .status(Response.Status.SERVICE_UNAVAILABLE)
                    .entity(obj)
                    .build();
        }

        return Response.ok(obj).build();
    }
}