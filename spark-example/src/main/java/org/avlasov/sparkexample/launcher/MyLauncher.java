package org.avlasov.sparkexample.launcher;

import org.apache.spark.launcher.SparkAppHandle;
import org.apache.spark.launcher.SparkLauncher;

import java.io.IOException;

public class MyLauncher {

    public static void main(String[] args) throws IOException {
        SparkAppHandle local = new SparkLauncher()
                .setAppResource("/Users/artemvlasov/git/bigdata/spark-example/build/libs/spark-example-1.0-SNAPSHOT.jar")
                .setMainClass("org.avlasov.sparkexample.simple.MainMoviesExample")
                .setMaster("local")
                .setConf(SparkLauncher.DRIVER_MEMORY, "2g")
                .startApplication();
    }

}
