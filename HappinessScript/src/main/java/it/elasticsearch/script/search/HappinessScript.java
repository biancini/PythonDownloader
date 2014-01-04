package it.elasticsearch.script.search;

import it.elasticsearch.models.ComputedHappiness;
import it.elasticsearch.utilities.Analyzer;
import it.elasticsearch.utilities.HappinessAnalyzer;

import java.io.IOException;
import java.util.Properties;

import org.elasticsearch.common.logging.ESLogger;
import org.elasticsearch.common.logging.Loggers;
import org.elasticsearch.script.AbstractSearchScript;

public class HappinessScript extends AbstractSearchScript {

	public static final String TEXT_FIELDNAME = "text";

	protected ESLogger logger = Loggers.getLogger("happiness.script");
	protected Properties properties = null;
	protected Analyzer analyzer = null;

	public HappinessScript(Properties properties) throws IOException {
		logger.debug("Initializing happiness script.");
		this.properties = properties;
		this.analyzer = new HappinessAnalyzer();
	}

	protected String getTweetText() {
		String tweetText = (String) source().get(TEXT_FIELDNAME);
		// ScriptDocValues.Strings fieldValue = (ScriptDocValues.Strings)
		// doc().get(TEXT_FIELDNAME);
		// if (fieldValue == null || fieldValue.getValues() == null) {
		// return null;
		// }
		//
		// String tweetText = fieldValue.getValue();
		return tweetText;
	}

	@Override
	public Object run() {
		String tweetText = getTweetText();

		if (tweetText == null) {
			return null;
		}

		logger.debug("Evaluating happiness on text: {}", tweetText);
		ComputedHappiness happiness = analyzer.computeHappiness(tweetText, properties);

		if (happiness == null) {
			logger.error("Returned null value from compute happiness.");
			return null;
		}

		return happiness.toMap();
	}

}