# Impala
Cloudera's alternative to Hive. Massive parallel SQL engine on Hadoop. Impala is always running, so you avoid the start-up costs when starting a Hive query (Made for BI-style queries). Bottom line: Impala is often faster than Hive, but Hive offers more versatility (variety).

# Accumulo
Another BigTable (from Google) clone (like HBAse). It offers a better security model (Cell-based access control) and server-side programming. Consider it for your NoSQL needs if you have complex security requirements, but make sure the systems that need to read this data can talk to it. 

# Redis
A distributed in-memory data store (like memcache), but it is more than cache. Good support for storing data structures, can persist data to disk, can be used as a data store and not just a cache. Popular caching layer for web apps.

# Ignite
An "in-memory data fabric". Sort of alternative to Redis, but it closer to database: ACID guarantees, SQL support.

# Elasticsearch
A distributed document search and analytics engine. Can handle things like real-time search-as-you-type.

# Amazon Ecosystem
Amazon Kinesis is basically the AWS version of Kafka.

Other Amazon products:
* Elastic MapReduce (EMR)
* S3
* Elasticsearch Service / CloudSearch
* DynamoDB
* Amazon RDS
* ElastiCache
* AI / Machine Learning service

EMR is particular is an easy way to spin up a Hadoop cluster on demand.

# Apache NiFi
Directed graphs of data routing (can connect to Kafka, HDFS, Hive). Web UI for designing complex systems. Often seen in the complex of IoT sensors, and managing their data. Relevant in that it can be a streaming data source you'll see. 

# Apache Falcon
A "data governance engine" that sits on top of Oozie. Like NiFi, it allows construction of data processing graphs, but it is meant to organize the flow of your data within Hadoop.

![Apache Falcon](https://2xbbhjxc6wk3v21p62t8n4d4-wpengine.netdna-ssl.com/wp-content/uploads/2014/03/falc1.png)

# Apache Slider
Deployment tool for apps on a YARN cluster. Allows monitoring of your apps. Allow growing or shrinking your development as it is running. Manages mixed configurations. Start/Stop applications on your cluster. Incubating.