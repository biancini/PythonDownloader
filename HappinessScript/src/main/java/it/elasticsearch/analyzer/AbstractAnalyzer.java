package it.elasticsearch.analyzer;

public abstract class AbstractAnalyzer implements Analyzer {

	protected String[] splitWords(String tweetText) {
		if (tweetText == null || tweetText.length() == 0) {
			return new String[0];
		}

		String[] words = tweetText.split(WORD_REGEXP);
		return (words != null) ? words : new String[0];
	}

}
