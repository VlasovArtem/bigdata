package org.avlasov.sparkexample.streaming;

public class StreamingExampleWithUrlCountMain {

    public static void main(String[] args) throws InterruptedException {
        StreamingExample streamingExample = new StreamingExample();
        streamingExample.readLogsWithRequestUrlCount();
    }

}
