package br.com.sample.rest;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.health.Health;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.HealthCheckResponseBuilder;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@Health
@ApplicationScoped
public class ServiceHealthCheck implements HealthCheck {
    @Inject
    @ConfigProperty(name = "app.name")
    private String app;

    @Override
    public HealthCheckResponse call() {
        HealthCheckResponseBuilder responseBuilder = HealthCheckResponse.named(app);

        responseBuilder.withData("memory", Runtime.getRuntime().freeMemory());
        responseBuilder.withData("availableProcessors", Runtime.getRuntime().availableProcessors());

        return responseBuilder.state(true).build();
    }
}
