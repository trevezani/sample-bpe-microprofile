package br.com.sample.controller;

import br.com.sample.enums.Ambiente;
import br.com.sample.enums.QRCodeURL;
import io.opentracing.Tracer;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.faulttolerance.Bulkhead;
import org.eclipse.microprofile.faulttolerance.Fallback;
import org.eclipse.microprofile.faulttolerance.Retry;
import org.eclipse.microprofile.faulttolerance.Timeout;
import org.eclipse.microprofile.metrics.annotation.Counted;
import org.eclipse.microprofile.opentracing.ClientTracingRegistrar;
import org.eclipse.microprofile.opentracing.Traced;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@ApplicationScoped
@Traced(value = true, operationName = "QRCodeController")
public class QRCodeController {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Inject
    @ConfigProperty(name = "app.name")
    private String app;

    @Inject
    @ConfigProperty(name = "bpechave.api.url", defaultValue = "http://bpe-chave:8080/")
    private String bpechaveURL;

    @Inject
    private Tracer tracer;

    public String getURL(final String ambienteId, final String ufId) {
        Ambiente ambiente = Ambiente.getAmbiente(ambienteId);
        String url = QRCodeURL.getURL(ambiente, ufId);

        return url;
    }

    @Counted
    @Timeout(300)
    @Bulkhead(value = 2, waitingTaskQueue = 10)
    @Fallback(fallbackMethod = "getChaveBeanFallBack")
    public JsonObject getChaveBean(final String correlationId, final String beanJsonString) {
        final JsonObjectBuilder jsonBuilder = Json.createObjectBuilder();
        jsonBuilder.add("correlation-id", correlationId);
        jsonBuilder.add("message", String.format("Calling %s", bpechaveURL));
        jsonBuilder.add("bean", beanJsonString);

        JsonObject json = jsonBuilder.build();

        tracer.activeSpan().log(json.toString());

        Client client = ClientTracingRegistrar.configure(ClientBuilder.newBuilder()).build();

        final Response response = client.target(bpechaveURL)
                .path("chave")
                .path("bean")
                .request(MediaType.APPLICATION_JSON)
                .header("x-correlation-id", correlationId)
                .post(Entity.json(beanJsonString));

        return response.readEntity(JsonObject.class);
    }

    public JsonObject getChaveBeanFallBack(final String correlationId, final String beanJsonString) {
        JsonObjectBuilder jsonBuilder = Json.createObjectBuilder();
        jsonBuilder.add("correlation-id", correlationId);
        jsonBuilder.add("chbpe", "NA");
        jsonBuilder.add("app", app);

        JsonObject json = jsonBuilder.build();

        logger.info(json.toString());

        return json;
    }
}
