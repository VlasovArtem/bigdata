ratings = LOAD '/user/maria_dev/ml-100k/u.data' AS (userID:int, movieID:int, rating:int, ratingTime:int);
metadata = LOAD '/user/maria_dev/ml-100k/u.item' USING PigStorage('|') AS (movieID:int, movieTitle:chararray);

ratingsByMovie = GROUP ratings BY movieID;

avgRatings = FOREACH ratingsByMovie GENERATE group AS movieID, AVG(ratings.rating) AS avgRating, COUNT(ratings.rating) as ratingCount;

twoStarMovies = FILTER avgRatings BY avgRating < 2.0;

twoStatsMoviesWithData = JOIN twoStarMovies BY movieID, metadata BY movieID;

mostPopularBadMovies = ORDER twoStatsMoviesWithData BY twoStarMovies::ratingCount DESC;

-- mostPopularBadMovies: {twoStarMovies::movieID: int,twoStarMovies::avgRating: double,twoStarMovies::ratingCount: long,metadata::movieID: int,metadata::movieTitle: chararray}

DESCRIBE mostPopularBadMovies;
DUMP mostPopularBadMovies;