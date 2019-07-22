# Apache Kafka
Publish/Subscribe Messaging system. 

Kafka servers store all incoming messages from _publisher_ from some period of time, and _publishes_ them to a stream of data called a _topic_. 

Kafka _consumers_ subscribe to one or more topics, and receive data as its is published. A stream/topic can have many different consumers, all with their own position in the stream maintained.

It is not just for Hadoop.

![Kafka Architecture](https://d2h0cx97tjks2p.cloudfront.net/blogs/wp-content/uploads/sites/2/2018/04/Apache-Kafka-Cluster-1.png)
![Kafka Architecture](https://d2h0cx97tjks2p.cloudfront.net/blogs/wp-content/uploads/sites/2/2018/04/Kafka-Architecture.png)

Kafka itself may be distributed among many processes on many servers. It will distribute the storage of stream data as well.

Consumer may also be distributed. Consumers of the same group will have messages distributed amongst them. Consumers of different groups will get their own copy of each message.

Start zookeeper server
```zookeeper-server-start config/zookeeper.properties``` - with brew
```./bin/zkServer config/zookeeper.properties```

Update ```config/server.properties``` for kafka
* listeners=PLAINTEXT://localhost:9092

Start kafka server
```kafka-server-start config/server.properties``` - with brew
```./bin/kafka-server-start.sh config/server.properties``` 

## Examples

### Simple console producer and consumer example

Go to kafka bin folder ```/usr/hdp/current/kafka-broker/bin```

Create new topic
```
./kafka-topics.sh --create --zookeeper sandbox-hdp.hortonworks.com:2181 --replication-factor 1 --partitions 1 --topic fred
```

Get list of topics
```
./kafka-topics.sh --list --zookeeper sandbox-hdp.hortonworks.com:2181
```
 
Create new producer, that will listen to the standard output (console) and put this data into the topic
```
./kafka-console-producer.sh --broker-list sandbox-hdp.hortonworks.com:6667 --topic fred
```

Create consumer from the standard output (console) and read all from the beginning of the topic
```
./kafka-console-consumer.sh --bootstrap-server sandbox-hdp.hortonworks.com:6667 --topic fred --from-beginning
```

If **--from-beginning** is not setup, then consumer will read only new messages

### Reading data from the file example

* Copy next files from ```/usr/hdp/current/kafka-broker/conf``` to any other folder:
    1. connect-file-sink.properties
    2. connect-file-source.properties
    3. connect-standalone.properties

* Modify **connect-standalone.properties** if it is required
    1. bootstrap.servers = **sandbox-hdp.hortonworks.com:6667**

* Modify **connect-file-sink.properties** - configuration where and how information will be stored
    1. file = **/home/maria_dev/logout.txt**
    2. topics = **log-test**

* Modify **connect-file-source.properties**
    1. file= **/home/maria_dev/access_log_small.txt**
    2. topics = **log-test**

* Download example of access log file ```wget http://media.sundog-soft.com/hadoop/access_log_small.txt``` or take it from the example folder ```./streaming-example/kafka/access_log_small.txt```

* Go to kafka bin folder ```/usr/hdp/current/kafka-broker/bin```

* Connect to standalone with configuration from the first four steps ```./connect-standalone.sh ~/kafka-connectors-configuration/connect-standalone.properties ~/kafka-connectors-configuration/connect-file-source.properties ~/kafka-connectors-configuration/connect-file-sink.properties```

* Create consumer ```./kafka-console-consumer.sh --bootstrap-server sandbox-hdp.hortonworks.com:6667 --topic log-test```

### Spark and Kafka example

1. Start zookeeper server ```./bin/zkServer config/zookeeper.properties```
2. Set kafka listener in **config/server.properties** ```listeners=PLAINTEXT://localhost:9092```
3. Start kafka server ```./bin/kafka-server-start.sh config/server.properties```
4. Create kafka topics
    * ```./kafka-topics.sh --create --zookeeper sandbox-hdp.hortonworks.com:2181 --replication-factor 1 --partitions 1 --topic test```
    * ```./kafka-topics.sh --create --zookeeper sandbox-hdp.hortonworks.com:2181 --replication-factor 1 --partitions 1 --topic Ex```
5. Build fat jar ```mvn clean assembly:assembly```
6. Run Kafka Producer ```java -jar target/spark-app-1.0-SNAPSHOT-jar-with-dependencies.jar "localhost:9092" test```
    * Where:
        * **"localhost:9092"** is bootstrap sever from step #2
        * **test** is name of the topic
    * To check topic data, run simple console consumer ```kafka-console-consumer --bootstrap-server localhost:9092 --topic test```
7. Run Spark ```spark-submit --packages "org.apache.spark:spark-streaming-kafka-0-10_2.11:2.4.3","org.apache.kafka:kafka-clients:2.3.0" --class org.avlasov.kafka.SparkKafkaMain target/spark-app-1.0-SNAPSHOT.jar "localhost:9092" test Ex```
    * Where:
        * **"localhost:9092"** is bootstrap sever from step #2
        * **test** is name of the consumer topic
        * **Ex** is name of the Spark producer topic
    * To check topic data, run simple console consumer ```kafka-console-consumer --bootstrap-server localhost:9092 --topic Ex```

# Apache Flume
Made from the start with Hadoop in mind. Has build-in sinks for HDFS and HBase. Originally made to handle log aggregation. 

![](https://flume.apache.org/_images/DevGuide_image00.png)

Components of an agent
1. Source
    * Where data is coming from
    * Can optionally have Channel Selectors and Interceptors
2. Channel
    * How the data is transferred (via memory or files)
3. Sink
    * Where the data is going
    * Can be organized into Sink Groups
    * A sink can connect to only one chanel
        * Channel is notified to delete a message once the sink processes it

Using Avro, agents can connect to other agents.

Thins of Flume as a buffer between your data and your cluster.

Configuration examples: 
* ```hadoop-example/streaming-example/flume/configuration/example.conf```
* ```hadoop-example/streaming-example/flume/configuration/flumelogs.conf```

Flume removed from HDP 3.0

[Flume sink types](https://data-flair.training/blogs/flume-sink/)

## Examples

### Reading data from netcat to log
Install Flume via brew ```brew install flume``` on local Mac

1. Go to the flume install folder (for example /usr/local/Cellar/flume/1.9.0/libexec/)
2. Run flume agent ```bin/flume-ng agent --conf conf --conf-file ~/git/bigdata/hadoop-example/streaming-example/flume/configuration/example.conf --name a1 -Dflume.root.logger=INFO,console```
3. Start netcat in other windows ```nc localhost 44444```

You can start typing into netcat and then check windows with flume, this window will show data from netcat.

### Reading data from 
Run Hadoop or use HDP

Update file ```hadoop-example/streaming-example/flume/configuration/flumelogs.conf```
* a1.sources.r1.spoolDir - path to the local dir
* a1.sinks.k1.hdfs.path - path on HDFS

Run flume agent ```bin/flume-ng agent --conf conf --conf-file ~/Documents/flume/flumelogs.conf --name a1 -Dflume.root.logger=INFO,console```

Upload file into **a1.sources.r1.spoolDir** and check if it's available in HDFS (a1.sinks.k1.hdfs.path) with required name (check configurations)

# Apache Storm

https://storm.apache.org/

Another framework for processing continuous streams of data on a cluster. Can run on top of YARN. Works on individual events, not micro-batches (like Spark Streaming does).

A _stream_ consists of _tuples_ that flow through to system. _Spouts_ that are sources of stream data (Kafka, Twitter, etc.). _Bolts_ that process stream data as it's received (Transform, aggregate, write to database/HDFS). A _topology_ is a graph of spouts and bolts process your stream.

![Apache Bolt](https://2xbbhjxc6wk3v21p62t8n4d4-wpengine.netdna-ssl.com/wp-content/uploads/2018/03/storm-100_topology.png)

![Architecture](https://andyleonard.blog/wp-content/uploads/2018/01/StormArchitectureAWSSlide-1024x560.jpg)

Developing storm application can be developed with Storm Core or Trident

## Start on local environment
1. Start zookeeper
2. Start nimbus `./bin/storm nimbus`
3. Start supervisor `./bin/storm supervisor`
4. (Optional) Start Storm UI `./bin storm ui`
    * Check `http://localhost:8080/`
5. Execute topology wordcount `./bin/storm jar examples/storm-starter/target/storm-starter-2.0.0.jar org.apache.storm.starter.WordCountTopology wordcount`
6. Check Apache Storm UI `http://localhost:8080/`, that this UI contains topology `wordcount`

Kill topology `./bin/storm kill wordcount`

# Apache Flink

Another stream processing engine. Most similar to Storm. Con run on standalone cluster, or on top of YARN or Mesos. Highly scalable. Fault-tolerant

**Flink vs. Spark Streaming vs. Storm**

1. Flink is faster than Storm
2. Flink offers "real streaming" link Storm (but if you are using Trident with Storm, you are using micro-batches)
3. Flink offers a higher-level API like Trident or Spark, but while still doing real-time streaming
4. Flink has good Scala support, like Spark Streaming
5. Flink has an ecosystem of its own, like Spark 
6. Flink can process data based on event times, not when data was received
    * Impressive windowing system
    * This plus real-time streaming and exactly-once semantics is important for financial applications.
    
![Apache Flink Architecture](https://static.packt-cdn.com/products/9781786466228/graphics/image_01_002.jpg)

**Connectors**

HDFS, Cassandra, Kafka, Elasticsearch, NiFi, Redis, RabbitMQ

**Stark flink cluster**

1. Download or install Apache Flink
2. Go to bin folder
3. Run `start-cluster.sh`
4. Check UI `http://localhost:8081`

Run Flink example

For example [Socket Word Count](https://github.com/apache/flink/blob/master/flink-examples/flink-examples-streaming/src/main/java/org/apache/flink/streaming/examples/socket/SocketWindowWordCount.java)

You can find example in archive of the app.

1. Run netcat `nc -l 9000`
2. Run Flink `./bin/flink run examples/streaming/SocketWindowWordCount.jar`
3. Check UI `http://localhost:8081/#/overview` that it has running jobs (job name: Socket Window WordCount)
4. Input words in netcat window
5. Check logs for input `./log` (*.out)