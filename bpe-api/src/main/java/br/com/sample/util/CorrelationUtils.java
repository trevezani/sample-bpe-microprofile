package br.com.sample.util;

import javax.enterprise.context.ApplicationScoped;
import java.util.UUID;

@ApplicationScoped
public class CorrelationUtils {

    public String getCorrelationId() {
        return UUID.randomUUID().toString();
    }

}
