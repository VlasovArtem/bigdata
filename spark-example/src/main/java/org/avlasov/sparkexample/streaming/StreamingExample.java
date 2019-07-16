package org.avlasov.sparkexample.streaming;

import org.apache.spark.SparkContext;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.streaming.Duration;
import org.apache.spark.streaming.StreamingContext;
import org.apache.spark.streaming.api.java.JavaPairDStream;
import org.apache.spark.streaming.api.java.JavaReceiverInputDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import org.apache.spark.streaming.flume.FlumeUtils;
import org.apache.spark.streaming.flume.SparkFlumeEvent;
import org.avlasov.sparkexample.streaming.entity.LogRecord;
import org.avlasov.sparkexample.streaming.entity.LogRequest;
import scala.Serializable;
import scala.Tuple2;

import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StreamingExample implements Serializable {

    private final Pattern logPattern;
    private final Duration batchDuration;
    private final Duration slideDuration;
    private final Duration windowDuration;

    public StreamingExample(long batchIntervalSeconds, int slideIntervalSeconds) {
        this.logPattern = Pattern.compile("(?<host>([0-9]{1,3}\\.){3}[0-9]{1,3}\\S) (\\S+) (?<user>\\S+) \\[(?<time>.*)\\] \"(?<requestMethod>\\w+) (?<requestUrl>.*) (?<requestEnd>.*)\" (?<status>[0-9]+) (?<size>\\S+) \"(?<refer>.*)\" \"(?<agent>.*)\"");
        batchDuration = Duration.apply(TimeUnit.SECONDS.toMillis(batchIntervalSeconds));
        slideDuration = Duration.apply(TimeUnit.SECONDS.toMillis(slideIntervalSeconds));
        windowDuration = Duration.apply(TimeUnit.MINUTES.toMillis(5));
    }

    public void readLogsWithRequestUrlCount() throws InterruptedException {
        JavaStreamingContext javaStreamingContext = getStreamingContext();

        JavaReceiverInputDStream<SparkFlumeEvent> flumeStream = FlumeUtils.createStream(javaStreamingContext, "localhost", 9092);

        JavaPairDStream<String, Integer> urlCounts = flumeStream.map(v1 -> new String(v1.event().getBody().array()))
                .map(this::toLogRecord)
                .filter(Objects::nonNull)
                .map(logRecord -> logRecord.getRequest().getUrl())
                .mapToPair(refer -> Tuple2.apply(refer, 1))
                .reduceByKeyAndWindow(Integer::sum, (v1, v2) -> v1 - v2,
                        windowDuration,
                        slideDuration)
                .transform(logPair -> logPair.map(v1 -> v1).sortBy(Tuple2::_2, false, 1))
                .mapToPair(tuple2 -> tuple2);

        urlCounts.print();

        javaStreamingContext.checkpoint("/Users/artemvlasov/Documents/flume/checkpoint");
        javaStreamingContext.start();
        javaStreamingContext.awaitTermination();
    }

    public void readLogsWithStatusCount() throws InterruptedException {
        JavaStreamingContext javaStreamingContext = getStreamingContext();

        JavaReceiverInputDStream<SparkFlumeEvent> flumeStream = FlumeUtils.createStream(javaStreamingContext, "localhost", 9092);

        JavaPairDStream<Integer, Integer> urlCounts = flumeStream.map(v1 -> new String(v1.event().getBody().array()))
                .map(this::toLogRecord)
                .filter(Objects::nonNull)
                .map(LogRecord::getStatus)
                .mapToPair(refer -> Tuple2.apply(refer, 1))
                .reduceByKeyAndWindow(Integer::sum, (v1, v2) -> v1 - v2,
                        windowDuration,
                        slideDuration)
                .transform(logPair -> logPair.map(v1 -> v1).sortBy(Tuple2::_2, false, 1))
                .mapToPair(tuple2 -> tuple2);

        urlCounts.print();

        javaStreamingContext.checkpoint("/Users/artemvlasov/Documents/flume/checkpoint");
        javaStreamingContext.start();
        javaStreamingContext.awaitTermination();
    }

    private JavaStreamingContext getStreamingContext() {
        SparkSession sparkSession = getSparkSession();
        SparkContext sparkContext = sparkSession.sparkContext();
        sparkContext.setLogLevel("INFO");
        StreamingContext streamingContext = new StreamingContext(sparkContext, batchDuration);
        return new JavaStreamingContext(streamingContext);
    }

    private SparkSession getSparkSession() {
        return SparkSession
                .builder()
                .master("local[*]")
                .appName("Streaming example")
                .getOrCreate();
    }

    private LogRecord toLogRecord(String line) {
        Matcher matcher = logPattern.matcher(line);
        if (matcher.find()) {
            return LogRecord.builder()
                    .host(matcher.group("host"))
                    .user(matcher.group("user"))
                    .time(matcher.group("time"))
                    .request(new LogRequest(matcher.group("requestMethod"), matcher.group("requestUrl"), matcher.group("requestEnd")))
                    .status(Integer.parseInt(matcher.group("status")))
                    .size(matcher.group("size"))
                    .refer(matcher.group("refer"))
                    .agent(matcher.group("agent"))
                    .build();
        }
        return null;
    }

}
