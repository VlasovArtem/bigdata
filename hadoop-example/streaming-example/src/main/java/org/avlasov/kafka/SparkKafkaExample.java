package org.avlasov.kafka;

import org.avlasov.kafka.producer.KafkaContentProducer;
import org.avlasov.kafka.spark.SparkStreaming;

public class SparkKafkaExample {

    public static void main(String[] args) {
        String bootstrapServer = "localhost:9092";

        KafkaContentProducer kafkaContentProducer = new KafkaContentProducer(bootstrapServer, 1000, 1000);
        SparkStreaming sparkStreaming = new SparkStreaming(bootstrapServer);

        Thread kafkaContentProducerThread = new Thread(() -> kafkaContentProducer.run("test"));
        Thread sparkStreamingThread = new Thread(sparkStreaming::startStreaming);

        kafkaContentProducerThread.start();
        sparkStreamingThread.start();
    }

}
