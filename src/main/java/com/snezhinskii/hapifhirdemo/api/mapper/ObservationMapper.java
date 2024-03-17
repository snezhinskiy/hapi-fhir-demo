package com.snezhinskii.hapifhirdemo.api.mapper;

import org.hl7.fhir.r4.model.Observation;

import java.util.ArrayList;
import java.util.List;

public class ObservationMapper {
    public static List<String> mapEntry(Observation observation) {
        List<String> properties = new ArrayList<>();

        properties.add(observation.getId());
        properties.add(observation.hasCode() ? observation.getCode().getText() : "");

        /**
         * I'm not sure about the mapping of this field, but a quick search turned up nothing.
         * Looks like my solution covers 99% of the results
         */
        if (observation.hasValueCodeableConcept()) {
            properties.add(observation.getValueCodeableConcept().getText());
        } else if (observation.hasValueQuantity()) {
            properties.add(observation.getValueQuantity().getValue() + " "+ observation.getValueQuantity().getCode());
        } else {
            properties.add("");
        }

        if (observation.hasIssued()) {
            properties.add(observation.getIssued().toInstant().toString());
        } else {
            properties.add("");
        }

        return properties;
    }
}
