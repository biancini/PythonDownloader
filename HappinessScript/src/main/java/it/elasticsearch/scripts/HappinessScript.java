package it.elasticsearch.scripts;

import it.elasticsearch.scripts.utilities.HappinessWords;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.elasticsearch.common.logging.ESLogger;
import org.elasticsearch.common.logging.Loggers;
import org.elasticsearch.script.AbstractSearchScript;
import org.elasticsearch.search.lookup.SourceLookup;

public class HappinessScript extends AbstractSearchScript {

	public static final String SCORE_KEY = "score";
	public static final String RELEVANCE_KEY = "relevance";
	public static final String TEXT_FIELDNAME = "text";
	public static final String SPACE = " ";

	protected ESLogger logger = Loggers.getLogger("happiness.script");
	protected Properties properties = null;

	public HappinessScript(Properties properties) throws IOException {
		logger.debug("Initializing happiness script.");
		this.properties = properties;
	}

	protected SourceLookup getSource() {
		return source();
	}

	@Override
	public Object run() {
		String tweetText = (String) getSource().get(TEXT_FIELDNAME);

		if (tweetText == null) {
			return null;
		}

		logger.debug("Evaluating happiness on text: {}", tweetText);
		Map<String, Double> vals = computeHappiness(tweetText);

		if (vals == null) {
			logger.error("Returned null value from compute happiness.");
			return null;
		}

		if (!vals.containsKey(SCORE_KEY) || !vals.containsKey(RELEVANCE_KEY)) {
			logger.error("Wrong returned value from compute happiness: {}.", vals);
			return null;
		}

		return vals;
	}

	protected Map<String, Double> computeHappiness(String tweetText) {
		HashMap<String, Double> wordHappiness = HappinessWords.getWordHappiness(properties);
		return computeHappiness(tweetText, wordHappiness);
	}

	protected Map<String, Double> computeHappiness(String tweetText, Map<String, Double> wordHappiness) {
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

		Map<String, Double> returnVal = new HashMap<String, Double>();
		returnVal.put(SCORE_KEY, happiness);
		returnVal.put(RELEVANCE_KEY, relevance);
		return returnVal;
	}
}