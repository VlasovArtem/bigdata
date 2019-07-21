package org.avlasov.kafka.consumer;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.IntegerDeserializer;
import org.avlasov.kafka.deserializer.ContentDeserializer;
import org.avlasov.kafka.entity.Content;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Properties;

public class KafkaContentConsumer {

    private int iterations;
    private final Properties properties;
    private final int sleepInMillis;

    public KafkaContentConsumer(String properties, int iterations, int sleepInMillis) {
        this.properties = new Properties();
        this.properties.put("bootstrap.servers", properties);
        this.iterations = iterations;
        this.sleepInMillis = sleepInMillis;
    }

    public void run(String topic) {
        properties.put(ConsumerConfig.GROUP_ID_CONFIG, "KafkaExampleConsumer");
        try (KafkaConsumer<Integer, Content> kafkaConsumer = new KafkaConsumer<>(properties, new IntegerDeserializer(), new ContentDeserializer())) {
            kafkaConsumer.subscribe(Collections.singletonList(topic));
            double contentSum = 0;
            while ((--iterations) != 0) {
                ConsumerRecords<Integer, Content> poll = kafkaConsumer.poll(Duration.of(sleepInMillis, ChronoUnit.MILLIS));
                for (ConsumerRecord<Integer, Content> record : poll.records(topic)) {
                    double newValue = record.value().getValue();
                    contentSum += newValue;
                    System.out.printf("Sum on records %d = %.1f (new value added %.1f)%n", record.key(), contentSum, newValue);
                }
                Thread.sleep(sleepInMillis);
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

}
