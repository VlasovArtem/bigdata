package org.avlasov.kafka.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Content {

    private LocalDateTime localDateTime;
    private String device;
    private double value;

    public Content() {
    }

    public Content(double value) {
        this.localDateTime = LocalDateTime.now();
        device = "device_name";
        this.value = value;
    }

}
