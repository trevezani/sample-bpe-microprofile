package br.com.trevezani.bpechave.rest;

import org.eclipse.microprofile.health.Health;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.HealthCheckResponseBuilder;

import javax.enterprise.context.ApplicationScoped;

@Health
@ApplicationScoped
public class ServiceHealthCheck implements HealthCheck {
    @Override
    public HealthCheckResponse call() {
        HealthCheckResponseBuilder responseBuilder = HealthCheckResponse.named(ServiceHealthCheck.class.getSimpleName());

        responseBuilder.withData("memory", Runtime.getRuntime().freeMemory());
        responseBuilder.withData("availableProcessors", Runtime.getRuntime().availableProcessors());

        return responseBuilder.state(true).build();
    }
}
