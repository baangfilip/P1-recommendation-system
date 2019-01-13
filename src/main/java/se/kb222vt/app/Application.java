package se.kb222vt.app;

import static spark.Spark.*;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import com.google.gson.Gson;

import se.kb222vt.endpoint.RecommendationController;
import se.kb222vt.entities.MovieEntity;
import se.kb222vt.entities.UserEntity;
import se.kb222vt.logic.CacheBenchmark;
import spark.servlet.SparkApplication;

//Start Application by web.xml
public class Application implements SparkApplication {
	//putting some logic here since it will be so much overhead to put it somewhere else
	private static final HashMap<Integer, UserEntity> users = new HashMap<>(); //TODO: make this read only for real
	private static final HashMap<Integer, MovieEntity> movies = new HashMap<>(); //TODO: make this read only for real
	
	//CSV-location when running from Servlet
	private String ratingsCSV = "webapps/rec/WEB-INF/classes/data/MovieLens_100k/ratings.csv"; //This will not work for any name for the webapp: https://github.com/perwendel/spark/pull/658/files
	private String moviesCSV = "webapps/rec/WEB-INF/classes/data/MovieLens_100k/movies.csv"; //This will not work for any name for the webapp: https://github.com/perwendel/spark/pull/658/files
	private Gson gson = new Gson();
	
	@Override
	public void init() {
		System.out.println("Start endpoints");
		exception(IllegalArgumentException.class, (e, req, res) -> {
			  res.status(404);
			  res.body(gson.toJson(e));
			});
        get("/API/rec/user/:measure/:userID", RecommendationController.userBasedRecommendation);
        get("/API/users", (req, res) -> {
        	return gson.toJson(users);
        });
        get("/API/movies", (req, res) -> {
        	return gson.toJson(movies);
        });
        
        /*
            TODO:
        	Grade A-B
        	Measure the time it takes to find top 5 recommended movies for a user (try for example user 256)
			Build a cache for similarity calculations to avoid calculating similarity between two users more than once
			How much does the cache improve execution times when finding top 5 recommended movies?
        */
        try {
        	System.out.println("Load movies");
	        initMovies();
        	System.out.println("Load users");
			initUsers();
	        System.out.println("Found " + users.values().size() + " users");
	        System.out.println("Found " + movies.values().size() + " movies");
		} catch (IOException e) {
			System.out.println("Couldnt read users or movies: " + e.getMessage());
			e.printStackTrace();
		}
        
        CacheBenchmark cbr = new CacheBenchmark();
        try {
        	int runs = 500;
			cbr.benchmark(false, "euclidean", runs, true);
			cbr.benchmark(false, "euclidean", runs, false);
			cbr.benchmark(false, "euclidean", runs, false);
			cbr.benchmark(false, "euclidean", runs, false);
			cbr.benchmark(false, "pearson", runs, true);
			cbr.benchmark(false, "pearson", runs, false);
			cbr.benchmark(false, "pearson", runs, false);
			cbr.benchmark(false, "pearson", runs, false);
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
	
	private void initUsers() throws IOException {
        Reader reader = Files.newBufferedReader(Paths.get(ratingsCSV).toAbsolutePath());
        CSVParser csv = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader());
        for (CSVRecord line : csv) {
        	int userId = Integer.parseInt(line.get(0));
        	int movieId = Integer.parseInt(line.get(1));
        	double rating = Double.parseDouble(line.get(2));
        	if(!movies.containsKey(movieId)) {
        		System.out.println("Movie doesnt exist for rating");
        	}
        	movies.get(movieId).addUserRating(userId, rating);
        	if(!users.containsKey(userId)) {
                users.put(userId, new UserEntity("UserID: " + userId, userId));
        	}
        	users.get(userId).addRatedMovie(movieId, rating);
        }
        csv.close();
	}
	
	private void initMovies() throws IOException {
		Reader reader = Files.newBufferedReader(Paths.get(moviesCSV));
        CSVParser csv = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader());
        for (CSVRecord line : csv) {
        	String title = line.get(1);
        	int movieID = Integer.parseInt(line.get(0));
            MovieEntity movie = null;
            movie = movies.get(movieID);
            if(movie == null) {
            	movie =  new MovieEntity(title, movieID);
            }
            movies.put(movieID, movie);
        }
        csv.close();
	}
	
	public static HashMap<Integer, MovieEntity> getMovies() {
		return movies;
	}
	
	public static HashMap<Integer, UserEntity> getUsers() {
		return users;
	}
	
	public void setRatingsCSV(String path) {
		this.ratingsCSV = path;
	}
	public void setMoviesCSV(String path) {
		this.moviesCSV = path;
	}
	
}