package org.avlasov.sparkexample.simple;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.avlasov.sparkexample.main.AbstractExample;
import org.avlasov.sparkexample.entity.Rating;
import org.avlasov.sparkexample.util.DataMapper;
import scala.Tuple2;

import java.util.List;

public class MoviesExample extends AbstractExample {

    protected MoviesExample(String movieRatingsFilePath, String moviesFilePath, DataMapper dataMapper) {
        super(movieRatingsFilePath, moviesFilePath, dataMapper);
    }

    public List<Tuple2<Double, Integer>> findTop10WorstMovies() {
        try (JavaSparkContext javaSparkContext = new JavaSparkContext(getSparkConfig())) {
            return readRatings(javaSparkContext)
                    .mapToPair(rating -> new Tuple2<>(rating.getMovieId(), new Tuple2<>(rating.getRating(), 1)))
                    .reduceByKey((movieToRating1, movieToRating2) ->
                            new Tuple2<>(movieToRating1._1() + movieToRating2._1(), movieToRating1._2() + movieToRating2._2()))
                    .mapToPair(movieToRatingTotal ->
                            new Tuple2<>(movieToRatingTotal._2()._1() / (double) movieToRatingTotal._2()._2(), movieToRatingTotal._1()))
                    .sortByKey(true)
                    .take(10);
        }
    }

    public List<Tuple2<Double, Tuple2<Integer, Integer>>> findTop10WorstMoviesSecondSolution() {
        try (JavaSparkContext javaSparkContext = new JavaSparkContext(getSparkConfig())) {
            return readRatings(javaSparkContext)
                    .mapToPair(rating -> new Tuple2<>(rating.getMovieId(), new Tuple2<>(rating.getRating(), 1)))
                    .reduceByKey((movieToRating1, movieToRating2) ->
                            new Tuple2<>(movieToRating1._1() + movieToRating2._1(), movieToRating1._2() + movieToRating2._2()))
                    .filter(movieRating -> movieRating._2()._2() > 10)
                    .mapToPair(movieToRatingTotal ->
                            new Tuple2<>(movieToRatingTotal._2()._1() / (double) movieToRatingTotal._2()._2(), new Tuple2<>(movieToRatingTotal._1(), movieToRatingTotal._2()._2())))
                    .sortByKey(true)
                    .take(10);
        }
    }

    @Override
    public JavaSparkContext getJavaSparkContext() {
        return new JavaSparkContext(getSparkConfig());
    }

    private SparkConf getSparkConfig() {
        return new SparkConf()
                .setMaster("local[*]")
                .setAppName("Movies Example");
    }

    private JavaRDD<Rating> readRatings(JavaSparkContext javaSparkContext) {
        return javaSparkContext.textFile(movieRatingsFilePath)
                .map(dataMapper.mapToRating());
    }

}
