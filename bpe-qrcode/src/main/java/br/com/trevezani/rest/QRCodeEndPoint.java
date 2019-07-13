package br.com.trevezani.rest;

import br.com.trevezani.bean.ChaveBean;
import br.com.trevezani.bean.QRCodeBean;
import br.com.trevezani.controller.BPeQRCodeController;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.faulttolerance.CircuitBreaker;
import org.eclipse.microprofile.faulttolerance.Fallback;
import org.eclipse.microprofile.faulttolerance.Timeout;
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

    private static final String RESPONSE_STRING_FORMAT = "bpe-qrcode => %s\n";
    private static final ObjectMapper om = new ObjectMapper();

    @Inject
    @ConfigProperty(name = "bpechave.api.url", defaultValue = "http://bpe-chave:8080/")
    private String bpechaveURL;

    @Inject
    private BPeQRCodeController bpeQRCodeController;

    @POST
    @Path("bean")
    @Produces({MediaType.APPLICATION_JSON})
    @Consumes(MediaType.APPLICATION_JSON)
    public Response doGetChaveBean(QRCodeBean bean) {
        if (!bean.isValid()) {
            return Response.status(400,"Parâmetros inválidos").build();
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

        String chave = getChaveBean(chaveBean);
        String url = bpeQRCodeController.getURL(bean.getAmbiente(), bean.getUf());

        StringBuilder retorno = new StringBuilder();
        retorno.append(url);
        retorno.append("?").append("chbpe").append("=").append(chave);
        retorno.append("&").append("tpamb").append("=").append(bean.getAmbiente());

        JsonObjectBuilder json = Json.createObjectBuilder();
        json.add("qrcode", retorno.toString());

        return Response.ok(json.build()).build();
    }

    public String getChaveBean(ChaveBean bean) {
        String chave = "NA";
        String beanJsonString = null;

        try {
            beanJsonString = om.writeValueAsString(bean);
        } catch (Exception ex) {
            logger.warn(ex.getCause().getClass().getSimpleName() + ": " + ex.getCause().getMessage(), ex);
            return "NA";
        }

        try {
            JsonObject obj = getChaveBean(beanJsonString);
            chave = obj.getString("chbpe");

        } catch (ProcessingException ex) {
            logger.warn("Exception trying to get the response from bpe-chave service.", ex);
        }

        return chave;
    }

    @Timeout(200)
    @CircuitBreaker
    @Fallback(fallbackMethod = "getChaveBeanFallBack")
    private JsonObject getChaveBean(final String beanJsonString) {
        Client client = ClientTracingRegistrar.configure(ClientBuilder.newBuilder()).build();

        final Response response = client.target(bpechaveURL)
                .path("chave")
                .path("bean")
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.json(beanJsonString));

        return response.readEntity(JsonObject.class);
    }

    private JsonObject getChaveBeanFallBack(final String beanJsonString) {
        JsonObjectBuilder json = Json.createObjectBuilder();
        json.add("chbpe", "NA");

        return json.build();
    }
}
