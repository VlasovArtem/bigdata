package org.avlasov.kafka.spark;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.IntegerSerializer;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.Function2;
import org.apache.spark.streaming.Duration;
import org.apache.spark.streaming.StreamingContext;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import org.apache.spark.streaming.kafka010.ConsumerStrategy;
import org.apache.spark.streaming.kafka010.KafkaUtils;
import org.apache.spark.streaming.kafka010.LocationStrategies;
import org.apache.spark.streaming.kafka010.Subscribe;
import org.avlasov.kafka.entity.Content;
import org.avlasov.kafka.entity.Summary;
import org.avlasov.kafka.serializer.SummarySerializer;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class SparkStreaming implements Serializable {

    private final String bootstrapServer;

    public SparkStreaming(String bootstrapServer) {
        this.bootstrapServer = bootstrapServer;
    }

    private SparkConf getSparkConf() {
        return new SparkConf().setMaster("local[*]").setAppName("Spark Streaming Example");
    }

    public void startStreaming() {
        try (JavaStreamingContext javaStreamingContext = getJavaStreamingContext();
             KafkaProducer<Integer, Summary> kafkaProducer = new KafkaProducer<>(getKafkaProperties(), new IntegerSerializer(), new SummarySerializer())) {
            ConsumerStrategy<Integer, Content> consumerStrategy = getConsumerStrategy();

            KafkaUtils.createDirectStream(javaStreamingContext, LocationStrategies.PreferConsistent(), consumerStrategy)
                    .map(mapToSummary())
                    .reduceByWindow(reduceSummary(), Duration.apply(TimeUnit.MINUTES.toMillis(1)), Duration.apply(TimeUnit.MINUTES.toMillis(1)))
                    .foreachRDD((v1, v2) -> {
                        if (!v1.isEmpty()) {
                            kafkaProducer.send(new ProducerRecord<>("Ex", v1.first()));
                        }
                    });
            javaStreamingContext.start();
            javaStreamingContext.awaitTermination();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private ConsumerStrategy<Integer, Content> getConsumerStrategy() {
        Map<String, Object> properties = getKafkaProperties();
        properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.IntegerDeserializer");
        properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "org.avlasov.kafka.deserializer.ContentDeserializer");
        properties.put(ConsumerConfig.GROUP_ID_CONFIG, "Spark Kafka Stream Example");
        return new Subscribe<>(Collections.singletonList("test"), properties, Collections.emptyMap());
    }

    private Map<String, Object> getKafkaProperties() {
        Map<String, Object> properties = new HashMap<>();
        properties.put("bootstrap.servers", bootstrapServer);
        return properties;
    }

    private JavaStreamingContext getJavaStreamingContext() {
        SparkConf sparkConf = getSparkConf();
        StreamingContext streamingContext = new StreamingContext(sparkConf, Duration.apply(TimeUnit.SECONDS.toMillis(1)));
        return new JavaStreamingContext(streamingContext);
    }

    private Function2<Summary, Summary, Summary> reduceSummary() {
        return (v1, v2) -> {
            int count = v1.getCount() + v2.getCount();
            double sum = v1.getSum() + v2.getSum();
            return new Summary(Math.min(v1.getMin(), v2.getMin()), Math.max(v1.getMax(), v2.getMax()), count, sum, sum / count, LocalDateTime.now());
        };
    }

    private Function<ConsumerRecord<Integer, Content>, Summary> mapToSummary() {
        return v1 -> {
            double value = v1.value().getValue();
            return new Summary(value, value, 1, value, value, LocalDateTime.now());
        };
    }


}
