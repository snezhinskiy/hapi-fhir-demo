package com.snezhinskii.hapifhirdemo.configuration;

import ca.uhn.fhir.context.FhirContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class HapiFhirConfiguration {

    @Value("${hapi-fhir.socket-timeout:10}")
    private int socketTimeout;

    @Bean
    public FhirContext fhirContext() {
        FhirContext fhirContext = FhirContext.forR4();
        fhirContext.getRestfulClientFactory().setSocketTimeout(socketTimeout * 1000);
        return fhirContext;
    }
}
