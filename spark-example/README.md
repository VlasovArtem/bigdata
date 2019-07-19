# Apache Spark
"A fast and general engine for large-scale data processing"

https://spark.apache.org/

This framework is using Cluster Manager (Spark, YARN) to define on which cluster to run his program. Key to performance is Cache. Is memory store solution.
"Run programs up to 100x faster than Hadoop MapReduce in memory, or 10x faster on disk."
DAG engine (directed acyclic graph) optimizes workflows (like TEZ for Pig).

Spark supports two types of shared variables: broadcast variables, which can be used to cache a value in memory on all nodes, and accumulators, which are variables that are only “added” to, such as counters and sums.

## RDD
Resilient Distributed Dataset

A Fault-Tolerant Abstraction for In-Memory Cluster Computing

RDD - is a collection of elements partitioned across the nodes of the cluster that can be operated on in parallel.

There are two ways to create RDDs: parallelizing an existing collection in your driver program, or referencing a dataset in an external storage system, such as a shared filesystem, HDFS, HBase, or any data source offering a Hadoop InputFormat.

RDDs support two types of operations: transformations, which create a new dataset from an existing one, and actions, which return a value to the driver program after running a computation on the dataset.

```JavaRDD<String> lines = sc.textFile("data.txt");``` - in this case dataset is not loaded in memory, is merely a pointer to the file.
```JavaRDD<Integer> lineLengths = lines.map(s -> s.length());``` - map will not performed right away. It happens due to laziness. (transformation operation)
```int totalLength = lineLengths.reduce((a, b) -> a + b);``` - final operation (action operation)

At this point Spark breaks the computation into tasks to run on separate machines, and each machine runs both its part of the map and a local reduction, returning only its answer to the driver program.

If you want to user lines later, then you can store this data in memory ```lineLengths.persist(StorageLevel.MEMORY_ONLY());```

http://spark.apache.org/docs/latest/rdd-programming-guide.html

### The SparkContext
* Created by your driver program
* Is responsible for making RDD's resilient and distributed
* Creates RDD
* The spark shell creates a "sc" object for you

## Spark SQL
http://spark.apache.org/docs/latest/sql-programming-guide.html

Extends RDD to a "DataFrame" object.
DataFrame is DataSet of Row objects (in Spark 2.0)
Works over Spark Thrift Server
It is possible to define custom function (UDF - User-defines functions) and use them in SQL queries/

A Dataset is a distributed collection of data.

A DataFrame is a Dataset organized into named columns.

In the Scala API, DataFrame is simply a type alias of Dataset[Row]

DataFrame:
1. Contain Row objects
2. Can run SQL queries
3. Has a schema (leading to more efficient storage)
4. Read and write to JSON, Hive, parquet
5. Communicates with JDBC/ODBC, Tableau

1. What is SqlContext and how it differ from SparkContext?
    * SQLContext is your gateway to SparkSQL. SQLContext has access to DataSet and DataFrame
2. How to produce DataFrame from different data sources - existent RDD, JSON, Parquete, JDBC, etc.?
    * It can be produce thought SQLContext and DataFrameReader (sqlContext.read()) 
3. What are the main DataFrame operations available?
    * join, select, groupBy, avg, count 
4. How to use SQL to query data frame objects?
    * sqlContext.sql()
5. How to control metainformation (schema) of dataframes?
    * sqlContext.createDataFrame() from Java class or StructType class
6. What are the main advantages of parquete serialization format from other supported types?
    * Is column oriented data storage format
7. Is it possible to use UDFs (User Defined Function) with DataFrames API?
    * sqlContext.udf().register()

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

# Run spark

1. Start master ```sbin/start-master.sh```
2. Find master url. 
    * You can find in spark master logs. For example: Starting Spark master at spark://avlasov-mac.local:7077
    * Or on spark UI. Default path is http://localhost:8080. For example: URL: spark://avlasov-mac.local:7077
3. Run slave (worker) ```sbin/start-slave.sh spark://avlasov-mac.local:7077```

# Questions

What are the ways to construct RDD instances from different data sources (in-memory collections, files on different places and formats)
What the SparkContext is?
What’s the difference between actions and transformations?
What are the most generic transformations?
How executable tasks are passed to the Spark API routines? Why it’s important to keep an eye on closures and outer context variables captured by them?
What is the difference between local and cluster execution modes? How to understand which one you are actually using?
How to dump elements of RDD onto the screen?
How Key-Value data is supported by SparkAPI? How it is different from regular RDD[T] collections. What are additional transformations and actions provided?
What are the means Spark provides to handle global or shared state?
How to format and assemble Spark programs and what is “job submission” procedure?
What the “shuffle” is and why, generically, you should avoid shuffles on large RDDs?
Is it possible to unit-test Spark API applications?

# Pratice

## User Visits
Use next dataset ```spark-example/data/uservisits``` with next data format, that is separated by **,**
* sourceIP VARCHAR(116)
* destURL VARCHAR(100)
* visitDate DATE
* adRevenue FLOAT
* userAgent VARCHAR(256)
* countryCode CHAR(3)
* languageCode CHAR(6)
* searchWord VARCHAR(32)
* duration INT

Output top 10 countries from which visits occurred with aggregated number of visits

### In spark shell
```
val userVisitsDirectoryRDD = sc.wholeTextFiles("/Users/artemvlasov/git/bigdata/spark-example/data/uservisits")
val lines = userVisitsDirectoryRDD.map(v1 => v1._2)
val userVisits = lines.flatMap(line => line.split("\n").toIterator)
val userVisitCountryCodes = userVisits.map(userVisit => userVisit.split(",")(5))
val countryCodeTuple = userVisitCountryCodes.map(countryCode => (countryCode, 1))
val countryCodeCount = countryCodeTuple.reduceByKey((v1, v2) => v1 + v2)
val topVisitCountries = countryCodeCount.sortBy(data => data._2, false, 1).take(10)
topVisitCountries(0)
```

topVisitCountries(0) - should return **(CHE,290)**

# Run spark on multiple node using Virtual Box

1. Install CentOS 7 on VirtualBox using this tutorial https://progressive-code.com/post/16/Create-a-minimal-CentOS-7-Virtual-Box-image-for-Java-application-deployment
2. Connect to VirtualBox via SSH.
    * Run shell command ```ip addr``` find ```inet```
    * Connect to ssh on local machine ```ssh root@<ip_config_from_inet>```
3. Download java ```sudo yum install java-1.8.0-openjdk-devel```
    * Verify java ```java -version```
4. Go to opt folder ```cd /opt```
5. Download Spark ```wget http://www-eu.apache.org/dist/spark/spark-2.4.3/spark-2.4.3-bin-hadoop2.7.tgz```
    * Unzip archive ```tar -xzf ./spark-2.4.3-bin-hadoop2.7.tgz```
    * Create soft link ```ln -s /opt/spark-2.4.3-bin-hadoop2.7 /opt/spark```
6. Run spark master on local machine
    * Find local machine ip
        * Command ```ifconfig```
        * Network MacOS
    * Start master ```<spark_folder>/sbin/start-master.sh -h <ip_address>```
7. Run spark slave on VirtualBox ```<spark_path/sbin/start-slave.sh <master_path>```
    * Go to ```http://localhost:8080``` to find master path
8. Check ```http://localhost:8080```, that new worker is available