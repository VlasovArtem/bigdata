ratings = LOAD '/user/maria_dev/ml-100k/u.data' AS (userID:int, movieID:int, rating:int, ratingTime:int);
metadata = LOAD '/user/maria_dev/ml-100k/u.item' USING PigStorage('|') AS (movieID:int, movieTitle:chararray, releaseDate:chararray, videoRelease:chararray, imdbLink:chararray);

--DUMP metadata; - show conent of relation.

nameLookup = FOREACH metadata GENERATE movieID, movieTitle, ToUnixTime(ToDate(releaseDate, 'dd-MMM-yyyy')) as releaseTime;

-- (movieID, movieTitle, releaseTime)

--Create bag of relations grouped by movieID
ratingsByMovie = GROUP ratings BY movieID;

-- (group: int, ratings : {(userID:int, movieID:int, rating:int, ratingTime:int), ...})

--DUMP ratingsByMovie;

avgRatings = FOREACH ratingsByMovie GENERATE group AS movieID, AVG(ratings.rating) AS avgRating;

--DUMP avgRatings;

-- (movieID, avgRating)

--DESCRIBE ratings;
--DESCRIBE ratingsByMovie;
--DESCRIBE avgRating;

fiveStarMovies = FILTER avgRatings BY avgRating > 4.0;

fiveStatsWithData = JOIN fiveStarMovies BY movieID, nameLookup BY movieID;

--fiveStatsWithData: {fiveStarMovies::movieID: int,fiveStarMovies::avgRating: double,nameLookup::movieID: int,nameLookup::movieTitle: chararray,nameLookup::releaseTime: long}

--DESCRIBE fiveStatsWithData;
--DUMP fiveStatsWithData;

oldestFiveStarMovies = ORDER fiveStatsWithData BY nameLookup::releaseTime;


DUMP  oldestFiveStarMovies;