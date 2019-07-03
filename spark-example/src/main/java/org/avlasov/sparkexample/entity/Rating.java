package org.avlasov.sparkexample.simple.entity;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

@Data
@Builder
public class Rating implements Serializable {

    private final int userId;
    private final int movieId;
    private final int rating;
    private final long ratingTime;

}
