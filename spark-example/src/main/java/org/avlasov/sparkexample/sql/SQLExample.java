package org.avlasov.sparkexample.sql;

import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.FilterFunction;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.sql.Column;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.RowFactory;
import org.apache.spark.sql.SQLContext;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.Metadata;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;
import org.avlasov.sparkexample.entity.Rating;
import org.avlasov.sparkexample.main.AbstractExample;
import org.avlasov.sparkexample.util.DataMapper;

import java.util.List;

import static org.avlasov.sparkexample.util.DataMapper.MOVIE_ID_COLUMN_NAME;
import static org.avlasov.sparkexample.util.DataMapper.RATING_COLUMN_NAME;
import static org.avlasov.sparkexample.util.DataMapper.USER_ID_COLUMN_NAME;

public class SQLExample extends AbstractExample  {

    public SQLExample(String movieRatingsFilePath, String moviesFilePath, DataMapper dataMapper) {
        super(movieRatingsFilePath, moviesFilePath, dataMapper);
    }

    public Row[] findPopularMovies() {
        SparkSession sparkSession = getSparkSession();
        String movieIdColumnName = "movieId";
        try (JavaSparkContext javaSparkContext = getJavaSparkContext()) {
            JavaRDD<Rating> map = javaSparkContext.textFile(movieRatingsFilePath)
                    .map(dataMapper.mapToRating());
            SQLContext sqlContext = new SQLContext(sparkSession);
            Dataset<Row> dataFrame = sqlContext.createDataFrame(map, Rating.class);
            Dataset<Row> averageRatings = dataFrame.groupBy(movieIdColumnName).avg("rating");
            Dataset<Row> ratingsCount = dataFrame.groupBy(movieIdColumnName).count();
            Dataset<Row> averageAndCounts = ratingsCount.join(averageRatings, movieIdColumnName);
            return (Row[]) averageAndCounts.orderBy(new Column("avg(rating)").desc()).take(10);
        }
    }

    public Row[] findTopWorstMovies() {
        SparkSession sparkSession = getSparkSession();
        try {
            JavaRDD<Row> rows = sparkSession.read().text(movieRatingsFilePath).javaRDD();
            JavaRDD<Row> ratingRDD = rows.map(mapRatingLineToRowRating());

            Dataset<Row> ratings = sparkSession.createDataFrame(ratingRDD, getRatingStructType()).cache();

            Dataset<Row> ratingCounts = ratings.groupBy(MOVIE_ID_COLUMN_NAME)
                    .count()
                    .filter((FilterFunction<Row>) row -> row.getLong(row.fieldIndex("count")) > 10);
            Dataset<Row> averageRatings = ratings.groupBy(MOVIE_ID_COLUMN_NAME)
                    .avg(RATING_COLUMN_NAME);

            return (Row[]) ratingCounts.join(averageRatings, MOVIE_ID_COLUMN_NAME)
                    .sort(new Column("avg(" + RATING_COLUMN_NAME + ")"))
                    .take(10);
        } finally {
            sparkSession.stop();
        }
    }

    public List<Row> findUserRatings(int userID) {
        SparkSession sparkSession = getSparkSession();
        try {
            JavaRDD<Row> rows = sparkSession.read().text(movieRatingsFilePath).javaRDD();
            JavaRDD<Row> ratingsRDD = rows.map(mapRatingLineToRowRating());

            Dataset<Row> ratings = sparkSession.createDataFrame(ratingsRDD, getRatingStructType()).cache();

            //or ratings.filter("userID = 0")
            return ratings.filter(new Column(USER_ID_COLUMN_NAME).equalTo(userID)).collectAsList();
        } finally {
            sparkSession.stop();
        }
    }

    @Override
    public JavaSparkContext getJavaSparkContext() {
        return new JavaSparkContext(getSparkSession().sparkContext());
    }

    private SparkSession getSparkSession() {
        return SparkSession
                .builder()
                .master("local[*]")
                .appName("SQL example")
                .getOrCreate();
    }

    private Function<Row, Row> mapRatingLineToRowRating() {
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

//    public void test() {
//        try () {
//            SQLContext sqlContext = new SQLContext(sparkSession);
//            UDFRegistration udf = sqlContext.udf();
//            udf.register("square", (Integer a1) -> a1 * a1, DataTypes.IntegerType);
//            sqlContext.sql("SELECT square('someNumericField FROM tableName");
//            Dataset<Row> json = sqlContext.read().json();
//            json.createOrReplaceGlobalTempView("test");
//            json.show();
//            json.select()
//            sparkSession.sql("SELECT foo FROM bar ORDER BY foobar");
//        }
//    }

}
