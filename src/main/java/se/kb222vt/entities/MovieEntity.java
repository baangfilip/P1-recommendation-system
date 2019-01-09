package se.kb222vt.entities;

import java.util.HashMap;

public class MovieEntity {
	private String title = "";
	private int movieID;
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
		if(this == other || other.getTitle().equals(this.title)) {
			return true;
		}
		return false;
	}
	
}
