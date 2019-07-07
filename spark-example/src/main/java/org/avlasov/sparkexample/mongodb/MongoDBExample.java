package org.avlasov.sparkexample.mongodb;

import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SaveMode;
import org.apache.spark.sql.SparkSession;
import org.avlasov.entity.User;
import org.avlasov.sparkexample.main.AbstractExample;
import org.avlasov.sparkexample.util.SparkDataMapper;

import java.util.List;

public class MongoDBExample extends AbstractExample {

    private final String userDataFilePath;

    protected MongoDBExample(String movieRatingsFilePath, String moviesFilePath, String userDataFilePath, SparkDataMapper dataMapper) {
        super(movieRatingsFilePath, moviesFilePath, dataMapper);
        this.userDataFilePath = userDataFilePath;
    }

    public void uploadUsers() {
        try (SparkSession sparkSession = getSparkSession();
             JavaSparkContext javaSparkContext = new JavaSparkContext(sparkSession.sparkContext())) {
            JavaRDD<User> users = javaSparkContext.textFile(userDataFilePath)
                    .map(dataMapper.mapToUser());
            Dataset<Row> dataFrame = sparkSession.createDataFrame(users, User.class);
            dataFrame.write()
                    .format("com.mongodb.spark.sql.DefaultSource")
                    .mode(SaveMode.Append)
                    .option("uri", "mongodb://127.0.0.1/movielens.users")
                    .save();
        }
    }

    public List<User> getUserWhereAgeLess(int age) {
        try (SparkSession sparkSession = getSparkSession()) {
            Dataset<Row> load = sparkSession.read()
                    .format("com.mongodb.spark.sql.DefaultSource")
                    .option("uri", "mongodb://127.0.0.1/movielens.users")
                    .load();
            load.createOrReplaceTempView("users");

            return sparkSession.sql("SELECT * FROM users WHERE age < " + age)
                    .javaRDD()
                    .map(mapRowToUser())
                    .collect();
        }
    }

    @Override
    public JavaSparkContext getJavaSparkContext() {
        return new JavaSparkContext(getSparkSession().sparkContext());
    }

    public Function<Row, User> mapRowToUser() {
        return row -> User.builder()
                .userId(row.getInt(row.fieldIndex("userId")))
                .age(row.getInt(row.fieldIndex("age")))
                .gender(row.getString(row.fieldIndex("gender")))
                .occupation(row.getString(row.fieldIndex("occupation")))
                .zip(row.getString(row.fieldIndex("zip")))
                .build();
    }

    private SparkSession getSparkSession() {
        return SparkSession
                .builder()
                .master("local[*]")
                .appName("MongoDB Integration")
                .getOrCreate();
    }


}
