package br.com.sample.rest;

import br.com.sample.bean.ChaveBean;
import br.com.sample.controller.ChaveController;
import br.com.sample.util.CorrelationUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.metrics.annotation.Counted;
import org.eclipse.microprofile.metrics.annotation.Timed;
import org.eclipse.microprofile.opentracing.Traced;
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
@Traced
public class ApiBPeChaveEndPoint {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private static final ObjectMapper om = new ObjectMapper();

    @Inject
    private ChaveController chaveController;

    @Inject
    private CorrelationUtils correlationUtils;

    @Inject
    @ConfigProperty(name = "bpechave.api.url", defaultValue = "http://bpe-chave:8080/")
    private String bpechaveURL;

    @Inject
    @ConfigProperty(name = "app.name")
    private String app;

    @POST
    @Counted(monotonic = true, name = "bpeapi-bpechave-count", absolute = true)
    @Timed(name = "bpeapi-bpechave-time", absolute = true)
    @Path("chave")
    @Produces({MediaType.APPLICATION_JSON})
    @Consumes(MediaType.APPLICATION_JSON)
    public Response doGetChaveBean(ChaveBean bean) {
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

        String chave = "NA";
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
            obj = chaveController.getChaveBean(correlationId, beanJsonString);
            chave = obj.getString("chbpe");

        } catch (ProcessingException ex) {
            String info = ex.toString();

            if (ex.getCause() != null) {
                info = ex.getCause().getClass().getSimpleName() + ": " + ex.getCause().getMessage();
            }

            jsonBuilder.add("correlation-id", correlationId);
            jsonBuilder.add("message", "Exception trying to get the response from ".concat(bpechaveURL));
            jsonBuilder.add("exception", info);
            jsonBuilder.add("bean", bean.toString());
            jsonBuilder.add("app", app);

            obj = jsonBuilder.build();

            logger.error(obj.toString(), ex);

            return Response
                    .status(Response.Status.SERVICE_UNAVAILABLE)
                    .entity(obj)
                    .build();
        }

        JsonObjectBuilder json = Json.createObjectBuilder();
        json.add("chbpe", chave);
        json.add("correlation-id", correlationId);

        if (chave.equals("NA")) {
            jsonBuilder.add("app", app);
        }

        return Response.ok(json.build()).build();
    }
}