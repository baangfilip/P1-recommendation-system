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
	
	/**
	 * Benchmark recommendation logic cache 
	 * This benchmark tests 4 different scenarios
	 * 1. Fetch recommendations for a single user without cache
	 * 2. Fetch recommendations for a single user with cache
	 * 3. Fetch recommendations for unique random users without cache
	 * 4. Fetch recommendations for the same random users as 3. with cache (one user may cache results for other users), starting from empty cache
	 * Test 1 and 2 can be run many times set with the param singleUserTestRuns and will be summarized to one result.
	 * 
	 * @param logEveryRun if every run should be printed to console
	 * @param measure what similarity measure to use for benchmark
	 * @param runTimes how many times should we fetch recommendations in 1. and 2. 
	 * @param skipLog skipLog if this is a warmup run, the first run usually have a higher result
	 * @param singleUsers a list of users to test case 1 and 2 on
	 * @param bulkUsers a list of users to test with for 3. and 4.
	 * @throws Exception
	 */
	public void benchmark(boolean logEveryRun, String measure, int runTimes, boolean skipLog, ArrayList<UserEntity> singleUsers, ArrayList<UserEntity> bulkUsers) throws Exception {
		//runTimes = 10;
		System.out.println("Starting benchmark on: " + measure + (skipLog ? " WAMRUP" : ""));
		
		//lets run the same user test a number of times to make sure the cache isn't just faster for the single randomized user
		System.out.print("Test 1 and 2 started and will run for " + singleUsers.size() + " users:");
		ArrayList<BenchmarkRun> sameUserRunsCache = new ArrayList<>();
		ArrayList<BenchmarkRun> sameUserRunsNoCache = new ArrayList<>();
		int count = 1;
		for(UserEntity user : singleUsers) {
			System.out.print(" " + count++ + "..");
			clearUserCaches();
			BenchmarkRun emptyCacheBCR = new BenchmarkRun("Benchmark empty cache times same user");
			sameUserRunsNoCache.add(emptyCacheBCR); //save benchmark run to be summarized with other users later
			for(int i = 0; i < runTimes; i++) {
				long startTime = System.currentTimeMillis();
				recLogic.userRec(user, measure, 10, 5);
				long recommendationTime = (System.currentTimeMillis() - startTime);
				emptyCacheBCR.addRun(recommendationTime);
				emptyCacheBCR.addUserRatingCount(user.getRatedMovies().size());
				emptyCacheBCR.addSimilarUsersCount(user.getSortedCache(measure).size());
				clearUserCaches();
			}
			if(logEveryRun)
				System.out.println(emptyCacheBCR.toString(false));
	
			clearUserCaches();
			BenchmarkRun cacheBCR = new BenchmarkRun("Benchmark full cache times same user");
			sameUserRunsCache.add(cacheBCR); //save benchmark run to be summarized with other users later
			for(int i = 0; i < runTimes; i++) {
				long startTime = System.currentTimeMillis();
				recLogic.userRec(user, measure, 10, 5);
				long recommendationTime = (System.currentTimeMillis() - startTime);
				if(i > 0) {
					cacheBCR.addRun(recommendationTime);
					cacheBCR.addUserRatingCount(user.getRatedMovies().size());
					cacheBCR.addSimilarUsersCount(user.getSortedCache(measure).size());
				}
			}
			if(logEveryRun)
				System.out.println(cacheBCR.toString(false));
		}
		//we have run the same user test a number of times now, lets summarize those runs
		BenchmarkRun noCacheBCR = new BenchmarkRun("Benchmark no cache times runned " + runTimes + " for " + singleUsers.size() + " users");
		BenchmarkRun cacheBCR = new BenchmarkRun("Benchmark full cache times runned " + runTimes + " for " + singleUsers.size() + " users");
		double totalTime = 0;
		int totalRatings = 0, totalSimUsers = 0;
		for(BenchmarkRun run : sameUserRunsCache) {
			totalTime += run.getMeanTime();
			totalRatings += run.getUserRatingCountMean();
			totalSimUsers += run.getSimilarUserCountMean();
		}
		cacheBCR.addRun(totalTime/singleUsers.size());
		cacheBCR.addSimilarUsersCount(totalSimUsers/singleUsers.size());
		cacheBCR.addUserRatingCount(totalRatings/singleUsers.size());
		
		totalTime = 0;
		totalRatings = 0; 
		totalSimUsers = 0;
		for(BenchmarkRun run : sameUserRunsNoCache) {
			totalTime += run.getMeanTime();
			totalRatings += run.getUserRatingCountMean();
			totalSimUsers += run.getSimilarUserCountMean();
		}
		noCacheBCR.addRun(totalTime/singleUsers.size());
		noCacheBCR.addSimilarUsersCount(totalSimUsers/singleUsers.size());
		noCacheBCR.addUserRatingCount(totalRatings/singleUsers.size());
		
		//Test how separate users benefit from cache and without cache.. 
		System.out.println("\nTest 3 started");
		clearUserCaches();
		BenchmarkRun noCacheRandomUsersBCR = new BenchmarkRun("Benchmark no cache times random users");
		for(UserEntity user : bulkUsers) {
			long startTime = System.currentTimeMillis();
			recLogic.userRec(user, measure, 10, 5);
			long recommendationTime = (System.currentTimeMillis() - startTime);
			noCacheRandomUsersBCR.addUserRatingCount(user.getRatedMovies().size());
			noCacheRandomUsersBCR.addRun(recommendationTime);
			noCacheRandomUsersBCR.addSimilarUsersCount(user.getSortedCache(measure).size());
			clearUserCaches();
		}
		if(logEveryRun)
			System.out.println(noCacheRandomUsersBCR.toString(false));

		System.out.println("Test 4 started");
		clearUserCaches();
		BenchmarkRun cacheRandomUsersBCR = new BenchmarkRun("Benchmark full cache times random users");
		for(UserEntity user : bulkUsers) {
			long startTime = System.currentTimeMillis();
			recLogic.userRec(user, measure, 10, 5);
			long recommendationTime = (System.currentTimeMillis() - startTime);
			cacheRandomUsersBCR.addRun(recommendationTime);
			cacheRandomUsersBCR.addUserRatingCount(user.getRatedMovies().size());
			cacheRandomUsersBCR.addSimilarUsersCount(user.getSortedCache(measure).size());
		}
		if(logEveryRun)
			System.out.println(cacheRandomUsersBCR.toString(false));
		
		if(!skipLog) {
			System.out.println("");
			System.out.println("###################################################################################################");
			System.out.println("# RESULT FROM BENCHMARK                                                                    	  #");
			System.out.println("# Measure: "+measure+" \t                                          	                          #");
			System.out.println("# ----------------------------------------------------------------------------------------------- #");
			System.out.println("# | Type \t\t\t\t| Similar users \t| Ratings \t| Mean time \t| #");
			System.out.println("# ----------------------------------------------------------------------------------------------- #");
			System.out.println("# | Same user no cache ("+singleUsers.size()+" runs)  \t| "+noCacheBCR.getSimilarUserCountMean()+" \t\t\t| "+noCacheBCR.getUserRatingCountMean()+" \t\t| "+noCacheBCR.getMeanTimeString()+"\t| #");
			System.out.println("# | Same user with cache ("+singleUsers.size()+" runs) \t| "+cacheBCR.getSimilarUserCountMean()+" \t\t\t| "+cacheBCR.getUserRatingCountMean()+" \t\t| "+cacheBCR.getMeanTimeString()+"\t| #");
			System.out.println("# | Random users no cache ("+bulkUsers.size()+" users) \t| "+noCacheRandomUsersBCR.getSimilarUserCountMean()+" \t\t\t| "+noCacheRandomUsersBCR.getUserRatingCountMean()+" \t\t| "+noCacheRandomUsersBCR.getMeanTimeString()+"\t| #");
			System.out.println("# | Random users with cache ("+bulkUsers.size()+" users) | "+cacheRandomUsersBCR.getSimilarUserCountMean()+" \t\t\t| "+cacheRandomUsersBCR.getUserRatingCountMean()+" \t\t| "+cacheRandomUsersBCR.getMeanTimeString()+"\t| #");
			System.out.println("# ----------------------------------------------------------------------------------------------- #");
			System.out.println("# SUMMARY:                                                                                 	  #");	
			System.out.println("# Is cache faster for the same user: "+isCacheFaster(cacheBCR, noCacheBCR)+" \t\t\t\t\t  #");			
			System.out.println("# Is cache faster for random users: "+isCacheFaster(cacheRandomUsersBCR, noCacheRandomUsersBCR)+" \t\t\t\t\t\t  #");
			System.out.println("###################################################################################################");
			System.out.println("");
		}
	}
	
	public String isCacheFaster(BenchmarkRun cache, BenchmarkRun notCache) {
		DecimalFormat df = new DecimalFormat("#.00"); 
		if(cache.getMeanTime() < notCache.getMeanTime()) {
			//cache is faster
			return "TRUE " + df.format((notCache.getMeanTime() / cache.getMeanTime()-1)*100) + "% faster";
		}else if(cache.getMeanTime() == notCache.getMeanTime()){
			return "Cache == no cache";
		}  else {
			return "FALSE " + df.format((notCache.getMeanTime() / cache.getMeanTime()-1)*100) + "% slower";
		}
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
	
	/**
	 * Get random number of users.
	 * If nbrOfUsers to get is more then 50% of the systems total users it will get users that are in a range rather then randomizing.
	 * @param nbrOfUsers
	 * @return
	 * @throws Exception 
	 */
	public ArrayList<UserEntity> getUsers(int nbrOfUsers) throws Exception{
		ArrayList<UserEntity> users = new ArrayList<UserEntity>();
		if(nbrOfUsers > Application.getUsers().size()) {
			throw new Exception("Maximum number of users are exceded, maximum user is: " + Application.getUsers().size());
		}
		Random rand = new Random();
		//get random starting point as long as there are enough users left in map
		int randomStart = rand.nextInt(Application.getUsers().size()-nbrOfUsers+1);
		if(nbrOfUsers > Application.getUsers().size()/2) {
			int count = 0;
			for(UserEntity user : Application.getUsers().values()) {
				if(count >= randomStart) {//start adding users
					users.add(user);
				}
				count++;
				if(users.size() == nbrOfUsers) {
					//we have enough users
					break;
				}
			}
		}else {
			//get random users
			for(int i = 0; i < nbrOfUsers; i++) {
				UserEntity randomUser = getRandomUser();
				while(users.contains(randomUser)) { 
					//make sure we dont get the same user
					randomUser = getRandomUser();
				}
				users.add(randomUser);
			}
		}
		
		return users;
	}
	
	/**
	 * Clear the applications users cache for similarity
	 */
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
		private ArrayList<Integer> similarUsersCount = new ArrayList<>();
		
		public BenchmarkRun(String description) {
			this.description = description;
		}

		public void addRun(double time) {
			times.add(time);
		}
		public ArrayList<Double> getTime() {
			return times;
		}

		public void addUserRatingCount(Integer ratings) {
			this.userRatingCount.add(ratings);
		}
		
		public void addSimilarUsersCount(Integer similarUsers) {
			this.similarUsersCount.add(similarUsers);
		}
		
		/**
		 * Get the average similar user count for users in this BenchmarkRun
		 * @return
		 */
		public int getSimilarUserCountMean() {
			int total = 0;
			for(Integer count : similarUsersCount) {
				total += count;
			}
			return total/similarUsersCount.size();
		}
		
		/**
		 * Get the average ratings done by users in this run
		 * @return
		 */
		public int getUserRatingCountMean() {
			int total = 0;
			for(Integer count : userRatingCount) {
				total += count;
			}
			return total/userRatingCount.size();
		}
		
		/**
		 * Get the average time of all times as a pretty string
		 * @return
		 */
		public String getMeanTimeString() {
			double total = 0;
			for(Double runTime : times) {
				total += runTime;
			}
			DecimalFormat df = new DecimalFormat("#.00"); 
			return df.format(total/times.size()) + "ms";
		}
		
		/**
		 * Get the average time of all times
		 * @return
		 */
		public double getMeanTime() {
			double total = 0;
			for(Double runTime : times) {
				total += runTime;
			}
			DecimalFormat df = new DecimalFormat("#.00"); 
			return total/times.size();
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
