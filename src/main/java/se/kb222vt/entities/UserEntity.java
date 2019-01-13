package se.kb222vt.entities;

import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import se.kb222vt.app.Application;

public class UserEntity {
	private String userName = "";
	private int userID;
	private transient HashMap<Integer, Double> movieRatings = new HashMap<>();
	private transient HashMap<String, HashMap<Integer, Double>> similarityUserCache = new HashMap<>(); 
	
	public UserEntity(String userName, int userID) {
		this.userName = userName;
		this.userID = userID;
	}
	
	public String getUserName() {
		return userName;
	}

	public int getUserID() {
		return userID;
	}
	
	public HashMap<Integer, Double> getRatedMovies(){
		return movieRatings;
	}
	
	public void addRatedMovie(int movieId, double rating) {
		movieRatings.put(movieId, rating);
	}
	
	public boolean equals(UserEntity other){
		if(this == other || other.getUserID() == this.userID) {
			return true;
		}
		return false;
	}
	
	/**
	 * Get similarity from cache, returns -1 if similarity is not found
	 * @param userID
	 * @param measure
	 * @return similarity if found, -1 otherwise
	 */
	public double getSimilarityFromCache(int userID, String measure) {
		HashMap<Integer, Double> similarityCache = similarityUserCache.get(measure); //get cache for the right similairty measure
		if(similarityCache == null) {
			return -1;
		}else if(similarityCache.containsKey(userID)){
			return similarityCache.get(userID);
		}else{
			return -1;
		}
	}

	/**
	 * Add similarity to cache for this user
	 * @param measure which measure is this similarity for
	 * @param similarity the value for the similarity
	 * @param userID which user is this similarity measure for
	 */
	public void addSimilarityToCache(String measure, double similarity, int userID) {
		HashMap<Integer, Double> similarityCache = similarityUserCache.get(measure); //get cache for the right similairty measure
		if(similarityCache == null) {
			//the cache doesnt exist, create a new one
			HashMap<Integer, Double> simCache = new HashMap<>();
			similarityUserCache.put(measure, simCache);
		}
		//get the cache (thats been created if not found)
		similarityCache = similarityUserCache.get(measure);
		//add similarity to the cache
		similarityCache.put(userID, similarity);
	}
	
	/**
	 * Get how many similarities are in the cache for this user
	 * @param measure
	 * @return
	 */
	public int getCacheSize(String measure) {
		if(similarityUserCache.get(measure) != null)
			return similarityUserCache.get(measure).size();
		else {
			return 0;
		}
	}
	
	/**
	 * Clear the cache for this user
	 * @return
	 */
	public void clearCache() {
		this.similarityUserCache = new HashMap<>();
	}
	
	/**
	 * Converts the cache to a SortedMap with only similarities above 0
	 * @param measure which cache to fetch
	 * @return
	 */
	public SortedMap<Double, UserEntity> getSortedCache(String measure){
		HashMap<Integer, Double> similarityCache = similarityUserCache.get(measure);
		SortedMap<Double, UserEntity> similarUsers = new TreeMap<Double, UserEntity>().descendingMap();
		for(Entry<Integer, Double> similarity : similarityCache.entrySet()) {
			if(similarity.getValue() > 0)
				similarUsers.put(similarity.getValue(), Application.getUsers().get(similarity.getKey()));
		}
		return similarUsers;
	}
	
}
