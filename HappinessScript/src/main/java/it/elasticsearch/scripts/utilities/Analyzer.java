package it.elasticsearch.scripts.utilities;

import java.util.Map;
import java.util.Properties;

public interface Analyzer {

	static final String SCORE_KEY = "score";
	static final String RELEVANCE_KEY = "relevance";
	static final String SPACE = " ";

	Map<String, Double> computeHappiness(String tweetText, Properties properties);

	Map<String, Double> computeHappiness(String tweetText, Map<String, Double> wordHappiness);

}
