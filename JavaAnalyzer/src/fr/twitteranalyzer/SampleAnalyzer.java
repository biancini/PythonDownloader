package fr.twitteranalyzer;

import java.util.Calendar;
import java.util.Date;

import fr.twitteranalyzer.exceptions.AnalyzerException;


public class SampleAnalyzer {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			ByPersonAnalyzer analyzer = new ByPersonAnalyzer();
			Date filterDate = Calendar.getInstance().getTime();
			analyzer.runAnalysis(filterDate, filterDate);
		} catch (AnalyzerException e) {
			e.printStackTrace();
		}
	}

}
