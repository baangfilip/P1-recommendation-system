package se.kb222vt.entities;

import java.util.Comparator;
import java.util.HashMap;

public class MovieEntity {
	private String title = "";
	private int movieID;
	private double recommendationScore = 0;
	private HashMap<Integer, Double> userRatings = new HashMap<>();
	
	public MovieEntity(String title, int movieID) {
		this.title = title;
		this.movieID = movieID;
	}

	public String getTitle(){
		return this.title;
	}
	
	public void addUserRating(int userID, double rating) {
		userRatings.put(userID, rating);
	}
	
	public int getMovieID() {
		return this.movieID;
	}
	
	public int getNbrOfRatings() {
		return userRatings.size();
	}
	
	public boolean equals(MovieEntity other){
		if(this == other || other.getTitle().equals(this.title) || other.getMovieID() == this.movieID) {
			return true;
		}
		return false;
	}

	public double getRecommendationScore() {
		return recommendationScore;
	}

	public void setRecommendationScore(double recommendationScore) {
		this.recommendationScore = recommendationScore;
	}
	

	/**
	 * Get comparator that sorts MovieEntity by recommendation score, if the score is the same sort by the number of ratings the entity have
	 * @return Comparator for sorting as described
	 */
	public static Comparator<MovieEntity> getMovieByRecommendationScore(){   
		 Comparator<MovieEntity> comparator = new Comparator<MovieEntity>(){
			@Override
			public int compare(MovieEntity movie1, MovieEntity movie2) {
				if(new Double(movie2.getRecommendationScore()).compareTo(new Double(movie1.getRecommendationScore())) == 0) {
					return new Integer(movie2.getNbrOfRatings()).compareTo(new Integer(movie1.getNbrOfRatings()));
				}else {
					return new Double(movie2.getRecommendationScore()).compareTo(new Double(movie1.getRecommendationScore()));
				}
			}        
		 };
		 return comparator;
	}
}
