package org.avlasov.sparkexample.sql;

import org.apache.spark.sql.Row;
import org.avlasov.sparkexample.util.DataMapper;

import java.util.Map;

public class MainSQLExample {

    public static void main(String[] args) {
        SQLExample sqlExample = new SQLExample(args[0], args[1], new DataMapper());

        Row[] popularMovies = sqlExample.findPopularMovies();
        Map<Integer, String> movies = sqlExample.readMovies();

        for (Row popularMovie : popularMovies) {
            System.out.printf("%s, %d, %.1f%n", movies.get(popularMovie.getInt(0)), popularMovie.getLong(1), popularMovie.getDouble(2));
        }
    }

}
