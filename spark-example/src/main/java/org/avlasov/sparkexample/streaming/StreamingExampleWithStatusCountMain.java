package org.avlasov.sparkexample.streaming;

public class StreamingExampleWithStatusCountMain {

    public static void main(String[] args) throws InterruptedException {
        StreamingExample streamingExample = new StreamingExample(Integer.parseInt(args[0]), Integer.parseInt(args[1]));
        streamingExample.readLogsWithStatusCount();
    }

}
