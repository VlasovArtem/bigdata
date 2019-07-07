package org.avlasov.sparkexample.cassandra;

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

public class CassandraExample extends AbstractExample {

    private final String userDataFilePath;

    public CassandraExample(String movieRatingsFilePath, String moviesFilePath, String userDataFilePath, SparkDataMapper dataMapper) {
        super(movieRatingsFilePath, moviesFilePath, dataMapper);
        this.userDataFilePath = userDataFilePath;
    }

    public void uploadUsers() {
        try (SparkSession sparkSession = getSparkSession();
             JavaSparkContext javaSparkContext = new JavaSparkContext(sparkSession.sparkContext())) {
            JavaRDD<Row> users = javaSparkContext.textFile(userDataFilePath)
                    .map(dataMapper.mapToUser())
                    .map(dataMapper.mapUserToRow());
            Dataset<Row> dataFrame = sparkSession.createDataFrame(users, dataMapper.getUserStructType());
            dataFrame.write()
                    .format("org.apache.spark.sql.cassandra")
                    .mode(SaveMode.Append)
                    .option("table", "users")
                    .option("keyspace", "movielens")
                    .save();
        }
    }

    public List<User> getUserWhereAgeLess(int age) {
        try (SparkSession sparkSession = getSparkSession()) {
            Dataset<Row> load = sparkSession.read()
                    .format("org.apache.spark.sql.cassandra")
                    .option("table", "users")
                    .option("keyspace", "movielens")
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
                .userId(row.getInt(0))
                .age(row.getInt(1))
                .gender(row.getString(2))
                .occupation(row.getString(3))
                .zip(row.getString(4))
                .build();
    }

    private SparkSession getSparkSession() {
        return SparkSession
                .builder()
                .master("local[*]")
                .config("spark.cassandra.connection.host", "127.0.0.1")
                .appName("Cassandra Integration")
                .getOrCreate();
    }

}
