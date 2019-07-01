package org.avlasov.sparkexample.simple;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.avlasov.sparkexample.simple.entity.Rating;
import org.avlasov.sparkexample.simple.util.DataMapper;
import scala.Tuple2;

import java.util.List;
import java.util.Map;

public class MoviesExample {

    private final DataMapper dataMapper;
    private final String ratingsFileLink;
    private final String moviesFileLink;

    public MoviesExample(DataMapper dataMapper, String ratingsFileLink, String moviesFileLink) {
        this.dataMapper = dataMapper;
        this.ratingsFileLink = ratingsFileLink;
        this.moviesFileLink = moviesFileLink;
    }

    public List<Tuple2<Integer, Integer>> findTop10WorstMovies() {
        try (JavaSparkContext javaSparkContext = new JavaSparkContext(getSparkConfig())) {
            return readRatings(javaSparkContext)
                    .mapToPair(rating -> new Tuple2<>(rating.getMovieId(), new Tuple2<>(rating.getRating(), 1)))
                    .reduceByKey((movieToRating1, movieToRating2) ->
                            new Tuple2<>(movieToRating1._1() + movieToRating2._1(), movieToRating1._2() + movieToRating2._2()))
                    .mapToPair(movieToRatingTotal ->
                            new Tuple2<>(movieToRatingTotal._2()._1() / movieToRatingTotal._2()._2(), movieToRatingTotal._1()))
                    .sortByKey(true)
                    .take(10);
        }
    }

    public Map<Integer, String> readMovies() {
        try (JavaSparkContext javaSparkContext = new JavaSparkContext(getSparkConfig())) {
            return javaSparkContext.textFile(moviesFileLink)
                    .mapToPair(line -> {
                        String[] split = line.split("\\|");
                        return new Tuple2<>(Integer.parseInt(split[0]), split[1]);
                    })
                    .collectAsMap();
        }
    }

    private SparkConf getSparkConfig() {
        return new SparkConf()
                .setMaster("local[*]")
                .setAppName("Movies Example");
    }

    private JavaRDD<Rating> readRatings(JavaSparkContext javaSparkContext) {
        return javaSparkContext.textFile(ratingsFileLink)
                .map(dataMapper.mapToRating());
    }

}
