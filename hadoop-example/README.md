# Hadoop
Is the platform the is provides distribute file system (HDFS) and distribute data processing. The hadoop is storing data in blocks in different nodes. Hadoop has NamedNode - node that contains all information about where blocks of data is store and on what node (Hadoop can contains only on NamedNode), and DataNode (multiple) - contains block of data and replicas of another DataNodes. All this and more are manages by Hadoop. Hadoop is providing MapReduce to distribute data processing 

## Hadoop Distribute File System (HDFS)

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

# Ambari

# Hot execute Hadoop Job
1. Build hadoop-example module - ```gradle clean build```
2. Copy jar to an HDFS
3. Run hadoop command - ```hadoop jar <path_to_the_jar> <path_to_the_movie_rating_data> <output_folder_path>```

Test data can be found by the link https://grouplens.org/datasets/ (ml-100k.zip)

# Materials
https://sundog-education.com/hadoop-materials/

 
