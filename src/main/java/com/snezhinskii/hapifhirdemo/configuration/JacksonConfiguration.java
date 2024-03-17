package com.snezhinskii.hapifhirdemo.configuration;

import com.fasterxml.jackson.databind.module.SimpleModule;
import com.snezhinskii.hapifhirdemo.api.mapper.ObservationSerializer;
import org.hl7.fhir.r4.model.Observation;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class JacksonConfiguration {

    @Bean
    public SimpleModule observationModule() {
        SimpleModule module = new SimpleModule();
        module.addSerializer(Observation.class, new ObservationSerializer());
        return module;
    }
}
