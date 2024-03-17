package com.snezhinskii.hapifhirdemo.api.mapper;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.hl7.fhir.r4.model.Observation;

import java.io.IOException;
import java.util.List;

public class ObservationSerializer extends JsonSerializer<Observation> {

    @Override
    public void serialize(Observation observation, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        List<String> properties = ObservationMapper.mapEntry(observation);
        jsonGenerator.writeStartObject();
        jsonGenerator.writeStringField("Id", properties.get(0));
        jsonGenerator.writeStringField("Code", properties.get(1));
        jsonGenerator.writeStringField("Value", properties.get(2));
        jsonGenerator.writeStringField("Date", properties.get(3));
        jsonGenerator.writeEndObject();
    }
}
