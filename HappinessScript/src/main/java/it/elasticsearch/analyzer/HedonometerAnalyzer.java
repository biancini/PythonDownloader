package it.elasticsearch.analyzer;

import it.elasticsearch.models.ComputedHappiness;
import it.elasticsearch.utilities.HappinessWords;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class HedonometerAnalyzer implements Analyzer {

	public static final String PARAM_ONLYRELEVANT = "only_relevant";

	private boolean onlyRelevantWords = false;

	@Override
	public void initialize(Properties properties) {
		if (properties != null && properties.containsKey(PARAM_ONLYRELEVANT)) {
			onlyRelevantWords = Boolean.parseBoolean(properties.getProperty(PARAM_ONLYRELEVANT));
		}
	}

	@Override
	public ComputedHappiness computeHappiness(String tweetText, Properties properties) {
		HashMap<String, Double> wordHappiness = HappinessWords.getWordHappiness(properties);
		return computeHappiness(tweetText, wordHappiness);
	}

	@Override
	public ComputedHappiness computeHappiness(String tweetText, Map<String, Double> wordHappiness) {
		if (wordHappiness == null || (tweetText == null || tweetText.length() == 0)) {
			return null;
		}
		String[] tweetWords = tweetText.split(SPACE);

		double happiness = 0.0;
		double relevance = 0.0;
		double allWords = 0.0;
		double relevantWords = 0.0;

		if (onlyRelevantWords) {
			for (String word : tweetWords) {
				word = word.toLowerCase();

				if (wordHappiness.containsKey(word)) {
					happiness += wordHappiness.get(word);
					relevantWords++;
				}

				allWords++;
			}

			if (relevantWords != 0) {
				happiness /= relevantWords;
			}
		} else {
			for (String word : tweetWords) {
				word = word.toLowerCase();

				if (wordHappiness.containsKey(word)) {
					happiness += wordHappiness.get(word);
					relevantWords++;
				} else if (!onlyRelevantWords) {
					happiness += 5.0;
				}

				allWords++;
			}

			happiness /= allWords;
		}

		relevance = relevantWords / allWords;

		return new ComputedHappiness(happiness, relevance);
	}
}
