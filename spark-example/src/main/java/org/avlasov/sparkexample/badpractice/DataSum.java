package org.avlasov.sparkexample.badpractice;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.util.LongAccumulator;

import java.util.Arrays;
import java.util.List;

public class DataSum {

    //It is bad practice because of closure. Spark will create copy of this operation, and return value probably will be 0. It can works on local,
    // but will fail on cluster mode.
    public int sumData(List<Integer> data) {
        final int[] counter = {0};
        SparkConf sparkConfig = getSparkConfig();
        try (JavaSparkContext javaSparkContext = new JavaSparkContext(sparkConfig)) {
            JavaRDD<Integer> parallelize = javaSparkContext.parallelize(data);

            parallelize.foreach(x -> counter[0] += x);
        }
        return counter[0];
    }

    public long sumDataCorrect(List<Integer> data) {
        SparkConf sparkConfig = getSparkConfig();
        try (JavaSparkContext javaSparkContext = new JavaSparkContext(sparkConfig)) {
            LongAccumulator longAccumulator = javaSparkContext.sc().longAccumulator();

            JavaRDD<Integer> parallelize = javaSparkContext.parallelize(data);

            parallelize.foreach(longAccumulator::add);

            //longAccumulator.value() - return sum
            return longAccumulator.sum();
        }
    }

    private SparkConf getSparkConfig() {
        return new SparkConf()
                .setMaster("local[*]")
                .setAppName("Counter Bad Practice");
    }

    public static void main(String[] args) {
        DataSum dataSum = new DataSum();

        System.out.println("Incorrect counter " + dataSum.sumData(Arrays.asList(1, 2, 3, 4, 5)));
        System.out.println("Correct counter " + dataSum.sumDataCorrect(Arrays.asList(1, 2, 3, 4, 5)));
    }

}
