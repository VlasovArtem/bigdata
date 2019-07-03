package org.avlasov.sparkexample.sql;

import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.Column;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SQLContext;
import org.apache.spark.sql.SparkSession;
import org.avlasov.sparkexample.main.AbstractExample;
import org.avlasov.sparkexample.entity.Rating;
import org.avlasov.sparkexample.util.DataMapper;

public class SQLExample extends AbstractExample  {

    protected SQLExample(String movieRatingsFilePath, String moviesFilePath, DataMapper dataMapper) {
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
