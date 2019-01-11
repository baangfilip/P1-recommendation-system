package se.kb222vt.entities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

public class RecommendationEntity {
	private UserEntity recommendationFor; //recommendation for this user
	private SortedMap<Double, UserEntity> similarUsers = new TreeMap<>(); //similarScore, User, sorted descending
	private List<MovieEntity> recommendedMovies = new ArrayList<>();
	
	
	public RecommendationEntity(UserEntity user) {
		this.recommendationFor = user;
	}

	public SortedMap<Double, UserEntity> getSimilarUsers() {
		return similarUsers;
	}

	public void setSimilarUsers(SortedMap<Double, UserEntity> similarUsers) {
		this.similarUsers = similarUsers;
	}

	public List<MovieEntity> getRecommendedMovies() {
		return recommendedMovies;
	}

	public void setRecommendedMovies(List<MovieEntity> recommendedMovies) {
		this.recommendedMovies = recommendedMovies;
	}

	public UserEntity getRecommendationFor() {
		return recommendationFor;
	}

	private HashMap<String, Object> metadata = new HashMap<>();
	public void addMetaData(String metadataName, Object data) {
		metadata.put(metadataName, data);
	}
	
}
