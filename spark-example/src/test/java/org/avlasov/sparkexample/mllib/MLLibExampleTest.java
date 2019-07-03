package org.avlasov.sparkexample.mllib;

import org.apache.spark.sql.Row;
import org.apache.spark.sql.RowFactory;
import org.avlasov.sparkexample.util.DataMapper;
import org.hamcrest.collection.IsCollectionWithSize;
import org.hamcrest.core.IsCollectionContaining;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class MLLibExampleTest {

    private MLLibExample mlLibExample =
            new MLLibExample("../data/movie-ratings.txt", "../data/movie-data.txt", new DataMapper());

    @Test
    public void findUserRatings() {
        List<Row> userRatings = mlLibExample.findUserRatings(1);

        Assert.assertThat(userRatings, IsCollectionWithSize.hasSize(272));
        Assert.assertThat(userRatings, IsCollectionContaining.hasItem(RowFactory.create(1, 171, 5)));
    }

    @Test
    public void findTopRecommendations() {
        Row[] topRecommendations = mlLibExample.findTopRecommendations(0);

        Assert.assertEquals(20, topRecommendations.length);
    }
}