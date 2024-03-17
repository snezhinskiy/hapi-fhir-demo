package com.snezhinskii.hapifhirdemo.service;

import ca.uhn.fhir.rest.api.SearchTotalModeEnum;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import com.snezhinskii.hapifhirdemo.utils.GenericClientFactory;
import lombok.RequiredArgsConstructor;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Observation;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PatientObservationsBundleService {
    private final GenericClientFactory clientFactory;
    public Bundle loadFirstPageBundleWithTotal(String patientId, Integer pageSize) {
        return spawnClient().search()
            .forResource(Observation.class)
            .where(Observation.SUBJECT.hasId(patientId))
            .totalMode(SearchTotalModeEnum.ACCURATE)
            .count(pageSize)
            .returnBundle(Bundle.class)
            .execute();
    }

    public Bundle loadPageBundle(String patientId, Integer pageSize, Integer offset) {
        return spawnClient().search()
            .forResource(Observation.class)
            .where(Observation.SUBJECT.hasId(patientId))
            .totalMode(SearchTotalModeEnum.NONE)
            .count(pageSize)
            .offset(offset)
            .returnBundle(Bundle.class)
            .execute();
    }

    private IGenericClient spawnClient() {
        /**
         * @See https://hapifhir.io/hapi-fhir/docs/client/generic_client.html
         * Client instances, on the other hand, are very inexpensive to create, so
         * you can create a new one for each request if needed (although there is no
         * requirement to do so, clients are reusable and thread-safe as well)
         */
        return clientFactory.createClient();
    }
}
