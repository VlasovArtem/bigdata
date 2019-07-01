# Apache Spark
"A fast and general engine for large-scale data processing"

https://spark.apache.org/

This framework is using Cluster Manager (Spark, YARN) to define on which cluster to run his program. Key to performance is Cache. Is memory store solution.
"Run programs up to 100x faster than Hadoop MapReduce in memory, or 10x faster on disk."
DAG engine (directed acyclic graph) optimizes workflows (like TEZ for Pig).

## RDD
Resilient Distributed Dataset

### The SparkContext
* Created by your driver program
* Is responsible for making RDD's resilient and distributed
* Creates RDD
* The spark shell creates a "sc" object for you

## Materials
### Books
https://www.manning.com/books/spark-in-action-second-edition
### Courses
https://www.udemy.com/apache-spark-course-with-java/
https://www.udemy.com/apache-spark-for-java-developers/

# How execute Spark example
1. Build spark-example module ```gradle clean build```
2. Copy jar to HDFS local file system
3. Copy u.data, u.item (Test data can be found by the link https://grouplens.org/datasets/ (ml-100k.zip)) to the HDFS
4. Execute command 

```spark-submit --class org.avlasov.sparkexample.simple.MainMoviesExample spark-example-1.0-SNAPSHOT.jar <link_to_u.data_file> <link_to_u.item_file>```

Where **link_to_u.data_file** or **link_to_u.item_file** can be link to HDFS or file on local hadoop env, for example:
* hdfs:///user/maria_dev/ml-100k/u.data 
* ./u.item
