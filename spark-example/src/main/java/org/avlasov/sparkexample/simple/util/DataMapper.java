package org.avlasov.sparkexample.simple.util;

import org.apache.spark.api.java.function.Function;
import org.avlasov.sparkexample.simple.entity.Movie;
import org.avlasov.sparkexample.simple.entity.Rating;

public class DataMapper {

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
