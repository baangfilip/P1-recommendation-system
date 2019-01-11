package se.kb222vt.logic;

import java.util.ArrayList;
import java.util.Random;

import se.kb222vt.app.Application;
import se.kb222vt.entities.UserEntity;

/**
 * This class is here to benchmark the cache functionality for similarity between users.
 * Choose to run this on startup if wanted
 * @author Baangfilip
 */
public class CacheBenchmark {

	private RecommendationLogic recLogic = new RecommendationLogic();
	
	public void benchmark() throws Exception {
		int runTimes = 100;
		//0. Clear the caches
		
		clearUserCaches();
		UserEntity user = getRandomUser();
		BenchmarkRun emptyCacheBCR = new BenchmarkRun(0, "Benchmark empty cache times euclidean");
		//1. Fetch some recommendation for user random user 100 ten times, clear cache everytime
		for(int i = 0; i < runTimes; i++) {
			long startTime = System.currentTimeMillis();//for metadata
			recLogic.userRec(user, "euclidean", 10, 5);
			long recommendationTime = (System.currentTimeMillis() - startTime);//for metadata
			emptyCacheBCR.addRun(recommendationTime);
			clearUserCaches();
		}
		System.out.println(emptyCacheBCR.toString(false));
		
		
		//Do the same as above, but dont clear cache and exclude the first result (since it doesnt benefit from cache)
		BenchmarkRun cacheBCR = new BenchmarkRun(100, "Benchmark full cache times euclidean");
		for(int i = 0; i < runTimes; i++) {
			long startTime = System.currentTimeMillis();//for metadata
			recLogic.userRec(user, "euclidean", 10, 5);
			long recommendationTime = (System.currentTimeMillis() - startTime);//for metadata
			if(i > 0)
				cacheBCR.addRun(recommendationTime);
		}
		System.out.println(cacheBCR.toString(false));
		
	
	}
	
	/**
	 * Get random user
	 * Not dependent on structure of hashmap or userid's
	 * @return a random user from the applications user list, null if something is wrong
	 */
	private UserEntity getRandomUser() {
		Random rand = new Random();
		int randomStopper = rand.nextInt(Application.getUsers().size());
		int current = 0;
		for(UserEntity u : Application.getUsers().values()) {
			if(current == randomStopper) {
				return u;
			}else {
				current++;
			}
		}
		return null;
	}
	
	private void clearUserCaches() {
		for(UserEntity u : Application.getUsers().values()) {
			u.clearCache();
		}
	}
	
	/**
	 * Container for a run in the benchmark
	 * @author Baangfilip
	 */
	class BenchmarkRun {
		private ArrayList<Double> times = new ArrayList<>();
		private String description;
		private double cache;
		
		public BenchmarkRun(double cache, String description) {
			this.description = description;
			this.setCache(cache);
		}

		public void addRun(double time) {
			times.add(time);
		}
		public ArrayList<Double> getTime() {
			return times;
		}

		public double getCache() {
			return cache;
		}

		public void setCache(double cache) {
			this.cache = cache;
		}
		
		public String toString(boolean showEachRun) {
			String string = ("Benchmark: " + description + "\n");
			double total = 0;
			int counter = 1;
			for(Double runTime : times) {
				total += runTime;
				if(showEachRun)
					string += ("Run "+counter+": " + runTime + "ms\n");
				counter++;
			}
			string += ("Mean value: " + (total/times.size()) + "ms");
			return string;
		}
	}
}
