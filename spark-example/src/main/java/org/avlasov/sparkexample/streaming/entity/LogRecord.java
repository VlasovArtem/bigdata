package org.avlasov.sparkexample.streaming.entity;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

@Data
@Builder
public class LogRecord implements Serializable {

    private final String host;
    private final String user;
    private final String time;
    private final LogRequest request;
    private final int status;
    private final String size;
    private final String refer;
    private final String agent;

}
