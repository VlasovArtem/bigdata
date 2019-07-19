# Test Data
Test data can be downloaded https://grouplens.org/datasets/ (ml-100k.zip)

# Hadoop MapReduce vs Apache Spark

MapReduce vs Apache Spark Comparison Table

||MapReduce|Apache Spark|
|:--|:--:|--:|
|Data Processing|Only for Batch Processing|Batch Processing as well as Real Time Data Processing|
|Processing Speed|Slower than Apache Spark because if I/O disk latency|100x faster in memory and 10x faster while running on disk|
|Category|Data Processing Engine|Data Analytics Engine|
|Costs|Less Costlier comparing Apache Spark|More Costlier because of a large amount of RAM|
|Scalability|Both are Scalable limited to 1000 Nodes in Single Cluster|Both are Scalable limited to 1000 Nodes in Single Cluster|
|Machine Learning|MapReduce is more compatible with Apache Mahout while integrating with Machine Learning|Apache Spark have inbuilt API’s to Machine Learning|
|Compatibility|Majorly compatible with all the data sources and file formats|Apache Spark can integrate with all data sources and file formats supported by Hadoop cluster|
|Security|MapReduce framework is more secure compared to Apache Spark|Security Feature in Apache Spark is more evolving and getting matured|
|Scheduler|Dependent on external Scheduler|Apache Spark has own scheduler|
|Fault Tolerance|Uses replication for fault Tolerance|Apache Spark uses RDD and other data storage models for Fault Tolerance|
|Ease of Use|MapReduce is bit complex comparing Apache Spark because of JAVA APIs|Apache Spark is easier to use because of Rich APIs|
|Duplicate Elimination|MapReduce do not support this features|Apache Spark process every records exactly once hence eliminates duplication.|
|Language Support|Primary Language is Java but languages like C, C++, Ruby, Python, Perl, Groovy has also supported|Apache Spark Supports Java, Scala, Python and R|
|Latency|Very High Latency|Much faster comparing MapReduce Framework|
|Complexity|Difficult to write and debug codes|Easy to write and debug|
|Apache Community|Open Source Framework for processing data|Open Source Framework for processing data at a higher speed|
|Coding|More Lines of Code|Lesser lines of Code|
|Interactive Mode|Not Interactive|Interactive|
|Infrastructure|Commodity Hardware’s|Mid to High-level Hardware’s|
|SQL|Supports through Hive Query Language|Supports through Spark SQL|

# CAP 
* Consistency - all nodes see the same data at the same time
* Availability - every request gets a response (success of failure)
* Partition tolerance - system works despite of network failures

![alt text](http://java-questions.com/img/cap-theorem.png "CAP")
