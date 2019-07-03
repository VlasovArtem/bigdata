package org.avlasov.sparkexample.mllib;

import org.apache.spark.ml.recommendation.ALS;
import org.apache.spark.sql.Row;
import org.avlasov.sparkexample.util.DataMapper;

import java.util.List;
import java.util.Map;

public class MainMLLibExample {

    public static void main(String[] args) {
        MLLibExample mlLibExample = new MLLibExample(args[0], args[1], new DataMapper());

        Map<Integer, String> moviesData = mlLibExample.readMovies();
        int userID = Integer.parseInt(args[2]);
        List<Row> userRatings = mlLibExample.findUserRatings(userID);

        System.out.printf("User (id = %d) ratings%n", userID);
        for (Row userRating : userRatings) {
            System.out.printf("%s - rating %d%n",
                    moviesData.get((int) userRating.getAs(DataMapper.MOVIE_ID_COLUMN_NAME)),
                    (int) userRating.getAs(DataMapper.RATING_COLUMN_NAME));
        }

        Row[] topRecommendations = mlLibExample.findTopRecommendations(userID);

        System.out.printf("%nUser (id = %d) top 20 recommendations%n", userID);
        for (Row topRecommendation : topRecommendations) {
            System.out.printf("%s - prediction %.1f%n", moviesData.get(topRecommendation.getAs(DataMapper.MOVIE_ID_COLUMN_NAME)),
                    (float) topRecommendation.getAs(new ALS().getPredictionCol()));
        }
    }

}
