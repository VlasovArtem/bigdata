package org.avlasov.sparkexample.simple;

import org.avlasov.sparkexample.util.DataMapper;
import scala.Tuple2;

import java.util.List;
import java.util.Map;

public class MainMoviesExample {

    public static void main(String[] args) {
        MoviesExample moviesExample = new MoviesExample( args[0], args[1], new DataMapper());

        List<Tuple2<Double, Integer>> top10WorstMovies = moviesExample.findTop10WorstMovies();
        Map<Integer, String> integerStringMap = moviesExample.readMovies();

        for (Tuple2<Double, Integer> top10WorstMovie : top10WorstMovies) {
            System.out.printf("%s, %.1f", integerStringMap.get(top10WorstMovie._2()), top10WorstMovie._1());
        }
    }

}
