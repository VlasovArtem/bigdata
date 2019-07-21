package org.avlasov.kafka;

import org.avlasov.kafka.consumer.KafkaContentConsumer;
import org.avlasov.kafka.producer.KafkaContentProducer;

public class KafkaExample {

    public static void main(String[] args) {
        int iterations = 1000;
        int sleepInMillis = 1000;
        String topic = "test";

        KafkaContentConsumer kafkaConsumerExample = new KafkaContentConsumer("localhost:9092", iterations, sleepInMillis);
        KafkaContentProducer kafkaProducerExample = new KafkaContentProducer("localhost:9092", iterations, sleepInMillis);

        Thread consumerThread = new Thread(() -> kafkaConsumerExample.run(topic));
        Thread producerThread = new Thread(() -> kafkaProducerExample.run(topic));

        consumerThread.start();
        producerThread.start();
    }

}
