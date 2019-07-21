package org.avlasov.kafka.producer;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.IntegerSerializer;
import org.avlasov.kafka.entity.Content;
import org.avlasov.kafka.serializer.ContentSerializer;

import java.util.Properties;
import java.util.Random;

public class KafkaContentProducer {

    private final int iterations;
    private final Properties properties;
    private final Random random;
    private final int sleepInMillis;

    public KafkaContentProducer(String bootstrapServer, int iterations, int sleepInMillis) {
        this.iterations = iterations;
        this.properties = new Properties();
        this.properties.put("bootstrap.servers", bootstrapServer);
        this.sleepInMillis = sleepInMillis;
        this.random = new Random();
    }

    public void run(String topic) {
        try (KafkaProducer<Integer, Content> kafkaProducer = new KafkaProducer<>(properties, new IntegerSerializer(), new ContentSerializer())) {
            int number = 1;

            while (number <= iterations) {
                kafkaProducer.send(new ProducerRecord<>(topic, number, new Content(random.nextDouble() * 100)));
                number++;
                Thread.sleep(sleepInMillis);
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

}
