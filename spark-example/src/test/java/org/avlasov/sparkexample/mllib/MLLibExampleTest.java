package org.avlasov.sparkexample.mllib;

import org.apache.spark.sql.Row;
import org.avlasov.sparkexample.util.SparkDataMapper;
import org.junit.Assert;
import org.junit.Test;

public class MLLibExampleTest {

    private MLLibExample mlLibExample =
            new MLLibExample("../data/movie-ratings.txt", "../data/movie-data.txt", new SparkDataMapper());

    @Test
    public void findTopRecommendations() {
        Row[] topRecommendations = mlLibExample.findTopRecommendations(0);

        Assert.assertEquals(20, topRecommendations.length);
    }

}