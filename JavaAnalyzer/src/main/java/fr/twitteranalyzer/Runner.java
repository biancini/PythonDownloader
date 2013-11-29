package fr.twitteranalyzer;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

import fr.twitteranalyzer.elastic.ByPersonAnalyzer;
import fr.twitteranalyzer.exceptions.AnalyzerException;
import fr.twitteranalyzer.utils.DateUtils;
import fr.twitteranalyzer.utils.LoggerUtils;

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

			Date date = Calendar.getInstance().getTime();
			date = DateUtils.parseDate(dayOfInterest);

			analyzer.runAnalysis(date);
			LoggerUtils.writeLog("Finished.", false);
		} catch (AnalyzerException e) {
			LoggerUtils.writeLog(e.getMessage(), true);
			e.printStackTrace();
		}
	}

}
