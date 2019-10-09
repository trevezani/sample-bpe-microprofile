package br.com.trevezani.rest;

import br.com.trevezani.bean.ChaveBean;
import br.com.trevezani.bean.QRCodeBean;
import br.com.trevezani.controller.BPeQRCodeController;
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
@Path("qrcode")
@Traced
public class QRCodeEndPoint {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private static final String servicename = "bpe-qrcode";
    private static final ObjectMapper om = new ObjectMapper();

    @Inject
    @ConfigProperty(name = "bpechave.api.url", defaultValue = "http://bpe-chave:8080/")
    private String bpechaveURL;

    @Inject
    private BPeQRCodeController bpeQRCodeController;

    @POST
    @Counted(monotonic = true, name = "bpeqrcode-count", absolute = true)
    @Timed(name = "bpeqrcode-time", absolute = true)
    @Path("bean")
    @Produces({MediaType.APPLICATION_JSON})
    @Consumes(MediaType.APPLICATION_JSON)
    public Response doGetQRCode(@HeaderParam("x-correlation-id") String correlationId, QRCodeBean bean) {
        final JsonObjectBuilder jsonBuilder = Json.createObjectBuilder();

        if (!bean.isValid()) {
            jsonBuilder.add("app", servicename);
            jsonBuilder.add("correlation-id", correlationId);
            jsonBuilder.add("message", "Parâmetros inválidos");
            jsonBuilder.add("bean", bean.toString());

            logger.error(jsonBuilder.build().toString());

            return Response.status(Response.Status.BAD_REQUEST).entity(jsonBuilder.build()).build();
        }

        ChaveBean chaveBean = new ChaveBean();
        chaveBean.setUf(bean.getUf());
        chaveBean.setEmissao(bean.getEmissao());
        chaveBean.setDocumento(bean.getDocumento());
        chaveBean.setModelo(bean.getModelo());
        chaveBean.setSerie(bean.getSerie());
        chaveBean.setTipoEmissao(bean.getTipoEmissao());
        chaveBean.setNumeroDocumentoFiscal(bean.getNumeroDocumentoFiscal());
        chaveBean.setCbp(bean.getCbp());

        String chave = getChaveBean(correlationId, chaveBean);
        String url = null;

        try {
            url = bpeQRCodeController.getURL(bean.getAmbiente(), bean.getUf());
        }  catch (Exception ex) {
            jsonBuilder.add("app", servicename);
            jsonBuilder.add("correlation-id", correlationId);
            jsonBuilder.add("exception", ex.toString());

            logger.error(jsonBuilder.build().toString(), ex);

            return Response.serverError().entity(jsonBuilder.build()).build();
        }

        StringBuilder retorno = new StringBuilder();
        retorno.append(url);
        retorno.append("?").append("chbpe").append("=").append(chave);
        retorno.append("&").append("tpamb").append("=").append(bean.getAmbiente());

        JsonObjectBuilder json = Json.createObjectBuilder();
        json.add("qrcode", retorno.toString());

        return Response.ok(json.build()).build();
    }

    public String getChaveBean(final String correlationId, ChaveBean bean) {
        final JsonObjectBuilder jsonBuilder = Json.createObjectBuilder();

        String chave = "NA";
        String beanJsonString = null;

        try {
            beanJsonString = om.writeValueAsString(bean);
        } catch (Exception ex) {
            String info = ex.toString();

            if (ex.getCause() != null) {
                info = ex.getCause().getClass().getSimpleName() + ": " + ex.getCause().getMessage();
            }

            jsonBuilder.add("app", servicename);
            jsonBuilder.add("correlation-id", correlationId);
            jsonBuilder.add("exception", info);
            jsonBuilder.add("bean", bean.toString());

            logger.warn(jsonBuilder.build().toString(), ex);

            return "NA";
        }

        try {
            JsonObject obj = getChaveBean(correlationId, beanJsonString);
            chave = obj.getString("chbpe");

        } catch (ProcessingException ex) {
            jsonBuilder.add("app", servicename);
            jsonBuilder.add("correlation-id", correlationId);
            jsonBuilder.add("message", "Exception trying to get the response from bpe-chave service");
            jsonBuilder.add("exception", ex.toString());
            jsonBuilder.add("bean", bean.toString());

            logger.warn(jsonBuilder.build().toString(), ex);
        }

        return chave;
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
                .request(MediaType.APPLICATION_JSON)
                .header("x-correlation-id", correlationId)
                .post(Entity.json(beanJsonString));

        return response.readEntity(JsonObject.class);
    }

    private JsonObject getChaveBeanFallBack(final String correlationId, final String beanJsonString) {
        JsonObjectBuilder json = Json.createObjectBuilder();
        json.add("chbpe", "NA");
        json.add("correlation-id", correlationId);

        return json.build();
    }
}
