package org.avlasov.sparkexample.simple;

import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.SparkSession;

import java.util.List;
import java.util.Map;

public class SimpleExample {

    public static final String EVEN = "even";
    public static final String ODD = "odd";

    public Map<String, Iterable<Integer>> groupNumbersByEvenOdd(List<String> numbers) {
        try (JavaSparkContext javaSparkContext = new JavaSparkContext(getSparkSession().sparkContext())) {
            return javaSparkContext.parallelize(numbers)
                    .map(Integer::parseInt)
                    .groupBy(v1 -> v1 % 2 == 0 ? EVEN : ODD)
                    .collectAsMap();
        }
    }

    public List<String> stringsSortedByLength(List<String> strings) {
        try (JavaSparkContext javaSparkContext = new JavaSparkContext(getSparkSession().sparkContext())) {
            return javaSparkContext.parallelize(strings)
                    .sortBy(String::length, false, 1)
                    .collect();
        }
    }

    public List<String> top(List<String> strings) {
        try (JavaSparkContext javaSparkContext = new JavaSparkContext(getSparkSession().sparkContext())) {
            return javaSparkContext.parallelize(strings)
                    .top(1);
        }
    }

    private SparkSession getSparkSession() {
        return SparkSession.builder()
                .master("local[*]")
                .getOrCreate();
    }

}
