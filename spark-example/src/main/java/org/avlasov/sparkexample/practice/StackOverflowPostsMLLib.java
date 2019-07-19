package org.avlasov.sparkexample.practice;

import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.mllib.recommendation.ALS;
import org.apache.spark.mllib.recommendation.MatrixFactorizationModel;
import org.apache.spark.mllib.recommendation.Rating;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.catalyst.expressions.GenericRow;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.Metadata;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;
import scala.Tuple2;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class StackOverflowPostsMLLib implements Serializable {

    private final Pattern postPattern;
    private final String questionIdColumnName;
    private final String tagColumnName;
    private final String tagIdColumnName;
    private final String filePath;
    private final String master;

    public StackOverflowPostsMLLib(String filePath, String master) {
        this.filePath = filePath;
        this.master = master;
        postPattern = Pattern.compile(".* Id=\"(?<questionId>([0-9]+))\".*Tags=\"(?<tags>(\\S+))\".*");
        questionIdColumnName = "questionId";
        tagColumnName = "tag";
        tagIdColumnName = "tagId";
    }

    public List<Tuple2<String, Double>> predict(int questionId, int numberOfTags) {
        MatrixFactorizationModel matrixFactorizationModel = createMatrixFactorizationModel();
        SparkSession sparkSession = getSparkSession();
        List<Tuple2<String, Double>> predict = predict(sparkSession, matrixFactorizationModel, questionId, numberOfTags);
        sparkSession.close();
        return predict;
    }

    public Map<Integer, List<Tuple2<String, Double>>> predict(List<Integer> questionIds, int numberOfTags) {
        MatrixFactorizationModel matrixFactorizationModel = createMatrixFactorizationModel();
        SparkSession sparkSession = getSparkSession();
        Map<Integer, List<Tuple2<String, Double>>> predict = new HashMap<>();
        for (Integer questionId : questionIds) {
            List<Tuple2<String, Double>> predictTags = predict(sparkSession, matrixFactorizationModel, questionId, numberOfTags);
            if (!predictTags.isEmpty()) {
                predict.put(questionId, predictTags);
            }
        }
        sparkSession.close();
        return predict;
    }

    private List<Tuple2<String, Double>> predict(SparkSession sparkSession, MatrixFactorizationModel matrixFactorizationModel, int questionId, int numberOfTags) {
        JavaRDD<Row> questionTable = sparkSession.sql(String.format("SELECT * FROM global_temp.questiontable where %s = %d", questionIdColumnName, questionId)).javaRDD();

        long count = questionTable.count();

        if (count > 0) {
            JavaPairRDD<Integer, Integer> questionIdTagId = questionTable.mapToPair(row ->
                    Tuple2.apply(row.getInt(row.fieldIndex(questionIdColumnName)), (int) row.getLong(row.fieldIndex(tagIdColumnName))));

            List<Rating> predict = matrixFactorizationModel.predict(questionIdTagId)
                    .sortBy(Rating::rating, false, 1)
                    .collect();
            List<Tuple2<String, Double>> tuple2s = new ArrayList<>();
            for (int i = 0; i < Math.min(predict.size(), numberOfTags); i++) {
                Rating rating = predict.get(i);
                String tagName = questionTable.filter(row -> row.getLong(row.fieldIndex(tagIdColumnName)) == rating.product())
                        .map(row -> row.getString(row.fieldIndex(tagColumnName)))
                        .first();
                tuple2s.add(Tuple2.apply(tagName, rating.rating()));
            }
            return tuple2s;
        }
        return Collections.emptyList();
    }

    private MatrixFactorizationModel createMatrixFactorizationModel() {
        SparkSession sparkSession = getSparkSession();
        JavaSparkContext javaSparkContext = new JavaSparkContext(sparkSession.sparkContext());
        JavaRDD<String> stringJavaRDD = javaSparkContext.textFile(filePath);

        JavaRDD<String> posts = stringJavaRDD
                .map(String::trim)
                .filter(v1 -> v1.matches("<row.*/>"));

        JavaPairRDD<Integer, String> questionIdToTag = posts.mapToPair(this::mapToQuestionIdTags)
                .filter(Objects::nonNull)
                .flatMapValues(this::mapTags);

        Dataset<Row> questionIdToTagDataFrame = sparkSession.createDataFrame(questionIdToTag
                .map(tuple -> new GenericRow(new Object[]{tuple._1(), tuple._2()})), getQuestionIdTagStructType());

        JavaPairRDD<String, Long> tagsTable = questionIdToTag.values()
                .distinct()
                .zipWithUniqueId()
                .cache();

        Dataset<Row> tagsTableDataFrame = sparkSession.createDataFrame(tagsTable
                .map(tuple -> new GenericRow(new Object[]{tuple._1(), tuple._2()})), getTagsTableStructType());

        Dataset<Row> questionTable = questionIdToTagDataFrame.join(tagsTableDataFrame, tagColumnName);

        questionTable.createOrReplaceGlobalTempView("questionTable");

        JavaPairRDD<Integer, Integer> questionIdToTagId = questionTable
                .javaRDD()
                .mapToPair(row -> Tuple2.apply(row.getInt(row.fieldIndex(questionIdColumnName)), (int) row.getLong(row.fieldIndex(tagIdColumnName))));

        JavaRDD<Rating> ratingJavaRDD = questionIdToTagId.map(tuple -> new Rating(tuple._1(), tuple._2(), 1.0));

        return ALS.trainImplicit(ratingJavaRDD.rdd(), 50, 15);
    }

    private SparkSession getSparkSession() {
        return SparkSession
                .builder()
                .master(master)
                .appName("User Visits example")
                .getOrCreate();
    }

    private Tuple2<Integer, String> mapToQuestionIdTags(String line) {
        Matcher matcher = postPattern.matcher(line);
        if (matcher.find()) {
            return Tuple2.apply(Integer.parseInt(matcher.group("questionId")), matcher.group("tags"));
        }
        return null;
    }

    private Iterable<String> mapTags(String tagsLine) {
        return Arrays.stream(tagsLine.split("&lt;"))
                .map(tag -> tag.replace("&gt;", "").trim())
                .filter(tag -> !Objects.toString(tag, "").equals(""))
                .collect(Collectors.toList());
    }

    private StructType getTagsTableStructType() {
        return new StructType(
                new StructField[]{
                        new StructField(tagColumnName, DataTypes.StringType, false, Metadata.empty()),
                        new StructField(tagIdColumnName, DataTypes.LongType, false, Metadata.empty())
                });
    }

    private StructType getQuestionIdTagStructType() {
        return new StructType(
                new StructField[]{
                        new StructField(questionIdColumnName, DataTypes.IntegerType, false, Metadata.empty()),
                        new StructField(tagColumnName, DataTypes.StringType, false, Metadata.empty())
                });
    }

    public static void main(String[] args) {
        StackOverflowPostsMLLib stackOverflowPostsMLLib = new StackOverflowPostsMLLib(args[0], args[1]);

        List<Integer> questionIds = Arrays.stream(args[2].split("\\s*,\\s*"))
                .map(Integer::parseInt)
                .collect(Collectors.toList());

        Map<Integer, List<Tuple2<String, Double>>> predict = stackOverflowPostsMLLib.predict(questionIds, Integer.parseInt(args[3]));
        for (Map.Entry<Integer, List<Tuple2<String, Double>>> integerListEntry : predict.entrySet()) {
            System.out.println("Tag predict for question with id - " + integerListEntry.getKey());
            for (Tuple2<String, Double> value : integerListEntry.getValue()) {
                System.out.println(String.format("Tag - %s, Rating - %f", value._1(), value._2()));
            }
            System.out.println("");
        }
    }

}
