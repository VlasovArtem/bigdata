# Apache Tez
Directed Acyclic Graph Framework. It makes your Hive, Pig, or MapReduce jobs faster. Constructs Directed Acyclic Graphs (DAGs) for more efficient processing of distributed jobs. Optimizes physical data flow and resource usage. Apache Tez is running over YARN.

Verifying executing time for the next query on MapReduce and Tez (Hive 3.0 is using Tez by default and cannot be changed to MapReduce)
```hiveql
DROP VIEW IF EXISTS topMovieIDs;

CREATE VIEW topMovieIDs AS
	SELECT movieID, count(movieID) as ratingCount
		FROM movielens.ratings
		GROUP by movieID
		ORDER BY ratingCount DESC;

SELECT mn.name, ratingCount
	FROM topMovieIDs t JOIN movienames mn ON t.movieID = mn.movieID;

DROP VIEW topMovieIDs;
```

# Apache Mesos
The alternative to YARN. Came out of Twitter. It is the system that manages resources across your data centers. It can allocate resources of web servers, small scripts, etc, and not just big data. Meant to solve more general problem then YARN. Mesos is not really part of the Hadoop ecosystem, but it can work nicely with it. Spark and Storm may both run on Mesos instead of YARN. Hadoop YARN my be integrated with Mesos using Myriad.

Difference between Apache Mesos and YARN
* YARN is a monolithic scheduler. You give it a job, and YARN figures out where to run. 
* Mesos is a two-tiered system. Mesos just makes offers of resource to you application. Your framework decides whether to accept or reject them. You also decide your own scheduling algorithm.
* YARN is optimized fo long, analytical jobs like you see in Hadoop.
* Mesos is build to handle that, as well as long-lived processes and short-lived processes as well. 

# Apache ZooKeeper
It basically keeps track of information that must be synchronized across your cluster.
- Which node is the master?
- What tasks are assigned to which workers?
- Which workers are currently available?

It is a tool that applications can use to recover from partial failures in your cluster. An integral part of HBase, High-Availability (HA) MapReduce, Drill, Storm, Solr and much more.

## Failure modes
* Master crashes, needs to fail over to a backup
* Worker crashes - its work needs to be redistributed
* Network trouble - part of your cluster can't see the rest of it

## Primitive operations in a distributed system
* Master election
    * One node registered itself as a master, and holds a "lock" on that data
    * Other nodes cannot become master until that lock is released
    * Only one node allowed to hold the lock at a time
* Crash detection
    * "Ephemeral" data on a node's availability automatically goes away if the node disconnects, or fails to refresh itself after some time-out period
* Group management
* Metadata
    * List of outstanding tasks, task assignments
    
## Znode
Znode is "file" that manages by ZooKeeper. Znode store information about master and workers.
* Persistent znodes remain stored until explicitly deleted. For example: assignment of tasks to workers must persist even if master crashes.
* Ephemeral znodes go away if the client that create it crashes or loses its connection to ZooKeeper. For example: if the master crashes, it should release its lock on the znode that indicates which node is the master.

## Architecture
Each Master or Worker has its own **ZooKeeper Client**. All ZK Clients have a list of ZooKeepers servers to connect to.
![zookeeper architecture](https://developer.ibm.com/developer/tutorials/bd-zookeeper/images/fig01.png)

## Connection
You can connect to ZooKeeper via ssh using zkCli.sh ```/usr/hdp/current/zookeeper-client/bin/zkCli.sh```.

Create ephemeral znode ```create -e /testmaster "127.0.0.1:2223```, this znode exits until current session is available.

Get znode ```get /testmaster```

Quit ZooKeeper ```quit```, please note that the znode that was create as ephemeral will be deleted.

# Apache Oozie
Orchestrating of Hadoop jobs. A system for running and scheduling Hadoop tasks.

A multi-stage Hadoop job. Might chain together MapReduce, Hive, Pig, sqoop, and disctp tasks, Other systems available via add-ons (like Spark).

A workflow is a Directed Acyclic Graph of actions. This actions are specified via XML, and can be run in parallel.

## Workflow
![Oozie Workflow](https://dmhnzl5mp9mj6.cloudfront.net/bigdata_awsblog/images/OozieImage1a.PNG)
Example in xml
```xml
<?xml version="1.0" encoding="UTF-8" ?>
<workflow-app xmlns="uri:oozie:workflow:0.2" name="top-movies">
    <start to="map-reduce"/>

    <action name="map-reduce">
        <map-reduce>
            configuration
        </map-reduce>
        <ok to="fork-node" />
        <error to="kill" />
    </action>

    <fork name="fork-node">
        <path start="spark-job"/>
        <path start="hive-query"/>
    </fork>

    <action name="spark-job">
        <spark xmlns="uri:oozie:spark-action:0.2"/>
        <ok to="joining"/>
        <error to="kill" />
    </action>

    <action name="hive-query">
        <hive xmlns="uri:oozie:hive-action:0.2"/>
        <ok to="joining"/>
        <error to="kill" />
    </action>

    <join name="joining" to="pig-job"/>

    <action name="pig-job">
        <pig/>
        <ok to="end"/>
        <error to="kill" />
    </action>

    <kill name="kill">
        <message>Workflow failed with error message [${wf:errorMessage(wf:lastErrorNode())}]
        </message>
    </kill>

    <end name="end" />

</workflow-app>
```

Set Up workflow
1. Make sure each action works on its own
2. Make a directory in HDFS for your job
3. Create your workflow.xml file and put it in your HDFS folder
4. Create job.properties defining any variable your workflow.xml needs
    * This goes on your local filesystem where you will launch the job from
    * You could also set these properties within your XML.
        * nameNode = hdfs://sandbox.hortonworks.com:8020
        * jobTracker = hdfs://sandbox.hortonworks.com:8050
        * queueName = default
        * oozie.use.system.libpath = true
        * oozie.wf.application.path = ${nameNode}/user/maria_dev
        
Running workflow
```oozie job --oozie http://localhost:11000/oozie -config /home/maria_dev/job.properties -run```

Monitor progress via http ```http://127.0.0.1:11000/oozie```

## Oozie Coordinators
* Schedules workflow execution
* Launches workflows based on a given start time and frequency
* Will also wait for required input data to become available
* Run in exactly the same way as workflow

```xml
<coordinator-app xmlns="uri:oozie:coordinator:0.2" name="sample coordinator" frequency="5 * * * *" start="2020-00-18T01:00Z" end="2025-00-18T01:00Z" timezone="America/Los_Angeles">
    <controls>
        <timeout>1</timeout>
        <concurrency>1</concurrency>
        <execution>FIFO</execution>
        <throttle>1</throttle>
    </controls>
    <action>
        <workflow>
            <app-path>pathof_workflow_xml/workflow.xml</app-path>
        </workflow>
    </action>
</coordinator-app>
```

## Oozie Bundles
New in Oozie 3.0. A bundle is a collection of coordinators that can be managed together. Example: you may have a bunch of coordinators for processing log data in various ways. By grouping them in a bundle, youc could suspend them all if there were some problem with log collection.

## Example
Run SQL script on MySQL on Hadoop cluster:
1. ```set names 'utf8;'```
2. ```set character set utf8;```
3. ```create database movielens;```
4. ```use movielens;```
5. ```source movielens.sql``` you can find the script by the path ./oozie-example/movielens.sql

Check use in MySQL for execution in workflow.xml.
1. You can use "empty" user (without providing **-u** attribute)
    * Check if "empty" user is exists ```SELECT User from mysql.user;```
        * If it not exits ```CREATE user ''@'localhost'; ```
2. Use any existing user with/without password

Update workflow.xml according to a user selection, for example:

Execution without user 
```<command>import --connect jdbc:mysql://localhost/movielens --driver com.mysql.jdbc.Driver --table movies -m 1</command>```
 
Execution with user that has no password 
```<command>import --connect jdbc:mysql://localhost/movielens --driver com.mysql.jdbc.Driver --table movies -m 1</command> -u user```

Execution with user that has password 
```<command>import --connect jdbc:mysql://localhost/movielens --driver com.mysql.jdbc.Driver --table movies -m 1</command> -u user -p password```

Download next file on the Hadoop cluster
1. ```./oozie-example/oldmovies.sql``` 
2. ```./oozie-example/workflow.xml```
2. ```./oozie-example/job.properties```

Do required changes in the file job.properties, according ot your Hadoop cluster configuration.

Put next files on HDFS
1. ```hadoop fs -put workflow.xml /user/maria_dev```
2. ```hadoop fs -put oldmovies.sql /user/maria_dev```
3. ```hadoop fs -put /usr/share/java/mysql-connector-java.jar /user/oozie/share/lib/<find_your_folder>/sqoop``` (do it from oozie user ```su oozie```)

Restart oozie via Amabri or with help of shell command.

Run oozie job ```oozie job --oozie http://localhost:11000/oozie -config /home/maria_dev/job.properties -run```, where **-config** path is path on the Hadoop cluster (local) not HDFS.

Go to ```http://localhost:11000/oozie``` and check result of oozie job. Unique identifier of the job returned by **oozie job** command.

If you issue with find **jobTracker** property value, then search for **yarn.resourcemanager.address** under yarn-site.xml

Check if user **hive** have all permissions on the folder where you will store data

[Example for Apache Ranger](https://cwiki.apache.org/confluence/display/RANGER/Apache+Ranger+0.5+-+User+Guide#ApacheRanger0.5-UserGuide-AddingHIVEpolicies)

If UI is not working
1. Stop oozie
2. Download ext library http://archive.cloudera.com/gplextras/misc/ext-2.2.zip
3. Move it to the folder ```/usr/hdp/current/oozie-client/libext/``` on Hadoop Local
4. Regenerate oozie war 
5. Start oozie
