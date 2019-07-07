package org.avlasov.util;

import org.avlasov.entity.Rating;

import java.io.Serializable;

public class DataMapper implements Serializable {

    public static final String USER_ID_COLUMN_NAME = "userID";
    public static final String MOVIE_ID_COLUMN_NAME = "movieID";
    public static final String RATING_COLUMN_NAME = "rating";

    public Rating mapRating(String line) {
        String[] split = line.split("\t");
        return Rating.builder()
                .userId(Integer.parseInt(split[0]))
                .movieId(Integer.parseInt(split[1]))
                .rating(Integer.parseInt(split[2]))
                .ratingTime(Long.parseLong(split[3]))
                .build();
    }

}
