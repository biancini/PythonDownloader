package fr.twitteranalyzer;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map.Entry;


public class SampleAnalyzer extends Analyzer {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		SampleAnalyzer analyzer = new SampleAnalyzer();
		
		Date filterDate = Calendar.getInstance().getTime();
		List<Entry<String, Integer>> tweetLeague = analyzer.queryTopTweeters(filterDate);
		
		System.out.println("Downloaded " + tweetLeague.size() + " twitters in the league.");
//		for (Entry<String, Integer> result : tweetLeague) {
//			System.out.println(result.getKey() + "\t" + result.getValue());
//		}
		
		for (int i = 0; i < 3; ++i) {
			Entry<String, Integer> curUser = tweetLeague.get(i);
			System.out.println("Getting " + curUser.getValue() + " tweets of user " + curUser.getKey() + ":");
			String tweets = analyzer.getAllTweetsForUserId(curUser.getKey(), curUser.getValue(), filterDate);
			System.out.println(tweets);
			System.out.println("");
		}

	}

}
