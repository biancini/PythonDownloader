package it.elasticsearch.utilities;

import it.elasticsearch.models.ComputedHappiness;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class HappinessAnalyzer implements Analyzer {

	@Override
	public ComputedHappiness computeHappiness(String tweetText, Properties properties) {
		HashMap<String, Double> wordHappiness = HappinessWords.getWordHappiness(properties);
		return computeHappiness(tweetText, wordHappiness);
	}

	@Override
	public ComputedHappiness computeHappiness(String tweetText, Map<String, Double> wordHappiness) {
		if (wordHappiness == null) {
			return null;
		}
		String[] tweetWords = tweetText.split(SPACE);

		double happiness = 0.0;
		double relevance = 0.0;
		double allWords = 0.0;
		double relevantWords = 0.0;

		for (String word : tweetWords) {
			if (wordHappiness.containsKey(word)) {
				happiness += wordHappiness.get(word);
				relevantWords++;
			} else {
				happiness += 5.0;
			}
			allWords++;
		}

		happiness /= allWords;
		relevance = relevantWords / allWords;

		return new ComputedHappiness(happiness, relevance);
	}
}
