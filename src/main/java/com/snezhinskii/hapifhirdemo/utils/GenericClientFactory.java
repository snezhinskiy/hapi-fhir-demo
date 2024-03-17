package com.snezhinskii.hapifhirdemo.utils;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GenericClientFactory {
    private final FhirContext fhirContext;

    @Value("${hapi-fhir.server}")
    private String serverUrl;

    public IGenericClient createClient() {
        return fhirContext.newRestfulGenericClient(serverUrl);
    }
}
