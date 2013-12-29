package it.elasticsearch.scripts;

import it.elasticsearch.scripts.utilities.HappinessWords;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.elasticsearch.common.Nullable;
import org.elasticsearch.common.logging.ESLogger;
import org.elasticsearch.common.logging.Loggers;
import org.elasticsearch.script.AbstractDoubleSearchScript;

public class HappinessScript extends AbstractDoubleSearchScript {

	public static final String TEXT_FIELDNAME = "text";
	public static final String SPACE = " ";

	private final ESLogger logger = Loggers.getLogger("happiness.script");
	Map<String, Object> params = null;

	public HappinessScript(@Nullable Map<String, Object> params) throws IOException {
		logger.debug("Initializing happiness script.");
		this.params = params;
	}

	@Override
	public double runAsDouble() {
		String tweetText = (String) source().get(TEXT_FIELDNAME);

		if (tweetText == null) {
			return 0;
		}

		logger.debug("Evaluating happiness on text: {}", tweetText);
		return computeHappiness(tweetText);
	}

	protected double computeHappiness(String tweetText) {
		try {
			HashMap<String, Double> wordHappiness = HappinessWords.getWordHappiness(params);
			return computeHappiness(tweetText, wordHappiness);
		} catch (IOException e) {
			logger.error("Error while computing happiness: {}.", e);
			return -1;
		}
	}

	protected double computeHappiness(String tweetText, HashMap<String, Double> wordHappiness) {
		String[] tweetWords = tweetText.split(SPACE);

		double happiness = 0.0;
		double words = 0.0;

		for (String word : tweetWords) {
			if (wordHappiness.containsKey(word)) {
				happiness += wordHappiness.get(word);
			} else {
				happiness += 5.0;
			}
			words++;
		}

		happiness /= words;
		return happiness;
	}
}