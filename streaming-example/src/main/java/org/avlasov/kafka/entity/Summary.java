package org.avlasov.kafka.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class Summary implements Serializable {

    private double min;
    private double max;
    @JsonIgnore
    private int count;
    @JsonIgnore
    private double sum;
    private double average;
    private final LocalDateTime localDateTime;

}
