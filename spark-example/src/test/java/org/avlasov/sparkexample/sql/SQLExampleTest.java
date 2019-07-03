package org.avlasov.sparkexample.sql;

import org.apache.spark.sql.Row;
import org.apache.spark.sql.RowFactory;
import org.avlasov.sparkexample.util.DataMapper;
import org.hamcrest.collection.IsCollectionWithSize;
import org.hamcrest.core.IsCollectionContaining;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;
import java.util.Map;

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

    @Test
    public void findUserRatings() {
        List<Row> userRatings = sqlExample.findUserRatings(1);

        Assert.assertThat(userRatings, IsCollectionWithSize.hasSize(272));
        Assert.assertThat(userRatings, IsCollectionContaining.hasItem(RowFactory.create(1, 171, 5)));
    }

    @Test
    public void findTopWorstMovies() {
        Row[] topWorstMovies = sqlExample.findTopWorstMovies();

        Map<Integer, String> movies = sqlExample.readMovies();

        Assert.assertEquals(10, topWorstMovies.length);
        for (Row row : topWorstMovies) {
            Assert.assertTrue(row.getLong(row.fieldIndex("count")) > 10);
        }
//        for (Row row : topWorstMovies) {
//            int movieID = row.getInt(row.fieldIndex(DataMapper.MOVIE_ID_COLUMN_NAME));
//            System.out.printf("Movie '%s' (id = %d) with average rating = %.1f and total ratings = %d%n",
//                    movies.get(movieID), movieID,
//                    row.getDouble(row.fieldIndex("avg(rating)")),
//                    row.getLong(row.fieldIndex("count")));
//        }
    }

}