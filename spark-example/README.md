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

## Spark SQL
Extends RDD to a "DataFrame" object.
DataFrame is DataSet of Row objects (in Spark 2.0)
Works over Spark Thrift Server
It is possible to define custom function (UDF - User-defines functions) and use them in SQL queries/

DataFrame:
1. Contain Row objects
2. Can run SQL queries
3. Has a schema (leading to more efficient storage)
4. Read and write to JSON, Hive, parquet
5. Communicates with JDBC/ODBC, Tableau

## MLLib
Apache Spark Machine Learning library

## Materials
### Books
https://www.manning.com/books/spark-in-action-second-edition
### Courses
https://www.udemy.com/apache-spark-course-with-java/
https://www.udemy.com/apache-spark-for-java-developers/

# Apache Spark and Cassandra

Example: org.avlasov.sparkexample.cassandra.CassandraExample
Main class: org.avlasov.sparkexample.cassandra.MainCassandraExample

Run with spark-submit
```shell script
spark-submit --packages datastax:spark-cassandra-connector:2.0.0-M2-s_2.11 --class org.avlasov.sparkexample.cassandra.MainCassandraExample spark-example-1.0-SNAPSHOT.jar ./ml-100k/u.data ./ml-100k/u.item ./ml-100k/u.user
```

# Apache Spark and MongoDB

Example: org.avlasov.sparkexample.mongodb.MongoDBExample
Main class: org.avlasov.sparkexample.mongodb.MainMongoDBExample

Run with spark-submit
```shell script
spark-submit --packages org.mongodb.spark:mongo-spark-connector_2.11:2.4.1 --class org.avlasov.sparkexample.mongodb.MainMongoDBExample ./spark-example/build/libs/spark-example-1.0-SNAPSHOT.jar ./data/movie-ratings.txt ./data/movie-data.txt ./data/users.txt
```


# How execute Spark example
1. Build spark-example module ```gradle clean build```
2. Copy jar to HDFS local file system
3. Copy u.data, u.item (Test data can be found by the link https://grouplens.org/datasets/ (ml-100k.zip)) to the HDFS
4. Execute command 

```spark-submit --class <main_class> spark-example-1.0-SNAPSHOT.jar <link_to_u.data_file> <link_to_u.item_file>```

Where **link_to_u.data_file** or **link_to_u.item_file** can be link to HDFS or file on local hadoop env, for example:
* hdfs:///user/maria_dev/ml-100k/u.data 
* ./u.item

And **main_class** can be: 
* org.avlasov.sparkexample.simple.MainMoviesExample
* org.avlasov.sparkexample.sql.MainSQLExample
* org.avlasov.sparkexample.mllib.MainMLLibExample

For **MainSQLExample** and **MainMLLibExample** make sure that you are using Spark2. Run this command ```spark-submit --version```, if you are using first version, run next command ```export SPARK_MAJOR_VERSION=2``` 

Please note: If you are running **MainSQLExample** and **MainMLLibExample**, then files will be searching on hdfs, but on local Hadoop cluster. In this case **hdfs:///user/maria_dev/ml-100k/u.data** and **ml-100k/u.data** is the reference to the same file.