package org.avlasov.sparkexample.main;

import org.apache.spark.api.java.JavaSparkContext;
import org.avlasov.sparkexample.util.DataMapper;
import scala.Tuple2;

import java.util.Map;

public abstract class AbstractExample {

    protected final String movieRatingsFilePath;
    protected final String moviesFilePath;
    protected final DataMapper dataMapper;

    protected AbstractExample(String movieRatingsFilePath, String moviesFilePath, DataMapper dataMapper) {
        this.movieRatingsFilePath = movieRatingsFilePath;
        this.moviesFilePath = moviesFilePath;
        this.dataMapper = dataMapper;
    }

    public abstract JavaSparkContext getJavaSparkContext();

    public Map<Integer, String> readMovies() {
        try (JavaSparkContext javaSparkContext = getJavaSparkContext()) {
            return javaSparkContext.textFile(moviesFilePath)
                    .mapToPair(line -> {
                        String[] split = line.split("\\|");
                        return new Tuple2<>(Integer.parseInt(split[0]), split[1]);
                    })
                    .collectAsMap();
        }
    }

}
