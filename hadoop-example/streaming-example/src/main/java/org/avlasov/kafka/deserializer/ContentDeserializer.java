package org.avlasov.kafka.deserializer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.kafka.common.serialization.Deserializer;
import org.avlasov.kafka.entity.Content;

import java.io.IOException;

public class ContentDeserializer implements Deserializer<Content> {

    private final ObjectMapper objectMapper;

    public ContentDeserializer() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        objectMapper.configure(SerializationFeature.WRITE_DATE_TIMESTAMPS_AS_NANOSECONDS, false);
    }

    @Override
    public Content deserialize(String topic, byte[] data) {
        try {
            return objectMapper.readValue(data, Content.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


}
