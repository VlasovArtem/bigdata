package org.avlasov.sparkexample.mllib;

import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.ml.recommendation.ALS;
import org.apache.spark.ml.recommendation.ALSModel;
import org.apache.spark.sql.Column;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.RowFactory;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.functions;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.Metadata;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;
import org.avlasov.sparkexample.main.AbstractExample;
import org.avlasov.sparkexample.util.DataMapper;

import java.util.List;

import static org.avlasov.sparkexample.util.DataMapper.MOVIE_ID_COLUMN_NAME;
import static org.avlasov.sparkexample.util.DataMapper.RATING_COLUMN_NAME;
import static org.avlasov.sparkexample.util.DataMapper.USER_ID_COLUMN_NAME;

public class MLLibExample extends AbstractExample {

    protected MLLibExample(String movieRatingsFilePath, String moviesFilePath, DataMapper dataMapper) {
        super(movieRatingsFilePath, moviesFilePath, dataMapper);
    }

    public List<Row> findUserRatings(int userID) {
        try (SparkSession sparkSession = getSparkSession()) {
            JavaRDD<Row> rows = sparkSession.read().text(movieRatingsFilePath).javaRDD();
            JavaRDD<Row> ratingsRDD = rows.map(mapRatingLineToRowRating());

            Dataset<Row> ratings = sparkSession.createDataFrame(ratingsRDD, getRatingStructType()).cache();

            //or ratings.filter("userID = 0")
            return ratings.filter(new Column(USER_ID_COLUMN_NAME).equalTo(userID)).collectAsList();
        }
    }

    public Row[] findTopRecommendations(int userID) {
        try (SparkSession sparkSession = getSparkSession()) {
            JavaRDD<Row> rows = sparkSession.read().text(movieRatingsFilePath).javaRDD();
            JavaRDD<Row> ratingsRDD = rows.map(mapRatingLineToRowRating());

            Dataset<Row> ratings = sparkSession.createDataFrame(ratingsRDD, getRatingStructType()).cache();

            ALS als = getPreconfiguredALS();
            ALSModel model = als.fit(ratings);

            Dataset<Row> ratingCounts = ratings.groupBy(MOVIE_ID_COLUMN_NAME).count().filter("count > 100");

            Dataset<Row> popularMovies = ratingCounts.select(MOVIE_ID_COLUMN_NAME).withColumn(USER_ID_COLUMN_NAME, functions.lit(userID));

            Dataset<Row> recommendations = model.transform(popularMovies);

            return (Row[]) recommendations.sort(new Column(model.getPredictionCol()).desc()).take(20);
        }
    }

    public void movieRecommendation() {
//        try (SparkSession sparkSession = getSparkSession()) {
//            RDD<Row> rows = sparkSession.read().text(movieRatingsFilePath).rdd();
//            RDD<Row> ratingsRDD = rows.map(mapToRatingRow(), ClassTag.apply(Row.class));
//
//            Dataset<Row> ratings = sparkSession.createDataFrame(rows, ).cache();
//
//            ALS als = getPreconfiguredALS();
//            ALSModel model = als.fit(ratings);
//
//            //or ratings.filter("userID = 0")
//            ratings.filter(new Column(userIDColumnName).equalTo(0));
//
//
//        }
    }

    private SparkSession getSparkSession() {
        return SparkSession
                .builder()
                .config("spark.sql.crossJoin.enabled", "true")
                .master("local[*]")
                .appName("Movie recommendation")
                .getOrCreate();
    }

    private ALS getPreconfiguredALS() {
        ALS als = new ALS();
        als.setMaxIter(5);
        als.setRegParam(0.01);
        als.setUserCol(USER_ID_COLUMN_NAME);
        als.setItemCol(MOVIE_ID_COLUMN_NAME);
        als.setRatingCol(RATING_COLUMN_NAME);
        return als;
    }

    @Override
    public JavaSparkContext getJavaSparkContext() {
        return new JavaSparkContext(getSparkSession().sparkContext());
    }

    public Function<Row, Row> mapRatingLineToRowRating() {
        return row -> {
            String line = row.getString(0);
            String[] split = line.split("\t");
            return RowFactory.create(Integer.parseInt(split[0]), Integer.parseInt(split[1]), Integer.parseInt(split[2]));
        };
    }

    private StructType getRatingStructType() {
        return new StructType(
                new StructField[]{
                        new StructField(USER_ID_COLUMN_NAME, DataTypes.IntegerType, false, Metadata.empty()),
                        new StructField(MOVIE_ID_COLUMN_NAME, DataTypes.IntegerType, false, Metadata.empty()),
                        new StructField(RATING_COLUMN_NAME, DataTypes.IntegerType, false, Metadata.empty())});
    }

}
