package se.kb222vt.logic;

import java.text.DecimalFormat;
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
	
	public void benchmark(boolean logEveryRun) throws Exception {
		int runTimes = 500;
		//0. Clear the caches
		
		clearUserCaches();
		UserEntity user = getRandomUser();
		BenchmarkRun emptyCacheBCR = new BenchmarkRun("Benchmark empty cache times euclidean same user");
		//1. Fetch some recommendation for user random user 100 ten times, clear cache everytime
		for(int i = 0; i < runTimes; i++) {
			long startTime = System.currentTimeMillis();//for metadata
			recLogic.userRec(user, "euclidean", 10, 5);
			long recommendationTime = (System.currentTimeMillis() - startTime);//for metadata
			emptyCacheBCR.addRun(recommendationTime);
			emptyCacheBCR.addUserRatingCount(user.getRatedMovies().size());
			clearUserCaches();
		}
		if(logEveryRun)
			System.out.println(emptyCacheBCR.toString(false));
		

		clearUserCaches();
		//Do the same as above, but dont clear cache and exclude the first result (since it doesnt benefit from cache)
		BenchmarkRun cacheBCR = new BenchmarkRun("Benchmark full cache times euclidean same user");
		for(int i = 0; i < runTimes; i++) {
			long startTime = System.currentTimeMillis();//for metadata
			recLogic.userRec(user, "euclidean", 10, 5);
			long recommendationTime = (System.currentTimeMillis() - startTime);//for metadata
			if(i > 0) {
				cacheBCR.addRun(recommendationTime);
				cacheBCR.addUserRatingCount(user.getRatedMovies().size());
			}
		}
		if(logEveryRun)
			System.out.println(cacheBCR.toString(false));

		clearUserCaches();
		//Do the same as above, but dont clear cache and exclude the first result (since it doesnt benefit from cache)
		BenchmarkRun noCacheRandomUsersBCR = new BenchmarkRun("Benchmark no cache times euclidean random users");
		for(int i = 0; i < runTimes; i++) {
			UserEntity randomUser = getRandomUser();
			long startTime = System.currentTimeMillis();//for metadata
			recLogic.userRec(randomUser, "euclidean", 10, 5);
			long recommendationTime = (System.currentTimeMillis() - startTime);//for metadata
			noCacheRandomUsersBCR.addUserRatingCount(randomUser.getRatedMovies().size());
			noCacheRandomUsersBCR.addRun(recommendationTime);
			clearUserCaches();
		}
		if(logEveryRun)
			System.out.println(noCacheRandomUsersBCR.toString(false));
	
		clearUserCaches();
		//Do the same as above, but dont clear cache and exclude the first result (since it doesnt benefit from cache)
		BenchmarkRun cacheRandomUsersBCR = new BenchmarkRun("Benchmark full cache times euclidean random users");
		for(int i = 0; i < runTimes; i++) {
			UserEntity randomUser = getRandomUser();
			long startTime = System.currentTimeMillis();//for metadata
			recLogic.userRec(randomUser, "euclidean", 10, 5);
			long recommendationTime = (System.currentTimeMillis() - startTime);//for metadata
			if(i > 0) {
				cacheRandomUsersBCR.addRun(recommendationTime);
				cacheRandomUsersBCR.addUserRatingCount(randomUser.getRatedMovies().size());
			}
		}
		if(logEveryRun)
			System.out.println(cacheRandomUsersBCR.toString(false));
		
		clearUserCaches();
		BenchmarkRun emptyCacheBCR2 = new BenchmarkRun("Benchmark empty cache times euclidean same user, same as first test case but run in end to see if the first is just bad because its \"warming up\"");
		//1. Fetch some recommendation for user random user 100 ten times, clear cache everytime
		for(int i = 0; i < runTimes; i++) {
			long startTime = System.currentTimeMillis();//for metadata
			recLogic.userRec(user, "euclidean", 10, 5);
			long recommendationTime = (System.currentTimeMillis() - startTime);//for metadata
			emptyCacheBCR2.addRun(recommendationTime);
			emptyCacheBCR2.addUserRatingCount(user.getRatedMovies().size());
			clearUserCaches();
		}
		if(logEveryRun)
			System.out.println(emptyCacheBCR2.toString(false));

		System.out.println("###########################################################################");
		System.out.println("# RESULT FROM BENCHMARK ("+runTimes+" runs) \t\t\t\t\t  #");
		System.out.println("# ----------------------------------------------------------------------- #");
		System.out.println("# | Same user no cache   \t| "+emptyCacheBCR.getUserRatingCountMean()+" rec \t| \t "+emptyCacheBCR.getMeanTime()+"\t| #");
		System.out.println("# | Same user with cache \t| "+cacheBCR.getUserRatingCountMean()+" rec \t| \t "+cacheBCR.getMeanTime()+"\t| #");
		System.out.println("# | Random users no cache \t| "+noCacheRandomUsersBCR.getUserRatingCountMean()+" rec \t| \t "+noCacheRandomUsersBCR.getMeanTime()+"\t| #");
		System.out.println("# | Random users with cache \t| "+cacheRandomUsersBCR.getUserRatingCountMean()+" rec \t| \t "+cacheRandomUsersBCR.getMeanTime()+"\t| #");
		System.out.println("# | Same as first just again \t| "+emptyCacheBCR2.getUserRatingCountMean()+" rec \t| \t "+emptyCacheBCR2.getMeanTime()+"\t| #");
		System.out.println("# ----------------------------------------------------------------------- #");
		System.out.println("###########################################################################");
		
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
		private ArrayList<Integer> userRatingCount = new ArrayList<>();
		
		public BenchmarkRun(String description) {
			this.description = description;
		}

		public void addRun(double time) {
			times.add(time);
		}
		public ArrayList<Double> getTime() {
			return times;
		}

		
		public void addUserRatingCount(Integer similarUsers) {
			this.userRatingCount.add(similarUsers);
		}
		
		public int getUserRatingCountMean() {
			int total = 0;
			for(Integer count : userRatingCount) {
				total += count;
			}
			return total/userRatingCount.size();
		}
		
		public String getMeanTime() {
			double total = 0;
			for(Double runTime : times) {
				total += runTime;
			}
			DecimalFormat df = new DecimalFormat("#.00"); 
			return df.format(total/times.size()) + "ms";
		}
		
		public String toString(boolean showEachRun) {
			String string = ("Benchmark: " + description + "\n");
			int counter = 1;
			if(showEachRun) {
				for(Double runTime : times) {
					string += ("\tRun "+counter+": " + runTime + "ms\n");
					counter++;
				}
			}
			string += ("\t RESULT: \n\t -Mean value: " + getMeanTime() + "ms \n\t -Mean recommendations: " + getUserRatingCountMean() + " rating");
			return string;
		}
	}
}
