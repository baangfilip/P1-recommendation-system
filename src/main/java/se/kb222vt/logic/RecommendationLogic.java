package se.kb222vt.logic;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import se.kb222vt.app.Application;
import se.kb222vt.entities.MovieEntity;
import se.kb222vt.entities.RecommendationEntity;
import se.kb222vt.entities.UserEntity;

public class RecommendationLogic {
	
	/**
	 * Get recommendation for movies for user using euclidean or pearson similarity measure. Will not include movies the user has already seen.
	 * @param user to get recommendations for
	 * @param measure what measure to use for similarity
	 * @param minRatings how many ratings a movie must at least have
	 * @return RecommendationEntity
	 * @throws Exception 
	 */
	public RecommendationEntity userRec(UserEntity user, String measure, int minRatings, int maxResults) throws Exception {
		
		
		long startTime = System.currentTimeMillis();//for metadata
		int cacheSizeBefore = user.getCacheSize(measure);//for metadata
		
		SortedMap<Double, UserEntity> similarUsers = findSimilarUsers(user, measure);
		long similarityTime = (System.currentTimeMillis() - startTime);//for metadata
		
		//got similar persons and their similarity
		ArrayList<MovieEntity> movieRec = getWeightedScoresForUnwatchedMovies(similarUsers, getUnwatchedMovies(user), minRatings);
		RecommendationEntity rec = new RecommendationEntity(user);
		
		//sort movie recommendations
		movieRec.sort(MovieEntity.getMovieByRecommendationScore());
		int maxResult = movieRec.size() > maxResults ? maxResults : movieRec.size();
		rec.setRecommendedMovies(movieRec.subList(0, maxResult));
		rec.setSimilarUsers(similarUsers);
		long recommendationTime = (System.currentTimeMillis() - startTime);//for metadata
		rec.addMetaData("Cached user similarities before starting for this measure", new Integer(cacheSizeBefore));
		rec.addMetaData("Similarity time (ms)", new Long(similarityTime));
		rec.addMetaData("Recommendation time (ms)", new Long(recommendationTime));
		return rec;
	}
		
	/**
	 * Compare two users and the ratings the gave movies that they both watched with pearson correlation
	 * @param user
	 * @param anotherUser
	 * @return the sum of the pearson correlation of their ratings on the same movies
	 */
	private double pearsonCorrelationUsers(UserEntity user, UserEntity anotherUser) {
		double sum1 = 0, sum2 = 0, sum1sq = 0, sum2sq = 0, pSum = 0;
		int matchingMovies = 0;
		HashMap<Integer, Double> anotherUserRatings = anotherUser.getRatedMovies();
		for(Map.Entry<Integer, Double> entry : user.getRatedMovies().entrySet()) {
			int movieId = entry.getKey();
			double rating = entry.getValue();
			if(anotherUserRatings.containsKey(movieId)) { //anotherUser have rated this movie
				sum1 += rating;
				sum2 += anotherUserRatings.get(movieId);
				sum1sq += Math.pow(rating, 2);
				sum2sq += Math.pow(anotherUserRatings.get(movieId), 2);
				pSum += rating * anotherUserRatings.get(movieId);
				matchingMovies++;
			}else {
				continue; //anotherUser haven't rated the same movie as user
			}
		}
		if(matchingMovies < 1) {
			return 0;
		}
		double num = pSum - (sum1 * sum2 / matchingMovies);
		double den = Math.sqrt((sum1sq - Math.pow(sum1, 2) / matchingMovies) * (sum2sq - Math.pow(sum2, 2) / matchingMovies));
		return num/den;
	}
	
	
	/**
	 * Find similar users by calculating pearson or using euclidean distance between other users that have watched the same movies
	 * @param user find similar users to user
	 * @param measure use pearson or euclidean
	 * @return a SortedMap with users sorted descending, the most similar user first
	 * @throws Exception 
	 */
	private SortedMap<Double, UserEntity> findSimilarUsers(UserEntity user, String measure) throws Exception {
		SortedMap<Double, UserEntity> similarUsers = new TreeMap<Double, UserEntity>().descendingMap();
		measure = measure.toLowerCase();
		if(user.getCacheSize(measure) == Application.getUsers().size()) {
			//all similarities for this measure and user are already cached, just return
			return user.getSortedCache(measure);
		}
		for(UserEntity u : Application.getUsers().values()) {
			if(u.equals(user))
				continue;
			double similarity = user.getSimilarityFromCache(u.getUserID(), measure);
			//check and see if the users similarity for this measure is in cache
			if(similarity == -1) {
				//similarity not found in cache, 1. find 2. add to cache
				switch(measure) {
					case "euclidean":
						similarity = euclideanDistanceUsers(user, u);
						break;
					case "pearson":
						similarity = pearsonCorrelationUsers(user, u);
						break;
					default: 
						throw new Exception("Measure is not supported");
						
				}
				//add similarity to cache for both users..
				cacheSimilarity(measure, similarity, u, user);
			}

			//System.out.println(user.getUserName() + " is " + similarity + "s to: " + u.getUserName());
			if(similarity > 0) {
				similarUsers.put(similarity, u);
			}
		}
		return similarUsers;
	}
	/**
	 * Compare two users and the ratings the gave movies that they both watched with euclidean distance
	 * @param user
	 * @param anotherUser
	 * @return the sum of the euclidean distance of their ratings on the same movies
	 */
	private double euclideanDistanceUsers(UserEntity user, UserEntity anotherUser) {
		double similarity = 0;
		int matchingMovies = 0;
		HashMap<Integer, Double> anotherUserRatings = anotherUser.getRatedMovies();
		for(Map.Entry<Integer, Double> entry : user.getRatedMovies().entrySet()) {
			int movieId = entry.getKey();
			double rating = entry.getValue();
			if(anotherUserRatings.containsKey(movieId)) { //anotherUser have rated this movie
				double movieRatingOther = anotherUserRatings.get(movieId);
				//System.out.println("Both " + user.getUserName() + " and " + anotherUser.getUserName() + " have watched " + title);
				similarity += Math.pow(rating - movieRatingOther, 2);
				matchingMovies++;
			}else {
				continue; //anotherUser haven't rated the same movie as user
			}
		}
		if(matchingMovies < 1) {
			return 0;
		}
		return 1 / (1 + similarity);
	}
	
	/**
	 * Get movie recommendations for unwatchedMovies based on similarity by similarUsers.
	 * @param similarUsers
	 * @param unwatchedMovies
	 * @return a sortedMap<"RecommendationScore", MovieEntity> with the highest recommended movie first
	 */
	private ArrayList<MovieEntity> getWeightedScoresForUnwatchedMovies(SortedMap<Double, UserEntity> similarUsers, ArrayList<MovieEntity> unwatchedMovies, int minRatings) {
		HashMap<Integer, Double> weightedMovies = new HashMap<Integer, Double>();
		ArrayList<MovieEntity> movieRec = new ArrayList<MovieEntity>();
		//System.out.println("SimilarUsers: " + similarUsers.size());
		//System.out.println("UnwatchedMovies: " + unwatchedMovies.size());
		
		for(MovieEntity movie : unwatchedMovies) {
			if(movie.getNbrOfRatings() < minRatings) {
				//System.out.println("This movie doesnt have enough ratings to be included in result");
				continue;
			}
			double totalSimilarityScoreForMovie = 0;
			//calculate the weighted score for this unwatchedMovie
			int unwatchedMovieID = movie.getMovieID();
			for(Map.Entry<Double, UserEntity> entry : similarUsers.entrySet()) {
				//have this user watched unwatchedMovieTitle?
				UserEntity similarUser = entry.getValue();
				double similarScore = entry.getKey(); //how similar this user is with other user
				if(similarUser.getRatedMovies().containsKey(unwatchedMovieID)) {
					totalSimilarityScoreForMovie += similarScore;
					//the similarUser have watched this movie, calc weightedScore based on user and similarUser similarity
					double weightedRating = similarUser.getRatedMovies().get(unwatchedMovieID) * similarScore; //weighted score on this movie based on similarity between users
					
					//add to weightedMovies
					boolean weightedScoreExists = weightedMovies.containsKey(unwatchedMovieID);
					double totalWeightedScore = (weightedScoreExists ? weightedMovies.get(unwatchedMovieID) : 0) + weightedRating;
					weightedMovies.put(unwatchedMovieID, totalWeightedScore); //add or overwrite previous score
				}
			}
			//now we have the sum weightedScores for every movie we could calculate it on, lets divide it by the sum of similarity score
			if(weightedMovies.get(unwatchedMovieID) != null) {
				double recommendationScore = weightedMovies.get(unwatchedMovieID) / totalSimilarityScoreForMovie;
				DecimalFormat df = new DecimalFormat("#.####");
				//TODO: It's not good to change the movie objects attributes, since this is the same movie object for the whole plattform.
				//		Should make a copy the movie that can be changed for the result list instead of using the same object.... mutable/immutable...
				movie.setRecommendationScore(new Double(df.format(recommendationScore)));
				movieRec.add(movie);
			}
		}
		
		return movieRec;
	}
	
	/**
	 * Get which movies user haven't watched
	 * @param user
	 * @param minRatings how many ratings a movie must have to be included in calculation
	 * @return a list of movies the user haven't watched
	 */
	private ArrayList<MovieEntity> getUnwatchedMovies(UserEntity user) {
		ArrayList<MovieEntity> unwatchedMovies = new ArrayList<>();
		for(MovieEntity movie : Application.getMovies().values()) {
			movie.setRecommendationScore(0);
			if(user.getRatedMovies().containsKey(movie.getMovieID())) {
				continue; //user have already watched this movie
			}else {
				unwatchedMovies.add(movie);
			}
		}
		//System.out.println("User: " + user.getUserName() + " has " + unwatchedMovies.size() + " unwatched movies");
		return unwatchedMovies;
	}	
	
	
	private void cacheSimilarity(String measure, double similarity, UserEntity u1, UserEntity u2) {
		u1.addSimilarityToCache(measure, similarity, u2.getUserID());
		u2.addSimilarityToCache(measure, similarity, u1.getUserID());
	}
}
