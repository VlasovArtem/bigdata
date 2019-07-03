package org.avlasov.sparkexample.util;

import org.apache.spark.api.java.function.Function;
import org.avlasov.sparkexample.entity.Movie;
import org.avlasov.sparkexample.entity.Rating;

public class DataMapper {

    public static final String USER_ID_COLUMN_NAME = "userID";
    public static final String MOVIE_ID_COLUMN_NAME = "movieID";
    public static final String RATING_COLUMN_NAME = "rating";

    public Function<String, Rating> mapToRating() {
        return line -> {
            String[] split = line.split("\t");
            return Rating.builder()
                    .userId(Integer.parseInt(split[0]))
                    .movieId(Integer.parseInt(split[1]))
                    .rating(Integer.parseInt(split[2]))
                    .ratingTime(Long.parseLong(split[3]))
                    .build();
        };
    }

    public Function<String, Movie> mapToMovie() {
        return line -> {
            String[] split = line.split("|");
            return Movie.builder()
                    .movieID(Integer.parseInt(split[0]))
                    .name(split[1])
                    .build();
        };
    }

}
