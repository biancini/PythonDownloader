package fr.twitteranalyzer;

import java.text.ParseException;
import java.util.Date;

import fr.twitteranalyzer.exceptions.UtilsException;
//import fr.twitteranalyzer.elastic.ByPersonAnalyzer;
import fr.twitteranalyzer.mapreduce.ByPersonAnalyzer;
import fr.twitteranalyzer.model.ElasticSearchConnection;
import fr.twitteranalyzer.utils.LoggerUtils;

public class Runner {

	private static final String SOURCE_CLUSTERNAME = "frenchtweets";
	private static final String SOURCE_ELASTCISEARCHHOST = "localhost";
	private static final int SOURCE_ELASTICSEARCHPORT = 9300;

	private static final String DESTINATION_CLUSTERNAME = "frenchtweets";
	private static final String DESTINATION_ELASTCISEARCHHOST = "localhost";
	private static final int DESTINATION_ELASTICSEARCHPORT = 9300;

	/**
	 * @param args
	 * @throws ParseException
	 */
	public static void main(String[] args) throws ParseException {
		try {
			ElasticSearchConnection source = new ElasticSearchConnection(SOURCE_CLUSTERNAME,
					SOURCE_ELASTCISEARCHHOST, SOURCE_ELASTICSEARCHPORT);
			ElasticSearchConnection destination = new ElasticSearchConnection(DESTINATION_CLUSTERNAME,
					DESTINATION_ELASTCISEARCHHOST, DESTINATION_ELASTICSEARCHPORT);

			// String dayOfInterest = "2013-11-27";
			// Date date = Calendar.getInstance().getTime();
			// date = DateUtils.parseDate(dayOfInterest);
			Date date = new Date();

			// ByPersonAnalyzer analyzer = new ByPersonAnalyzer(source,
			// destination);
			ByPersonAnalyzer analyzer = new ByPersonAnalyzer();
			analyzer.runAnalysis(date);

			LoggerUtils.info("Finished.");
		} catch (UtilsException e) {
			LoggerUtils.error(e.getMessage(), e);
		}
	}

}
