package fr.twitteranalyzer;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import fr.twitteranalyzer.exceptions.AnalyzerException;


public class SampleAnalyzer {

	/**
	 * @param args
	 * @throws ParseException 
	 */
	public static void main(String[] args) throws ParseException {
		try {
			ByPersonAnalyzer analyzer = new ByPersonAnalyzer();
			
			Date filterDate = Calendar.getInstance().getTime();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			filterDate = sdf.parse("2013-10-18");
			
			analyzer.runAnalysis(filterDate, filterDate);
		} catch (AnalyzerException e) {
			e.printStackTrace();
		}
	}

}
