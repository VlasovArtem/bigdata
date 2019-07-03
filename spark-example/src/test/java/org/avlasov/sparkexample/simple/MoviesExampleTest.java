package org.avlasov.sparkexample.simple;

import org.avlasov.sparkexample.util.DataMapper;
import org.hamcrest.collection.IsCollectionWithSize;
import org.junit.Test;
import scala.Tuple2;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;

public class MoviesExampleTest {

    private MoviesExample moviesExample =
            new MoviesExample("../data/movie-ratings.txt", "../data/movie-data.txt", new DataMapper());

    @Test
    public void findWorstMovie() {
        List<Tuple2<Integer, Integer>> top10WorstMovies = moviesExample.findTop10WorstMovies();

        System.out.println(top10WorstMovies);
        assertThat(top10WorstMovies, IsCollectionWithSize.hasSize(10));
        assertEquals(1494, (int) top10WorstMovies.get(0)._2());
    }

}