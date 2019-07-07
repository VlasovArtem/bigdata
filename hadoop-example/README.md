# Hadoop
Is the platform the is provides distribute file system (HDFS) and distribute data processing. The hadoop is storing data in blocks in different nodes. Hadoop has NamedNode - node that contains all information about where blocks of data is store and on what node (Hadoop can contains only on NamedNode), and DataNode (multiple) - contains block of data and replicas of another DataNodes. All this and more are manages by Hadoop. Hadoop is providing MapReduce to distribute data processing.

## How to execute Hadoop Job
1. Build hadoop-example module - ```gradle clean build```
2. Copy jar to an HDFS
3. Run hadoop command - ```hadoop jar <path_to_the_jar> <path_to_the_movie_rating_data> <output_folder_path>```

```shell script
hadoop jar hadoop-example-1.0-SNAPSHOT.jar org.avlasov.hadoopexample.counter.words.FileWordsJob  words/ temp/ --loglevel ERROR
```

Test data can be found by the link https://grouplens.org/datasets/ (ml-100k.zip)

## Hadoop Distribute File System (HDFS)

## Integrating Hadoop with real databases

![alt text](http://java-questions.com/img/cap-theorem.png "CAP")

### SQL
Integrating with MySQL. Can be used for OLTP (online transaction processing)

#### Sqoop
Sqoop will help to integrate MySQL to Hadoop. He is actually kicks MapReduce jobs to handle importing and exporting data.

##### Import data from Sqoop -> ...

```sqoop import --connect jdbc:mysql://localhost/movielens --driver com.mysql.jdbc.Driver --table movies``` - import table movies from MySQL to HDFS. This command will create bunch of file on a Hadoop server.

```sqoop import --connect jdbc:mysql://localhost/movielens --driver com.mysql.jdbc.Driver --table movies --hive-import``` - import table movies from MySQL to Hive.

Sqoop support incremental imports. It means, that you can keep your relational database and Hadoop in sync.
* --check-column
* --last-value

##### Export data from ... -> Sqoop
```sqoop export --connect jdbc:mysql://localhost/movielens -m 1 --driver com.mysql.jdbc.Driver --table movies --export-dir /apps/hive/warehouse/movies --input-fields-terminated-by '\0001''``` - export data from HDFS to MySQL. Where:
* -m 1 - number of mappers that will work on exporting data. For local Hadoop cluster is explicit. Would not be used on real cluster.
* --table movies - table in MySQL must already exists with column in expected order.
* --export-dir /apps/hive/warehouse/movies - actual folder where hive is storing data.
* --input-fields-terminated-by '\0001' - delimiter for the input fields in Hive file

##### Examples
Test data http://media.sundog-soft.com/hadoop/movielens.sql

Prepare MySQL
```mysql
SET NAMES 'utf8';

SET CHARACTER SET utf8;

source movielens.sql

GRANT ALL PRIVILEGES ON movielens.* to ''@'localhost';
```

Import data from MySQL to HDFS
```shell script
sqoop import --connect jdbc:mysql://localhost/movielens --driver com.mysql.jdbc.Driver --table movies -m 1
```
Import data from MySQL to Hive
```shell script
sqoop import --connect jdbc:mysql://localhost/movielens --driver com.mysql.jdbc.Driver --table movies -m 1 --hive-import
```
Import from Hive to MySQL

```shell script
sqoop export --connect jdbc:mysql://localhost/movielens -m 1 --driver com.mysql.jdbc.Driver --table exported_movies --export-dir /apps/hive/warehouse/movies --input-fields-terminated-by '\0001'
```

### NoSQL
You can use NoSQL when de-normalization is good for you. For example your application has two SQL tables with users and orders, and you always collect this information in one request, it will be better if you get this information without searching thought the whole tables and join them together, but store this information in one place.  

Do you really need SQL?
1. Your high-transaction queries are probably pretty simple once de-normalized
2. A simple get/put API may meet your needs
3. Looking up values for a given key is simple, fast, and scalable
4. You can do both...

#### Apache HBase
https://hbase.apache.org/
Documentation - https://hbase.apache.org/book.html
Non-relational, scalable database build on HDFS
Based on Google's BigTable - https://static.googleusercontent.com/media/research.google.com/en//archive/bigtable-osdi06.pdf

Provides on CRUD operations, there is not query language
* Create
* Read
* Update
* Delete

But those operations are very quick in large scale.
HBase is divided into multiple Region Servers, the server contains information and can scale if number of information is growing. It can automatically distribute data between multiple Region Servers. Region Servers are build over HDFS. Master mind of HBase cluster is HMaster, it is stores information about Region Server and data are stored and where. Zookeeper is tracking down how now is HMaster if something goes wrong and one HMaster is down.

##### Data model
1. Fast access to any given **ROW**
2. A **ROW** is referenced by a unique **KEY**
3. Each **ROW** has some small number of **COLUMN FAMILIES**
4. A **COLUMN FAMILY** may contains arbitrary **COLUMNS**
5. You can have a very large number of **COLUMNS** in a **COLUMN FAMILY**
6. Each **CELL** can have many **VERSIONS** with given timestamp
7. Sparse data is A-OK - missing column is a row consume no storage

##### Access to HBase
* HBase shell
* Java API
* Spark, Hive, Pig
* REST service
* Thrift service
* Avro service

##### HBase REST
Create a HBase table for movie ratings group by user

Java Client -> REST Service -> HBase + HDFS

Run HBase REST daemon on Hadoop cluster.
First you need to start HBase service (it can be done with help of Ambari)
Make sure that port 8000 is forwarded
```shell script
/usr/hdp/current/hbase-master/bin/hbase-daemon.sh start rest -p 8000 --infoport 8001
```
https://hbase.apache.org/1.2/apidocs/org/apache/hadoop/hbase/rest/package-summary.html

Run with argument for path to movie ratings data: org.avlasov.hadoopexample.datastoresexample.hbase.HBaseExample.main

Stop REST
```shell script
/usr/hdp/current/hbase-master/bin/hbase-daemon.sh stop rest
```

##### HBase with PIG integration
Pig with get data from HDFS and pull it to the HBase

1. Must create HBase table ahead of time
2. Your relation must have a unique key as its first column, followed by subsequent columns as you want them save in HBase
3. USING clause allows you to STORE into an HBase table
4. Can work at scale -  HBAse is transactional on rows

Connect to HBase shell
```shell script
hbase shell
```

Drop table, please note, that table should be disabled at first 
```shell script
disable 'table_nane'
```
```shell script
drop 'table_name'
```
Create table with following column families
```shell script
create 'users','userinfo'
```
Where 'users' is table name, and 'userinfo' is column family.

Example pig script
```pig
users = LOAD '/user/maria_dev/ml-100k/u.user' 
USING PigStorage('|') 
AS (userID:int, age:int, gender:chararray, occupation:chararray, zip:int);

STORE users INTO 'hbase://users' 
USING org.apache.pig.backend.hadoop.hbase.HBaseStorage (
'userinfo:age,userinfo:gender,userinfo:occupation,userinfo:zip');
```
WHere **userID** will be unique key of the row
userinfo:age - userinfo COLUMN FAMILY age COLUMN NAME

Run Pig script
```shell script
pig hbase.pig
```
Show all columns in the table **users** (connect to hbase shell)
```
scan 'users'
```
Find user by userID (unique identifier of the ROW, ROW is top part of HBase table)
```
get 'users', '99'
```
Get data from table 'users' with unique identifier '99'

Find user by userID and show only specific column
```
get 'users', '99', {COLUMN => 'userinfo:age'}
```
Get data from table 'users' with unique identifier '99' and show only specified column 'userinfo:age'

#### Cassandra
A distributed database with no single point of failure.
Unlike HBase, there is not master node at all - every node runs exactly the same software and performs the same functions.

Data model is similar to BigTable/HBase

It is non-relational database, but has a limiter CQL query language as its interface.

The CAP theorem says you can only have 2 of 3: consistency, availability, partition-tolerance. But partition-tolerance is a requirement with "Big Data", so you really need to choose between consistency and availability.

Cassandra favors availability over consistency. 

* It is eventually consistent. You will not receive data right away, it can take time to store it and return to you.  
* But you can specify your consistency requirements as a part of your request. So really is tunable consistency.

Consistency - if you write something into database you will receive this value back no matter what.
Partition-tolerance - database can be split up and can be used on different clusters.

##### CQL
Has no JOINs
All queries must on some primary key.
All tables must be in keyspace - keyspaces is like databases
CQLSH is allow to manipulate Cassandra with help of command line

##### Cassandra and Spark
This two technologies creates great family.

* DataStax offer a Spark-Cassandra connector
* Allows you to read and write Cassandra tables as DataFrames

Use cases:
1. Use Spark for analytics on data stored in Cassandra
2. Use Spark to transform data and store it into Cassandra for transactional use

##### How to install Cassandra on Hadoop provided by Hortonworks
1. Connect to the Hadoop via SSH as root
2. Update yum ```yum update```
3. Upgrade Python from 2.6 to 2.7
    1. Install software collection utils, this utils will help to switch between versions of python ```yum install scl-utils```
    2. Install CentOS specific component, that will help to switch between versions ```yum install centos-release-scl-rh```
    3. Install python 2.7 ```yum install python27```
    4. Enable python 2.7 ```scl enable python27 bash```
4. Install Cassandra
    1. Add repo for Cassandra
        1. Go to repo directory ```cd /etc/yum.repos.d```
        2. Create new file ```vi datastax.repo```
            1. Add file data
            ```
           [datastax]
           name = DataStax Repo for Apache Cassandra
           baseurl = http://rpm.datastax.com/community
           enabled = 1
           gpgcheck = 0
            ```
    2. Install Cassandra ```yum install dsc30```
5. Install Python Pip ```yum install python-pip``` if do not go through #3
6. Install CQL shell ```pip install cqlsh```

You can start Cassandra ```service cassandra start```
For starting CQL shell with another version ```cqlsh --cqlversion="3.4.0"```

##### Example
Create keyspace 
```
CREATE KEYSPACE movielens WITH replication = {'class' : 'SimpleStrategy','replication_factor': '1'} AND durable_writes = true;
```
Use keyspace
```
USE movielens;
```
Create table
```
CREATE TABLE users (user_id int, age int, gender text, occupation text, zip text, PRIMARY KEY (user_id));
```

#### MongoDB
You can have different fields in every document if you want to. No single "key" as in other databases. But you can create indices on any fields you want, or even combinations of fields. If you want to "shard", then you must do so on some index. Results in a lot of flexibility, but with great power comes great responsibility. MongoDB has next terminology:
* Databases
* Collections
* Documents

##### Replication sets
Single-master
Maintains backup copies of your database instance. Secondaries can elect a new primary within seconds if your primary goes down. But make sure your operation log is long enough to give you time to recover the primary where it comes back. When Primary node goes down, the MongoDB decide which of secondary nodes will become temporally primary, it strongly depends on ping time from a node to a client. You need to be sure, that the primary node will be available soon, because reading data from secondaries for two much time is not recommended.

##### Sharding
Ranges of some indexed value you specify are assigned to different replica sets. Each application server has running **mongos**, which will talk to one of three **Config Servers** and then decide from what replica set get required data. mongos has balancer, that will rebalace data (in real time) in replica sets if request for most of time try to reach only one replica set. As mentioned earlier mongos required to have three Config servers, and if one of then goes down, than all DB will go down.

##### Neat things about MongoDB
* It's not just a NoSQL database - very flexible document model.
* Shell if a full JavaScript interpreter
* Support many indices
    * But only one can be used for sharding
    * More then 2-3 are still discouraged
    * Full-text indices for text search
    * Spatial indices
* Build-in aggregation capabilities, MapReduce, GridFS
    * For some application uou might not need Hadoop at all
    * But MongoDB still integrates with Hadoop, Spark, and most languages
* A SQL connector is available
    * But MongoDB still is not designed for joins and normalized data really.
    
##### How to install MongoDB on Hadoop provided by Hortonworks

1. Go to Hadoop services folder
```shell script
cd /var/lib/ambari-server/resources/stacks/HDP/<your_version>/services
```
2. Get MongoDB connector for Ambari
```shell script
git clone https://github.com/nikunjness/mongo-ambari.git
```
3. Restart Ambari
```shell script
ambari-server restart
```
4. Go to Ambari UI - 127.0.0.1:8080
5. On main page add service for MongoDB. Accept all defaults.
    * For version 2.5 Actions -> Add service -> Select MongoDB
    * For version 3.0 three dots near Services -> Add service -> Select MongoDB 
    

## YARN (Yet Another Resource Negotiator)
Manages where and how resource are manages

### Materials
#### Links
https://searchdatamanagement.techtarget.com/definition/Apache-Hadoop-YARN-Yet-Another-Resource-Negotiator

https://www.quora.com/What-is-YARN-in-Hadoop

## Apache Pig
Easy way to write mappers and reducers. Pig introduces _Pig Latin_, a scripting language that lets you use SQL-like syntax to define your map and reduce steps

Running Pig
* Grunt
* Script
* Ambari / Hue

### Pig Commands
Load data from file into relation divided by default delimiter \t
````pig
ratings = LOAD '/user/maria_dev/ml-100k/u.data' AS (userID:int, movieID:int, rating:int, ratingTime:int);
````
Load data from file into relation divided by defined delimiter (**|**)
````pig
metadata = LOAD '/user/maria_dev/ml-100k/u.item' USING PigStorage('|') AS (movieID:int, movieTitle:chararray, releaseDate:chararray, videoRelease:chararray, imdbLink:chararray);
````
Create new relation from another
```pig
nameLookup = FOREACH metadata GENERATE movieID, movieTitle, ToUnixTime(ToDate(releaseDate, 'dd-MMM-yyyy')) as releaseTime;
```
Create bag of relations (Grouping)
```pig
ratingsByMovie = GROUP ratings BY movieID;

-- (group: int, ratings : {(userID:int, movieID:int, rating:int, ratingTime:int), ...})
```
Filtering
```pig
fiveStarMovies = FILTER avgRatings BY avgRating > 4.0;
```
Join
```pig
fiveStatsWithData = JOIN fiveStarMovies BY movieID, nameLookup BY movieID;

--fiveStatsWithData: {fiveStarMovies::movieID: int,fiveStarMovies::avgRating: double,nameLookup::movieID: int,nameLookup::movieTitle: chararray,nameLookup::releaseTime: long}
```
Order
```pig
oldestFiveStarMovies = ORDER fiveStatsWithData BY nameLookup::releaseTime;
```
Show data
```pig
DUMP data;
```
Describe relation
```pig
DESCRIBE relation;
```
Store, default delimiter is **tab** 
```pig
STORE ratings INTO 'outRatings' USING PigStorage(':');
```

Full list of Pig Latin:

Operations
* **LOAD** - load from disk 
* **STORE** - write to a disk
* **DUMP** - show in the console
* **FILTER** - filter data 
* **DISTINCT** - unique value 
* **FOREACH/GENERATE** - create new relation from another, go from line at a time 
* **MAPREDUCE** 
* **STREAM** 
* **SAMPLE** - create random sample from relation
* **JOIN** - join two relations
* **COGROUP** - variation of join, but have different structure
```
COGROUP - fiveStatsWithData: {group: int,fiveStarMovies: {(movieID: int,avgRating: double)},nameLookup: {(movieID: int,movieTitle: chararray,releaseTime: long)}}
JOIN - fiveStatsWithData: {fiveStarMovies::movieID: int,fiveStarMovies::avgRating: double,nameLookup::movieID: int,nameLookup::movieTitle: chararray,nameLookup::releaseTime: long}
```
* **GROUP** - group/reduce relation by key
* **CROSS** - Cartesian product
* **CUBE**
* **ORDER** - sort in order 
* **RANK** - do not change order, but adding rank to an each row 
* **LIMIT** - limit result
* **UNION** - squashes two relations
* **SPLIT** - split one relation into multiple

Diagnostics
* **DESCRIBE** - describe relation structure/schema
* **EXPLAIN** - explain of query
* **ILLUSTRATE** - Additional information of

UDF (User Defined functions)
* **REGISTER** - register jar file
* **DEFINE** - define name for functions
* **IMPORT** - import macros

Other function
* **AVG** - average value
* **CONCAT** - concatenate two columns
* **COUNT** - count rows
* **MAX** - max value in a relation
* **MIN** - min value in a relation
* **SIZE** - size of a relation
* **SUM** - sum of values in column

Loaders 
* **PigStorage** - divide loaded data by delimiter
* **TextLoader** - load one line per row
* **JsonLoader** - json
* **AvroStorage** - format serialization/deserialization (schema, splittable)
* **ParguetLoader** - column oriented data format
* **OrcStorage** - Compressed format
* **HBaseStorage** - NoSQL data (part of Hadoop)

### Tez
Pig request optimizer

### Materials
#### Books
http://shop.oreilly.com/product/0636920044383.do
### Examples
./pig/most-popular-bad-movies.pig

./pig/oldest-five-rating-movie.pig

## Udemy Course
https://www.udemy.com/the-ultimate-hands-on-hadoop-tame-your-big-data

## Links
http://localhost:19888/jobhistory/app - Hadoop job history
Hortonworks Sandbox for Hadoop - https://www.cloudera.com/downloads/hortonworks-sandbox/hdp.html

# Apache Hive
Is translates SQL queries to MapReduce or Tez jobs on your cluster. The framework is translating SQL to the MapReduce or Tez commands. Running on the top of Hadoop YARN. SQL commands will be broken on map and reduce commands and then figure out how to execute them.
## Why Hive?
1. Use familiar SQL syntax (HiveQL)
2. Interactive
3. Scalable - works with "big data" on a cluster
4. Easy OLAP (online analytics processing) queries. More easier than writing MapReduce in Java
5. Highly optimized
6. Highly extensible
    * User defined functions
    * Thrift server
    * JDBC/ODBC driver
## Why not Hive?
1. High latency - not appropriate for OLTP (online transaction processing). Because it works with MapReduce and Tez (disk latency).
2. Stores data de-normalized
3. SQL is limited in what it can do (Pig, Spark allows more complex stuff)
4. No transactions
5. No record-level updates, inserts, deletes

Nigh latency is because Hive is working with MapReduce and Tez.

## HiveQL
Pretty much MySQL with some extensions. Some of the examples **views**. Views can store result of a query into a "view", which subsequent queries can use as a table. Allows you to specify how structured data is stored and partitioned.

## Schema on Read
Hive maintains a "metascore" that imparts a structure you define on the unstructured data that is stored on HDFS
```hiveql
CREATE TABLE ratings (
    userID INT,
    movieID INT,
    rating INT,
    time INT)
ROW FORMAT DELIMITED 
FIELDS TERMINATED BY '\t'
STORED AS TEXTFILE;

LOAD DATA LOCAL INPATH '${env:HOME}/ml-100k/u.data'
OVERWRITE INTO TABLE ratings;
```
**LOAD DATA** - moves data from a distribute filesystem into Hive
**LOAD DATA LOCAL** - copies data from local filesystem into Hive

Create external table
```hiveql
CREATE EXTERNAL TABLE IF NOT EXISTS ratings (
    userID INT,
    movieID INT,
    rating INT,
    time INT)
ROW FORMAT DELIMITED FIELDS TERMINATED BY '\t'
LOCATION '/data/ml-100k/u.data';
```
In this case if ratings table will be dropped, than data will still live in the file **/data/ml-100k/u.data**. Without **EXTERNAL** attribute will be removed completely on drop table.

## Partitioning
You can store your data in partitioned subdirectories. Give huge optimization if your queries are only on certain partitions.
```hiveql
CREATE TABLE customers (
    name STRING,
    address STRUCT<street:STRING, city:STRING, state:STRING, zip:INT>
)
PARTITIONED BY (country STRING);
```
.../customers/country=CA/
.../customers/country=GB/

## Ways to use Hive
1. Interactive via hive> prompt / CLI
2. Save query files ```hive -f /somefolder/query.hql```
3. Through Ambari/Hue
4. Through JDBC/ODBC server
5. Through Thrift service (but Hive is not suitable for OLTP)
6. Via Oozie

## Connection on Hadoop cluster
beeline -u jdbc:hive2://localhost:10000 -n maria_dev

## Examples
All sample data can be found in https://grouplens.org/datasets/ (ml-100k.zip)

Find most rated movie with rating count more than 10 times.
```hiveql
SELECT mr.name, r.movie_id, avg(r.rating) as avgRating, count(r.rating) as ratingCount
	FROM ratings r 
	JOIN movie_names mr ON r.movie_id = mr.movie_id
	GROUP BY r.movie_id, mr.name
	HAVING ratingCount > 10
	ORDER BY avgRating DESC
	LIMIT 1;
```

Find top 10 most rated movies
```hiveql
CREATE VIEW topMovieIDs AS
	SELECT movie_id, count(movie_id) as ratingCount
		FROM ratings
		GROUP by movie_id
		ORDER BY ratingCount DESC;

SELECT mn.name, ratingCount
	FROM topMovieIDs t JOIN movie_names mn ON t.movie_id = mn.movie_id
	LIMIT 10;

DROP VIEW topMovieIDs;
```

# Ambari

# Materials
https://sundog-education.com/hadoop-materials/

 
