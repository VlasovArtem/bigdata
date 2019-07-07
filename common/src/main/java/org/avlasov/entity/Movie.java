package org.avlasov.entity;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Movie {
    private int movieID;
    private String name;
}
