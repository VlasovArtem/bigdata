package org.avlasov.flink;

import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

public class WordCountExample {

    public static void main(String[] args) {
        ParameterTool parameterTool = ParameterTool.fromArgs(args);

        StreamExecutionEnvironment executionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment();

        executionEnvironment.getConfig().setGlobalJobParameters(parameterTool);

    }

}
