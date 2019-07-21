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