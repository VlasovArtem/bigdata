package org.avlasov.sparkexample.practice;

import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.SparkSession;
import org.avlasov.sparkexample.practice.entity.UserVisit;
import scala.Serializable;
import scala.Tuple2;

import java.util.Arrays;
import java.util.List;

public class UserVisits implements Serializable {

    private final String master;

    public UserVisits(String master) {
        this.master = master;
    }

    public List<Tuple2<String, Integer>> findTop10CountriesByVisit(String path) {
        SparkSession sparkSession = getSparkSession();
        try (JavaSparkContext javaSparkContext = new JavaSparkContext(sparkSession.sparkContext())) {
            return javaSparkContext.wholeTextFiles(path)
                    .map(Tuple2::_2)
                    .flatMap(fileContent -> Arrays.asList(fileContent.split("\\n")).iterator())
                    .map(this::mapToUserVisit)
                    .map(UserVisit::getCountryCode)
                    .mapToPair(countryCode -> Tuple2.apply(countryCode, 1))
                    .reduceByKey((v1, v2) -> v1 + v2)
                    .map(countryCodeCountTuple -> countryCodeCountTuple)
                    .sortBy(Tuple2::_2, false, 1)
                    .take(10);
        }
    }

    private SparkSession getSparkSession() {
        return SparkSession
                .builder()
                .master(master)
                .appName("User Visits example")
                .getOrCreate();
    }

    private UserVisit mapToUserVisit(String line) {
        String[] userVisitData = line.split(",");
        return UserVisit.builder()
                .sourceIP(userVisitData[0])
                .destURL(userVisitData[1])
                .visitDate(userVisitData[2])
                .adRevenue(Float.parseFloat(userVisitData[3]))
                .userAgent(userVisitData[4])
                .countryCode(userVisitData[5])
                .languageCode(userVisitData[6])
                .searchWord(userVisitData[7])
                .duration(Integer.parseInt(userVisitData[8]))
                .build();
    }

    public static void main(String[] args) {
        UserVisits userVisits = new UserVisits(args[0]);
        List<Tuple2<String, Integer>> top10CountriesByVisit = userVisits.findTop10CountriesByVisit(args[1]);

        for (Tuple2<String, Integer> countryCodeVisitsTuple : top10CountriesByVisit) {
            System.out.printf("Country code: %s, Visits: %d%n", countryCodeVisitsTuple._1(), countryCodeVisitsTuple._2());
        }
    }

}
