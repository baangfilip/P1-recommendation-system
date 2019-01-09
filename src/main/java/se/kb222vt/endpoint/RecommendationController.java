package se.kb222vt.endpoint;

import java.util.Arrays;
import java.util.List;

import com.google.gson.Gson;

import se.kb222vt.app.Application;
import se.kb222vt.entities.UserEntity;
import se.kb222vt.logic.RecommendationLogic;
import spark.Request;
import spark.Response;
import spark.Route;

public class RecommendationController {
	private static RecommendationLogic logic = new RecommendationLogic();
	private static Gson gson = new Gson();
	private static List<String> supportedRecommendations = Arrays.asList(new String[]{"pearson", "euclidean"});
	
    public static Route userBasedRecommendation = (Request request, Response response) -> {
    	int userID = Integer.parseInt(request.params("userID"));
    	String measure = request.params("measure");
    	int minRatings = 0;
    	if(request.queryParams("minRatings") != null)
    		minRatings = Integer.parseInt(request.queryParams("minRatings"));
    	
    	if(!supportedRecommendations.contains(measure)) {
			throw new IllegalArgumentException("Measure: " + measure + " is not a supported similarity measure, valid similarity measures are: " + supportedRecommendations.toString());
    	}
		UserEntity user = Application.getUsers().get(userID);
		if(user == null) {
			throw new IllegalArgumentException("No user found for ID: " + userID);
		}
    	return gson.toJson(logic.userRec(user, measure, minRatings));
    };      
}