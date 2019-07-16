package org.avlasov.sparkexample.streaming.entity;

import lombok.Data;

@Data
public class LogRequest {

    private final String httpMethod;
    private final String url;
    private final String end;

}
