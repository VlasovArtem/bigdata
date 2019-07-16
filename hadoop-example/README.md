# Hadoop
Is the platform the is provides distribute file system (HDFS) and distribute data processing. The hadoop is storing data in blocks in different nodes. Hadoop has NamedNode - node that contains all information about where blocks of data is store and on what node (Hadoop can contains only on NamedNode), and DataNode (multiple) - contains block of data and replicas of another DataNodes. All this and more are manages by Hadoop. Hadoop is providing MapReduce to distribute data processing.

[Hadoop File System Commands](https://hadoop.apache.org/docs/current/hadoop-project-dist/hadoop-common/FileSystemShell.html#ls)

## How to install Hadoop on Local Mac
Example for Hadoop 3.1.2

* Make sure that **brew** is installed
* Install hadoop ```brew install hadoop```
* Verify that current java version is Java 8 ```echo $(/usr/libexec/java_home --version 1.7+)```
* Configure connection to localhost via SSH
    1. Use in shell ```ssh localhost``` if this command return message **Last login:...** then you ok.
    2. Enable remote login ```sudo systemsetup -setremotelogin on``` and try to execute #1, if not ok go forward.
    3. Generate ssh keys ```ssh-keygen -t rsa```
    4. Add new key to authorized key ```cat ~/.ssh/id_rsa.pub >> ~/.ssh/authorized_keys``` try to execute #1
* Go to hadoop config folder (example ```/usr/local/Cellar/Hadoop/<version>/libexec/etc/hadoop``` or find linked path with command ```echo $(brew --prefix hadoop)```)
    * Update file hadoop-env.sh with next properties:
        * ```export JAVA_HOME=$JAVA_HOME```
        * ```export HADOOP_OPTS="-Djava.net.preferIPv4Stack=true"```
    * Update core-site.xml
    ```xml
      <configuration>
          <property>
              <name>hadoop.tmp.dir</name>
              <value>/usr/local/Cellar/hadoop/hdfs/tmp</value>
              <description>A base for other temporary directories.</description>
          </property>
          <property>
              <name>fs.default.name</name>
              <value>hdfs://localhost:8020</value>
          </property>
      </configuration>
    ```
    * Update mapred-site.xml
    ```xml
      <configuration>
          <property>
              <name>mapred.job.tracker</name>
              <value>localhost:8021</value>
          </property>
      </configuration>
    ```
    * Update hdfs-site.xml
    ```xml
      <configuration>
          <property>
              <name>dfs.replication</name>
              <value>1</value>
          </property>
      </configuration>
    ```
* Run shell command to format name node ```hdfs namenode -format```
* Launch hadoop ```/usr/local/Cellar/Hadoop/<version>/sbin/start-all.sh```
* Go to http://localhost:9870

On the web page make sure that DataNode successfully starts ```http://localhost:9870/dfshealth.html#tab-datanode```. One DataNode should be available. If is no started check logs http://localhost:9870/logs/ for datanode.
 
Possible problems
* ClusterId in NameNode and DataNodes not matching https://stackoverflow.com/questions/22316187/datanode-not-starts-correctly

## How to execute Hadoop Job
1. Build hadoop-example module - ```gradle clean build```
2. Copy jar to an HDFS
3. Run hadoop command - ```hadoop jar <path_to_the_jar> <path_to_the_movie_rating_data> <output_folder_path>```

```shell script
hadoop jar hadoop-example-1.0-SNAPSHOT.jar org.avlasov.hadoopexample.counter.words.FileWordsJob  words/ temp/ --loglevel ERROR
```

Test data can be found by the link https://grouplens.org/datasets/ (ml-100k.zip)

## Hadoop Distribute File System (HDFS)

## YARN (Yet Another Resource Negotiator)
Introduces in Hadoop 2. Separates the problem of managing resources on your cluster from MapReduce. Enabled development of MapReduce alternatives (Spark, Tez) build on top of YARN. Special scheduler that is managing recourse to run different application (MapReduce, Tez, Spark).

MapReduce, Tez, Spark -> YARN -> HDFS

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
https://hortonworks.com/tutorial/learning-the-ropes-of-the-hortonworks-sandbox/

## Apache Hive
Is translates SQL queries to MapReduce or Tez jobs on your cluster. The framework is translating SQL to the MapReduce or Tez commands. Running on the top of Hadoop YARN. SQL commands will be broken on map and reduce commands and then figure out how to execute them.

### How to install on local machine mac

Hadoop is required

* Make sure that **brew** is installed
* Install hadoop ```brew install hive```
* Verify that current java version is Java 8 ```echo $(/usr/libexec/java_home --version 1.7+)```
* Go to hive config folder (example ```/usr/local/Cellar/hive/<version>/libexec/conf``` or find linked path with command ```echo $(brew --prefix hadoop)```)
* Copy hive-default.xml.template as hive-site.xml
* Update following properties in hive-site.xml
```xml
<property>
  <name>javax.jdo.option.ConnectionURL</name>
  <value>jdbc:mysql://localhost/metastore?createDatabaseIfNotExist=true</value>
</property>

<property>
  <name>javax.jdo.option.ConnectionDriverName</name>
  <value>com.mysql.jdbc.Driver</value>
</property>

<property>
  <name>javax.jdo.option.ConnectionUserName</name>
  <value>(YOUR MYSQL USERNAME)</value>
</property>

<property>
  <name>javax.jdo.option.ConnectionPassword</name>
  <value>(YOUR MYSQL PASSWORD)</value>
</property>
```
* Download [MySQL JDBC](https://mvnrepository.com/artifact/mysql/mysql-connector-java/6.0.6) connect to the ```/usr/local/Cellar/hive/<version>/libexec/libs```
* Follows step for adding required folders to the hadoop cluster https://cwiki.apache.org/confluence/display/Hive/GettingStarted#GettingStarted-RunningHive
* Run init script for MySQL ```schematool -dbType <db type> -initSchema``` (you can add --verbose if you face with issues)
* Run Thrift server if ```hive.metastore.local = false``` in hive-site.xml ```hive --service metastore -p 9084```
* Run ```hive``` or ```beeline -u jdbc:hive2://```

Debug hive start ```hive -hiveconf hive.root.logger=DEBUG,console```

How to enable Hive transactions https://stackoverflow.com/questions/42669171/semanticexception-error-10265-while-running-simple-hive-select-query-on-a-tran

If have [problems with Timezones](https://stackoverflow.com/questions/26515700/mysql-jdbc-driver-5-1-33-time-zone-issue) during schema initialization, try to match this property in hive-site.xml
```xml
<property>
    <name>javax.jdo.option.ConnectionURL</name>
    <value>jdbc:mysql://localhost/metastore?createDatabaseIfNotExist=true&amp;useJDBCCompliantTimezoneShift=true&amp;useLegacyDatetimeCode=false&amp;serverTimezone=UTC</value>
  </property>
```

### Why Hive?
1. Use familiar SQL syntax (HiveQL)
2. Interactive
3. Scalable - works with "big data" on a cluster
4. Easy OLAP (online analytics processing) queries. More easier than writing MapReduce in Java
5. Highly optimized
6. Highly extensible
    * User defined functions
    * Thrift server
    * JDBC/ODBC driver

### Why not Hive?
1. High latency - not appropriate for OLTP (online transaction processing). Because it works with MapReduce and Tez (disk latency).
2. Stores data de-normalized
3. SQL is limited in what it can do (Pig, Spark allows more complex stuff)
4. No transactions
5. No record-level updates, inserts, deletes

Nigh latency is because Hive is working with MapReduce and Tez.

### HiveQL
Pretty much MySQL with some extensions. Some of the examples **views**. Views can store result of a query into a "view", which subsequent queries can use as a table. Allows you to specify how structured data is stored and partitioned.

### Schema on Read
Hive maintains a "metascore" that imparts a structure you define on the unstructured data that is stored on HDFS
```hiveql
CREATE TABLE ratings (
    userID INT,
    movieID INT,
    rating INT,
    ratingTime INT)
ROW FORMAT DELIMITED 
FIELDS TERMINATED BY '\t'
STORED AS TEXTFILE;

LOAD DATA LOCAL INPATH './ml-100k/u.data' 
OVERWRITE INTO TABLE ratings;
```

**/home/hive/ml-100l/u.data** - complete path to the file

**LOAD DATA** - moves data from a distribute filesystem into Hive
**LOAD DATA LOCAL** - copies data from local filesystem into Hive

Create external table
```hiveql
CREATE EXTERNAL TABLE IF NOT EXISTS ratings (
    userID INT,
    movieID INT,
    rating INT,
    ratingTime INT)
ROW FORMAT DELIMITED FIELDS TERMINATED BY '\t'
LOCATION '/data/ml-100k/u.data';
```
In this case if ratings table will be dropped, than data will still live in the file **/data/ml-100k/u.data**. Without **EXTERNAL** attribute will be removed completely on drop table.

### Partitioning
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

### Ways to use Hive
1. Interactive via hive> prompt / CLI
2. Save query files ```hive -f /somefolder/query.hql```
3. Through Ambari/Hue
4. Through JDBC/ODBC server
5. Through Thrift service (but Hive is not suitable for OLTP)
6. Via Oozie

### Connection on Hadoop cluster

```beeline -u jdbc:hive2://localhost:10000 -n maria_dev```
Enter as hive user (superuser in Hive)
```beeline -u jdbc:hive2://localhost:10000 -n hive -p hadoop```

```hive```

### Examples
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

Show create schema
```shell script
show create table ratings
```

Create Table with transaction properties
```shell script
CREATE TABLE ratings ( userID INT, movieID INT, rating INT, ratingTime INT) STORED AS ORC TBLPROPERTIES ("transactional"="true");
```

External Table (or table type **EXTERNAL_TABLE**). Hive shell find **Table Type:**
```
DESC FORMATTED <table_name>
```

Select Grant for the table
```
show grant on table test;
```
Select grant for the table and user
```
show grant user hive on table test;
```

Create table and copy data between tables
```hiveql
CREATE TABLE movieNamesTemp (movieID INT, name String, releaseDate String) ROW FORMAT DELIMITED FIELDS TERMINATED BY '|' STORED AS TEXTFILE;
CREATE TABLE movieNames (movieID INT, name String, releaseDate String);
LOAD DATA LOCAL INPATH '/home/hive/ml-100k/u.item' OVERWRITE INTO TABLE movieNamesTemp;
insert overwrite table movieNames select * from movieNamesTemp;
```

### Configure Hive

[Website](https://docs.hortonworks.com/HDPDocuments/HDP3/HDP-3.1.0/integrating-hive/content/hive_visualizing_hive_data_using_superset.html)
[PDF](https://docs.hortonworks.com/HDPDocuments/HDP3/HDP-3.0.1/integrating-hive/hive_integrating_hive_and_bi.pdf)

#### Hot wo get Hive link for Superset
Example of the link
```hive://hive@c7402:10000/default```
Where **c7402** is name node, name of the node can be found with help command line:
1. Connect to Hadoop via SSH
2. Enter as root ```su root```
3. Enter as hdfs super user ```su - hdfs```
4. Run hdfs report ```hdfs dfsadmin -report```
5. Find name of the live nodes in the report (in my case was 172.18.0.2:50010, I trimmed port and get **172.18.0.2**)

Probably it can be found in **Ambari** in YARN Heatmap tab.

# Apache Ranger
Security framework for different application on Hadoop. For example for Hive

Possible URL http://localhost:6080/index.html#!/policymanager/resource
 
Admin password on Hortonworks

* login: admin
* password: hortonworks1

# Materials
https://sundog-education.com/hadoop-materials/

# Hortonworks links and passwords
HDP 3.0.2

|Name|Login|Password|
|---|---|---|
|mysql|root|hortonworks1|
|Ambari Admin|admin|Configure with script **admin-password-reset**|
|Ambari maria_dev|maria_dev|maria_dev|

|Link|Description|
|---|---|
|http://sandbox-hdp.hortonworks.com:8088|Job Tracker|
 
