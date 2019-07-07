package org.avlasov.sparkexample.simple;

import org.avlasov.sparkexample.util.SparkDataMapper;
import org.hamcrest.collection.IsCollectionWithSize;
import org.junit.Assert;
import org.junit.Test;
import scala.Tuple2;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;

public class MoviesExampleTest {

    private MoviesExample moviesExample =
            new MoviesExample("../data/movie-ratings.txt", "../data/movie-data.txt", new SparkDataMapper());

    @Test
    public void findWorstMovie() {
        List<Tuple2<Double, Integer>> top10WorstMovies = moviesExample.findTop10WorstMovies();

        assertThat(top10WorstMovies, IsCollectionWithSize.hasSize(10));
        assertEquals(1494, (int) top10WorstMovies.get(0)._2());
    }

    @Test
    public void findTop10WorstMoviesSecondSolution() {
        List<Tuple2<Double, Tuple2<Integer, Integer>>> top10WorstMoviesSecondSolution = moviesExample.findTop10WorstMoviesSecondSolution();

        assertThat(top10WorstMoviesSecondSolution, IsCollectionWithSize.hasSize(10));

        for (Tuple2<Double, Tuple2<Integer, Integer>> integerTuple2Tuple2 : top10WorstMoviesSecondSolution) {
            Assert.assertTrue(integerTuple2Tuple2._2()._2() > 10);
        }
    }
}