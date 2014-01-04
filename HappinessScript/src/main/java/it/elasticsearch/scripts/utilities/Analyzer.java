package it.elasticsearch.scripts.utilities;

import it.elasticsearch.scripts.models.ComputedHappiness;

import java.util.Map;
import java.util.Properties;

public interface Analyzer {

	static final String SPACE = " ";

	ComputedHappiness computeHappiness(String tweetText, Properties properties);

	ComputedHappiness computeHappiness(String tweetText, Map<String, Double> wordHappiness);

}
