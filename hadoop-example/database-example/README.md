# Integrating Hadoop with real databases

![alt text](http://java-questions.com/img/cap-theorem.png "CAP")

## SQL
Integrating with MySQL. Can be used for OLTP (online transaction processing)

### Sqoop
Sqoop will help to integrate MySQL to Hadoop. He is actually kicks MapReduce jobs to handle importing and exporting data.

#### Import data from Sqoop -> ...

```sqoop import --connect jdbc:mysql://localhost/movielens --driver com.mysql.jdbc.Driver --table movies``` - import table movies from MySQL to HDFS. This command will create bunch of file on a Hadoop server.

```sqoop import --connect jdbc:mysql://localhost/movielens --driver com.mysql.jdbc.Driver --table movies --hive-import``` - import table movies from MySQL to Hive.

Sqoop support incremental imports. It means, that you can keep your relational database and Hadoop in sync.
* --check-column
* --last-value

#### Export data from ... -> Sqoop
```sqoop export --connect jdbc:mysql://localhost/movielens -m 1 --driver com.mysql.jdbc.Driver --table movies --export-dir /apps/hive/warehouse/movies --input-fields-terminated-by '\0001''``` - export data from HDFS to MySQL. Where:
* -m 1 - number of mappers that will work on exporting data. For local Hadoop cluster is explicit. Would not be used on real cluster.
* --table movies - table in MySQL must already exists with column in expected order.
* --export-dir /apps/hive/warehouse/movies - actual folder where hive is storing data.
* --input-fields-terminated-by '\0001' - delimiter for the input fields in Hive file

#### Examples
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

## NoSQL
You can use NoSQL when de-normalization is good for you. For example your application has two SQL tables with users and orders, and you always collect this information in one request, it will be better if you get this information without searching thought the whole tables and join them together, but store this information in one place.  

Do you really need SQL?
1. Your high-transaction queries are probably pretty simple once de-normalized
2. A simple get/put API may meet your needs
3. Looking up values for a given key is simple, fast, and scalable
4. You can do both...

### Apache HBase
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

#### Data model
1. Fast access to any given **ROW**
2. A **ROW** is referenced by a unique **KEY**
3. Each **ROW** has some small number of **COLUMN FAMILIES**
4. A **COLUMN FAMILY** may contains arbitrary **COLUMNS**
5. You can have a very large number of **COLUMNS** in a **COLUMN FAMILY**
6. Each **CELL** can have many **VERSIONS** with given timestamp
7. Sparse data is A-OK - missing column is a row consume no storage

#### Access to HBase
* HBase shell
* Java API
* Spark, Hive, Pig
* REST service
* Thrift service
* Avro service

Enter HBase shell ```hbase shell```

#### HBase REST
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

#### HBase with PIG integration
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

### Cassandra
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

#### CQL
Has no JOINs
All queries must on some primary key.
All tables must be in keyspace - keyspaces is like databases
CQLSH is allow to manipulate Cassandra with help of command line

#### Cassandra and Spark
This two technologies creates great family.

* DataStax offer a Spark-Cassandra connector
* Allows you to read and write Cassandra tables as DataFrames

Use cases:
1. Use Spark for analytics on data stored in Cassandra
2. Use Spark to transform data and store it into Cassandra for transactional use

#### How to install Cassandra on Hadoop provided by Hortonworks
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

#### Example
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

### MongoDB
You can have different fields in every document if you want to. No single "key" as in other databases. But you can create indices on any fields you want, or even combinations of fields. If you want to "shard", then you must do so on some index. Results in a lot of flexibility, but with great power comes great responsibility. MongoDB has next terminology:
* Databases
* Collections
* Documents

#### Replication sets
Single-master
Maintains backup copies of your database instance. Secondaries can elect a new primary within seconds if your primary goes down. But make sure your operation log is long enough to give you time to recover the primary where it comes back. When Primary node goes down, the MongoDB decide which of secondary nodes will become temporally primary, it strongly depends on ping time from a node to a client. You need to be sure, that the primary node will be available soon, because reading data from secondaries for two much time is not recommended.

#### Sharding
Ranges of some indexed value you specify are assigned to different replica sets. Each application server has running **mongos**, which will talk to one of three **Config Servers** and then decide from what replica set get required data. mongos has balancer, that will rebalace data (in real time) in replica sets if request for most of time try to reach only one replica set. As mentioned earlier mongos required to have three Config servers, and if one of then goes down, than all DB will go down.

#### Neat things about MongoDB
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
    
#### How to install MongoDB on Hadoop provided by Hortonworks

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
    
#### Examples
Add index for the field
```
db.users.createIndex({userId : 1})
```
Aggregate
```
db.users.aggregate([ { $group: {_id: { occupation: "$occupation"}, avgAge : { $avg: "$age" }}}])
```
    g