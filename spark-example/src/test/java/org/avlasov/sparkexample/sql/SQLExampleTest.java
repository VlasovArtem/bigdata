package org.avlasov.sparkexample.sql;

import org.apache.spark.sql.Row;
import org.avlasov.sparkexample.util.DataMapper;
import org.junit.Assert;
import org.junit.Test;

public class SQLExampleTest {

    private SQLExample sqlExample = new SQLExample( "../data/movie-ratings.txt", "../data/movie-data.txt", new DataMapper());

    @Test
    public void findPopularMovie() {
        Row[] popularMovies = sqlExample.findPopularMovies();

        Assert.assertEquals(10, popularMovies.length);
        Assert.assertEquals(1536, popularMovies[0].getInt(0));
        Assert.assertEquals(1, popularMovies[0].getLong(1), 0.0);
        Assert.assertEquals(5.0, popularMovies[0].getDouble(2), 0.0);
    }

}