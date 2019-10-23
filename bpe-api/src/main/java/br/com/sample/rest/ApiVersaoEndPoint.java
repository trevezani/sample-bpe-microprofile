package br.com.sample.rest;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.metrics.annotation.Counted;
import org.eclipse.microprofile.metrics.annotation.Timed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonObjectBuilder;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.InputStream;
import java.util.Properties;

@ApplicationScoped
@Path("/")
public class ApiVersaoEndPoint {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Inject
    @ConfigProperty(name = "ambiente", defaultValue = "2")
    private String ambiente;

    @GET
    @Counted(monotonic = true, name = "bpeapi-versao-count", absolute = true)
    @Timed(name = "bpeapi-versao-time", absolute = true)
    @Path("versao")
    @Produces({MediaType.APPLICATION_JSON})
    public Response doGetversao() {
        JsonObjectBuilder json = Json.createObjectBuilder();
        json.add("versao", getVersion());
        json.add("ambiente", ambiente);

        return Response.ok(json.build()).build();
    }

    public synchronized String getVersion() {
        String version = null;

        try {
            Properties p = new Properties();
            InputStream is = getClass().getResourceAsStream("/META-INF/maven/br.com.trevezani/bpe-api/pom.properties");
            if (is != null) {
                p.load(is);
                version = p.getProperty("version", "");
            }
        } catch (Exception e) {
        }

        // fallback to using Java API
        if (version == null) {
            Package aPackage = getClass().getPackage();
            if (aPackage != null) {
                version = aPackage.getImplementationVersion();
                if (version == null) {
                    version = aPackage.getSpecificationVersion();
                }
            }
        }

        if (version == null) {
            version = "1DF";
        }

        return version;
    }
}
