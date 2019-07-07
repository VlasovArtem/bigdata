package org.avlasov.sparkexample.util;

import org.apache.spark.api.java.function.Function;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.RowFactory;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.Metadata;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;
import org.avlasov.entity.Movie;
import org.avlasov.entity.Rating;
import org.avlasov.entity.User;
import org.avlasov.util.DataMapper;

public class SparkDataMapper extends DataMapper {

    public StructType getUserStructType() {
        return new StructType(
                new StructField[]{
                        new StructField("user_id", DataTypes.IntegerType, false, Metadata.empty()),
                        new StructField("age", DataTypes.IntegerType, false, Metadata.empty()),
                        new StructField("gender", DataTypes.StringType, false, Metadata.empty()),
                        new StructField("occupation", DataTypes.StringType, false, Metadata.empty()),
                        new StructField("zip", DataTypes.StringType, false, Metadata.empty())});
    }

    public Function<User, Row> mapUserToRow() {
        return user -> RowFactory.create(user.getUserId(), user.getAge(), user.getGender(), user.getOccupation(), user.getZip());
    }

    public Function<String, Rating> mapToRating() {
        return this::mapRating;
    }

    public Function<String, Movie> mapToMovie() {
        return line -> {
            String[] split = line.split("\\|");
            return Movie.builder()
                    .movieID(Integer.parseInt(split[0]))
                    .name(split[1])
                    .build();
        };
    }

    public Function<String, User> mapToUser() {
        return line -> {
            String[] split = line.split("\\|");
            return User.builder()
                    .userId(Integer.parseInt(split[0]))
                    .age(Integer.parseInt(split[1]))
                    .gender(split[2])
                    .occupation(split[3])
                    .zip(split[4])
                    .build();
        };
    }

}
