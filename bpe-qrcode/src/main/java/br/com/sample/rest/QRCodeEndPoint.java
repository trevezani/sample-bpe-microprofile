package br.com.sample.rest;

import br.com.sample.bean.ChaveBean;
import br.com.sample.bean.QRCodeBean;
import br.com.sample.controller.QRCodeController;
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

    private static final ObjectMapper om = new ObjectMapper();

    @Inject
    @ConfigProperty(name = "app.name")
    private String app;

    @Inject
    @ConfigProperty(name = "bpechave.api.url", defaultValue = "http://bpe-chave:8080/")
    private String bpechaveURL;

    @Inject
    private QRCodeController qrcodeController;

    @POST
    @Counted(monotonic = true, name = "bpeqrcode-count", absolute = true)
    @Timed(name = "bpeqrcode-time", absolute = true)
    @Path("bean")
    @Produces({MediaType.APPLICATION_JSON})
    @Consumes(MediaType.APPLICATION_JSON)
    public Response doGetQRCode(@HeaderParam("x-correlation-id") String correlationId, QRCodeBean bean) {
        final JsonObjectBuilder jsonBuilder = Json.createObjectBuilder();

        if (!bean.isValid()) {
            jsonBuilder.add("correlation-id", correlationId);
            jsonBuilder.add("message", "Parâmetros inválidos");
            jsonBuilder.add("bean", bean.toString());
            jsonBuilder.add("app", app);

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
            url = qrcodeController.getURL(bean.getAmbiente(), bean.getUf());
        }  catch (Exception ex) {
            jsonBuilder.add("correlation-id", correlationId);
            jsonBuilder.add("exception", ex.toString());
            jsonBuilder.add("app", app);

            JsonObject json = jsonBuilder.build();

            logger.error(json.toString(), ex);

            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(json).build();
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

            jsonBuilder.add("correlation-id", correlationId);
            jsonBuilder.add("exception", info);
            jsonBuilder.add("bean", bean.toString());
            jsonBuilder.add("app", app);

            logger.warn(jsonBuilder.build().toString(), ex);

            return "NA";
        }

        try {
            JsonObject obj = qrcodeController.getChaveBean(correlationId, beanJsonString);
            chave = obj.getString("chbpe");

        } catch (ProcessingException ex) {
            jsonBuilder.add("correlation-id", correlationId);
            jsonBuilder.add("message", "Exception trying to get the response from ".concat(bpechaveURL));
            jsonBuilder.add("exception", ex.toString());
            jsonBuilder.add("bean", bean.toString());
            jsonBuilder.add("app", app);

            logger.warn(jsonBuilder.build().toString(), ex);
        }

        return chave;
    }
}
