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
spark-submit --packages datastax:spark-cassandra-connector:2.3.1-s_2.11,commons-configuration:commons-configuration:1.10 --class org.avlasov.sparkexample.cassandra.MainCassandraExample spark-example-1.0-SNAPSHOT.jar ./ml-100k/u.data ./ml-100k/u.item ./ml-100k/u.user
```

# Apache Spark and MongoDB

Example: org.avlasov.sparkexample.mongodb.MongoDBExample
Main class: org.avlasov.sparkexample.mongodb.MainMongoDBExample

Run with spark-submit
```shell script
spark-submit --packages org.mongodb.spark:mongo-spark-connector_2.11:2.4.1 --class org.avlasov.sparkexample.mongodb.MainMongoDBExample ./spark-example/build/libs/spark-example-1.0-SNAPSHOT.jar ./data/movie-ratings.txt ./data/movie-data.txt ./data/users.txt
```

# Spark Streaming

* Analyze data streams in real time, instead of in huge batch jobs daily
* Analyzing streams of web log data to react to use behavior
* Analyze streams of real-time sensor data for "Internet of Things" stuff

![Apache Spark Streaming](https://beyondcorner.com/wp-content/uploads/2017/12/microbatch.png)

Processing of RDD's (batches of processed data) can happen in parallel on different worker nodes.

## DStreams (Discretized Streams)
![DStreams](https://d2h0cx97tjks2p.cloudfront.net/blogs/wp-content/uploads/sites/2/2017/06/apache-spark-dstream-1.jpg)

* Generates the RDD's for each time step, and can produce output at each time step.
* Can be transformed and acted on in much the same way as RDD's
* Or you can access their underlying RDD's if you need them. 

Common stateless transformations on DStream
* Map
* Flatmap
* Filter
* reduceByKey

Stateful data

You can also maintain a long-lived state on a DStream. For example - running totals, broken down by keys, or aggregating session data in web activity.

### Windowed Transformations (Windowing)

Allow you to compute results across a longer time period that your batch interval. 

Example: top sellers from the past hour. You might process data every one second (the batch interval), but maintain a window of one hour.
 
The window "slides" as time goes on, to represent batches within the window interval.
 
1. Batch interval - is how often data is captured into a DStream
2. Slide interval - is how ofter a windowed transformation is computed
3. Window interval - is how far back in time the windowed transformation goes
 
![DStream example](https://github.com/VlasovArtem/bigdata/blob/develop/image/apache-spark-dstream.png)
 
Each batch contains one second of data (the batch interval)
 
We set up a window interval of 3 seconds and a slide interval of 2 seconds
 
## Structured streaming

![Structured streaming](https://spark.apache.org/docs/latest/img/structured-streaming-example-model.png)

New data just keeps getting appended to it. Your continuous application keeps querying updated data as it comes in.

* Streaming code looks a lot like the equivalent non-streaming code.
* Structured data allows Spark to represent data more efficiently
* SQL-style queries allows for query optimization opportunities - and even better performance.
* Interoperability with other Spark components based on DataSets. MLLib is also moving toward DataSets as its primary API.
* DataSets in general is the direction Spark is moving.

## Example

### Read and parse log files throw flume to spark
1. Copy Flume configuration file ```hadoop-example/streaming-example/flume/configuration/sparkstreamingflume.conf```
2. Update Flume configuration file:
    * a1.sources.r1.spoolDir - path to the local dir
3. Run Flume Agent ```bin/flume-ng agent --conf conf --conf-file ~/Documents/flume/sparkstreamingflume.conf --name a1 -Dflume.root.logger=INFO,console```
4. Run Spark ```spark-submit --packages org.apache.spark:spark-streaming-flume_2.11:2.4.3 --class org.avlasov.sparkexample.streaming.StreamingExampleWithUrlCountMain ./spark-example/build/libs/spark-example-1.0-SNAPSHOT.jar 1 1```
5. Copy file ```hadoop-example/streaming-example/flume/access_log.txt``` into **a1.sources.r1.spoolDir**

Check results.

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
