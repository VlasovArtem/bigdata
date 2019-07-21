package org.avlasov.sparkexample.practice;

import org.apache.spark.sql.Column;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SQLContext;
import org.apache.spark.sql.SparkSession;

import java.util.List;

import static org.apache.spark.sql.functions.col;
import static org.apache.spark.sql.functions.lower;
import static org.apache.spark.sql.functions.when;

public class Vacancies {

    private final String master;

    public Vacancies(String master) {
        this.master = master;
    }

    public List<Row> averageSalaryByProfessionAndCity(String headHuntersJsonFilePath) {
        SparkSession sparkSession = getSparkSession();
        sparkSession.sparkContext().setLogLevel("ERROR");
        SQLContext sqlContext = new SQLContext(sparkSession);
        Dataset<Row> json = sqlContext.read().json(headHuntersJsonFilePath);

        String salaryAverageColumnName = "salaryAverage";

        return json
                .withColumn("vacancyType", vacanciesTypeColumn())
                .filter(col("salary").isNotNull().or(col("salary.to").isNotNull().and(col("salary.from").isNotNull())))
                .withColumn(salaryAverageColumnName, salaryAverageColumn().divide("2"))
                .groupBy(col("area.name"), col("vacancyType"), col("salary.currency"))
                .avg(salaryAverageColumnName)
                .sort(col("area.name"))
                .collectAsList();
    }

    private SparkSession getSparkSession() {
        return SparkSession
                .builder()
                .master(master)
                .appName("User Visits example")
                .getOrCreate();
    }

    private Column vacanciesTypeColumn() {
        return when(lower(col("name")).contains("программист")
                        .or(lower(col("name")).contains("разработчик"))
                        .or(lower(col("name")).contains("developer")), "Programmer")
                .when(lower(col("name")).contains("дизайнер")
                        .or(lower(col("name")).contains("design")), "Designer")
                .when(lower(col("name")).contains("администратор"), "System Administrator")
                .when(col("name").isNotNull(), "Other");
    }

    private Column salaryAverageColumn() {
        return when(col("salary.from").isNull(), col("salary.to"))
                .when(col("salary.from").isNotNull(), col("salary.from"))
                .plus(when(col("salary.to").isNull(), col("salary.from"))
                        .when(col("salary.to").isNotNull(), col("salary.to")));
    }

    public static void main(String[] args) {
        Vacancies vacancies = new Vacancies(args[0]);

        List<Row> rows = vacancies.averageSalaryByProfessionAndCity(args[1]);
        for (Row row : rows) {
            System.out.println(row);
        }
    }

}
