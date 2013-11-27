package fr.twitteranalyzer;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

import fr.twitteranalyzer.elastic.ByPersonAnalyzer;
import fr.twitteranalyzer.exceptions.AnalyzerException;
import fr.twitteranalyzer.utils.DateUtils;

//import fr.twitteranalyzer.mapreduce.ByPersonAnalyzer;

public class Runner {

	/**
	 * @param args
	 * @throws ParseException
	 */
	public static void main(String[] args) throws ParseException {
		try {
			String dayOfInterest = "2013-11-27";
			ByPersonAnalyzer analyzer = new ByPersonAnalyzer();

			Date fromDate = Calendar.getInstance().getTime();
			fromDate = DateUtils.parseDate(dayOfInterest);

			Date toDate = Calendar.getInstance().getTime();
			toDate = DateUtils.parseDate(dayOfInterest);

			analyzer.runAnalysis(fromDate, toDate);
			System.out.println("Finished.");
		} catch (AnalyzerException e) {
			System.err.println(e.getMessage());
			e.printStackTrace();
		}
	}

}
