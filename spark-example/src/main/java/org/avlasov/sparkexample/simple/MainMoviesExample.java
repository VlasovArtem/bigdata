package org.avlasov.sparkexample.simple;

import org.avlasov.sparkexample.simple.util.DataMapper;
import scala.Tuple2;

import java.util.List;

public class MainMoviesExample {

    public static void main(String[] args) {
        MoviesExample moviesExample = new MoviesExample(new DataMapper(), args[0], args[1]);

        List<Tuple2<Integer, Integer>> top10WorstMovies = moviesExample.findTop10WorstMovies();

        System.out.println(top10WorstMovies);
    }

}
