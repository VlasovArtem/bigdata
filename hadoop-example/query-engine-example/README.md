# Apache Drill

[Apache Drill](https://drill.apache.org/)

A SQL query engine for variety non-relational databases and data files:
* Hive, HBase, MongoDB
* Flat JSON, Parquet files on HDFS, S3, Azure, Google Cloud, local file system

Based on Google Dremel. It is real SQL, and not SQL-like. It has ODBC/JDBC driver, so other tools can connect to it like to any other relational database. You need to remember, that it still has non-relational database under the hood. Allows SQL analysis of disparate data source without having to transform and load it first. Internally data is represented as JSON and so has no fixed schema.

## Install Drill

* Connect to Hadoop Cluster
* Enter as root
```shell script
su root
```
* Download archive from https://drill.apache.org/download/
```shell script
wget http://www.apache.org/dyn/closer.cgi/drill/drill-1.16.0/apache-drill-1.16.0.tar.gz
```
* Decomprese archive
```shell script
tar -xvf apache-drill-1.16.0.tar.gz
```
* Run Drill
```shell script
./apache-drill-1.16.0/bin/drillbit.sh start -Ddrill.exec.http.port=8765
```
Or for older versions
```shell script
 ./apache-drill-1.12.0/bin/drillbit.sh start -Ddrill.exec.port=8765
```
* Enter the web UI **http://localhost:8765**
* Or user drill shell 
```
./apache-drill-1.16.0/bin/drill-conf
``` 

If you have problems with executing queries for Hive, try to check create table request in hive shell ```show create table <table_name>```. Make sure, that this table has property ```'transactional_properties'='default'```. If not, then try to create table with the next create table query 
```hiveql
CREATE TABLE ratings ( userID INT, movieID INT, rating INT, ratingTime INT) STORED AS ORC TBLPROPERTIES ("transactional"="true");
```

Possible solution to load data to ORC table https://stackoverflow.com/questions/21721251/loading-data-from-a-txt-file-to-table-stored-as-orc-in-hive

Connect Hive and MongoDb
1. Go to the Drill web UI **http://localhost:8765**
2. Enter **Storage** tab
3. Enable **Hive** and **MongoDB** (Do not forget to connect start drill sh as **root** user)

Update Drill Hive Plugin configuration (click **Update** for Hive plugin on Storage page) and update with following json:
```json
{
  "type": "hive",
  "configProps": {
    "hive.metastore.uris": "thrift://localhost:9083",
    "javax.jdo.option.ConnectionURL": "jdbc:derby:;databaseName=../sample-data/drill_hive_db;create=true",
    "hive.metastore.warehouse.dir": "/tmp/drill_hive_wh",
    "fs.default.name": "file:///",
    "hive.metastore.sasl.enabled": "false",
    "hive.metastore.schema.verification": "false",
    "datanucleus.schema.autoCreateAll": "true"
  },
  "enabled": true
}
```

## Integration with MongoDB and Hive

### Prepare databases
#### Hive
* Enter Hive command line ```hive```
* Create database ```create database movielens;```
* Create rating table 
```hiveql
CREATE TABLE ratings (userID int, movieID int, rating int, ratingTime int) 
ROW FORMAT DELIMITED 
FIELDS TERMINATED BY '\t' 
STORED AS TEXTFILE;
``` 
* Upload data
```hiveql
LOAD DATA LOCAL INPATH './ml-100k/u.data' OVERWRITE INTO TABLE ratings;
```
Do remember, that when you login into hive console you will login as **hive** user, and file should be located in /home/hive/ml-100k/u.data and have permission to read by this user.

#### MongoDB
Run shell script with jar file from project **spark-example**

```shell script
spark-submit --packages org.mongodb.spark:mongo-spark-connector_2.11:2.4.1 --class org.avlasov.sparkexample.mongodb.MainMongoDBExample ./spark-example/build/libs/spark-example-1.0-SNAPSHOT.jar ./data/movie-ratings.txt ./data/movie-data.txt ./data/users.txt
```

### Example

Select from Hive
```sql
SELECT * FROM hive.movielens.ratings LIMIT 10;
```
Select From MongoDB
```sql
SELECT * FROM mongo.movielens.users LIMIT 10;
```

Join between Hive and MongoDB
```sql
SELECT u.occupation, COUNT(*) FROM hive.movielens.ratings r JOIN mongo.movielens.users u ON r.userid = u.userId GROUP BY u.occupation
```

# Apache Phoenix
[Apache Phoenix](https://phoenix.apache.org/)

A SQL driver for HBase that supports transactions. Fast, low-latency - OLTP support. Originally developed by Salesforce, then open-sourced. Expose a JDBC connector for HBase. Supports secondary indices and user-defined functions. Integrates with MapReduce, Spark, Hive, Pig, and Flume. 

It's really fast.

Using Phoenix
1. CLI
2. Java API
3. JDBC Driver
4. Phoenix Query Server
5. Jar's for MapReduce, Hive, Pig, Flume, Spark

## CLI
[Apache Phoenix Language](https://phoenix.apache.org/language/index.html)
Install on Hortonworks
```yum install phoenix```

Phoenix is installed under the folder ```/usr/hdp/current/phoenix-client/```

Run shell ```python ./bin/sqlline.py```
Create Table
```sql
CREATE TABLE IF NOT EXISTS us_population(state CHAR(2) NOT NULL, city VARCHAR NOT NULL, population BIGINT CONSTRAINT my_pk PRIMARY KEY (state,city));
```

Insert Data
```
UPSERT into US_POPULATION VALUES('NY', 'New York', 8143197)
```

Show tables ```!tables```

## Pig
* Enter phoenix shell ``python ./bin/sqlline.py``
* Create table 
```sql
CREATE TABLE users(userid INTEGER NOT NULL, age INTEGER, gender CHAR(1), occupation VARCHAR, zip VARCHAR CONSTRAINT pk PRIMARY KEY(userid));
```
* Create pig script
```pig
REGISTER /usr/hdp/current/phoenix-client/phoenix-client.jar

users = LOAD '/user/maria_dev/ml-100k/u.user' 
USING PigStorage('|') 
AS (USERID:int, AGE:int, GENDER:chararray, OCCUPATION:chararray, ZIP:chararray);

STORE users into 'hbase://users' using
    org.apache.phoenix.pig.PhoenixHBaseStorage('localhost','-batchSize 5000');

occupations = load 'hbase://table/users/USERID,OCCUPATION' using org.apache.phoenix.pig.PhoenixHBaseLoader('localhost');

grpd = GROUP occupations BY OCCUPATION; 
cnt = FOREACH grpd GENERATE group AS OCCUPATION,COUNT(occupations);
DUMP cnt;
```
Or find file under ./hadoop-example/pig/phoenix.pig
* Run pig script ```pig phoenix.pig```
Note that in HBase int values and column names will be stored as HEX. For example ``\x80\x00\x00\x01`` where only last hex is value or number of the column x01 = 1
[Converter](https://www.rapidtables.com/convert/number/ascii-hex-bin-dec-converter.html)

# Presto

It's lot like Drill. It can connect to many different "big data" databases and data stores at once, and query across them. Familiar SQL syntax. Optimized for OLAP (online analytics processing) - analytical queries, data warehousing. Developer, and still partially maintained by Facebook. Exposes JDBC, Command-line, and Tableau interface.
Big benefit against Drill it has connector to Cassandra. Facebook use this technology to querying data throw their huge amount of data. A single Presto query can combine data from multiple sources, allowing for analytics across your entire organization. Presto breaks the false choice between having fast analytics using an expensive commercial solution or using a slow "free" solution that requires excessive hardware.

It can connect to:
* Cassandra
* Hive
* MongoDB
* MySQL
* Local files
* And other stuff, such as: Kafka, JMX, PostgresSQL, Redis, Accumulo

## Install on Hortonworks
1. Connect to your Hadoop environment via SSH as root user.
2. Download presto server from [Presto Website](http://prestodb.github.io/download.html) ```wget https://repo1.maven.org/maven2/com/facebook/presto/presto-server/0.221/presto-server-0.221.tar.gz```
3. Uncompressed it ```tar -xvf ./presto-server-0.221.tar.gz```
4. Enter presto server directory ```cd presto-server-0.221```
5. Add presto configuration for connectors [Connectors Configuration](#connectors-configuration)
6. Install CLI for presto [Follow the steps](http://prestodb.github.io/docs/current/installation/cli.html)
8. Launch server ```./bin/launcher start```
7. Check UI ```http://127.0.0.1:8090/ui/```

To run presto CLI (for example for Hive) run next shell command ```./bin/presto --server 127.0.0.1:8090 --catalog hive```

## Connectors Configuration 
Examples are stored under the folder ./hadoop-example/query-engine-example/presto-hdp-configs

You need to copy **etc** folder inside the presto server folder.

Set up, under the file node.properties:node.id - it should be unique id for any cluster.

[Example tar URL](http://media.sundog-soft.com/hadoop/presto-hdp-config.tgz)

### Cassandra Connector
Find example how to install cassandra on Hortonworks in ./hadoop-example/database-example/README.md (Cassandra paragraph)
1. Start Cassandra ```service cassandra start```
2. Enable thrift on Cassandra ```nodetool enablethrift```
3. Copy of create Cassandra connector catalog ```<presto_folder>/etc/catalog/cassandra.properties``` (find example in ```./presto-hdp-config/etc/catalog/cassandra.properties```)
4. Connect to presto and check ```./bin/presto --server 127.0.0.1:8090 --catalog cassandra```

## Examples
Show table from schema
```sql
SHOW TABLES FROM default;
```

Join tables from Cassandra and Hive

Connect to presto shell ``````./bin/presto --server 127.0.0.1:8090 --catalog hive,cassandra``````

Show schemas from catalog ```show schemas from cassandra;```

Join two catalogs ```select * from cassandra.movielens.users cu join hive.movielens.ratings hr ON cu.user_id = hr.userId;```

## Possible problems with presto 0.221
If a table created with help of hive (Hive 3.1.0.3.0.1.0-187) and stored as ORC, then Presto is NOT able to read data from this table.
If a table created with help of hive (Hive 3.1.0.3.0.1.0-187) and stored as textfile, then Presto is NOT able to read data from this table and show next error
```
Query 20190708_223555_00013_87y4w failed: Your client does not appear to support insert-only tables. To skip capability checks, please set metastore.client.capability.check to false. This setting can be set globally, or on the client for the current metastore session. Note that this may lead to incorrect results, data loss, undefined behavior, etc. if your client is actually incompatible. You can also specify custom client capabilities via get_table_req API.
```
If a table created as EXTERNAL with help of hive (Hive 3.1.0.3.0.1.0-187), then Presto is able to read data from this table, but cannot write
```
Query 20190708_225741_00023_87y4w failed: Cannot write to non-managed Hive table
```
If a table create with help of presto (Presto 0.221), then Presto is able to read data from this table, please note that this table is stored as ORC. Data inserted from Hive will not be available from Presto. If data will be inserted from Presto, then is will be available from Presto, but whole table will not be available from Hive. 
 
Probably problem is in configuration and structure of the tables created by Presto and Hive.

Structure of the HDFS files for a table create via Hive

/warehouse/tablespace/managed/hive/movielens.db/ratings/delta_0000001_0000001_0000/bucket_00000

Structure of the HDFS files for a table create via Presto

/warehouse/tablespace/managed/hive/movielens.db/ratings_presto/20190709_174556_00055_ciedj_d88a9d98-98db-467f-881f-88a01c5c0a93