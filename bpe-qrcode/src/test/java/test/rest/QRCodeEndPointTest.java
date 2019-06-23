package test.rest;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.json.JsonObject;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.IOException;
import java.net.URL;


@RunWith(Arquillian.class)
public class QRCodeEndPointTest {
    @ArquillianResource
    private URL webappUrl;

    WebTarget target;

    ObjectMapper om = new ObjectMapper();

    @Before
    public void before() throws Exception {
        target = ClientBuilder.newClient().target(webappUrl.toURI());
    }

    @Deployment
    public static WebArchive createTestArchive() {
        File[] files = Maven.resolver().loadPomFromFile("pom.xml")
                .importRuntimeDependencies().resolve().withTransitivity().asFile();

        WebArchive war = ShrinkWrap.create(WebArchive.class)
                .addAsLibraries(files)
                .addPackages(true, "br.com.trevezani.bpechave")
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");

        return war;
    }

    @Test
    @RunAsClient
    @InSequence(1)
    public void testQRCode() throws IOException {
        final Response response = target.path("qrcode")
                .path("2")
                .path("23")
                .path("20190621")
                .path("04406541659")
                .path("63")
                .path("001")
                .path("1")
                .path("13")
                .path("123")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get();

        Assert.assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

        JsonObject obj = response.readEntity(JsonObject.class);

        Assert.assertNotNull("QRCode inválido", obj);

        System.out.println("Validando o retorno: ".concat(obj.toString()));

        Assert.assertEquals("QRCode diferente: ".concat(obj.getString("qrcode")),
                "https://dfe-portal.svrs.rs.gov.br/bpe/qrCode?chbpe=23196200004406541659630010000000131000001232&tpamb=2", obj.getString("qrcode"));
    }
}