package fr.twitteranalyzer;

import java.util.Date;

import fr.twitteranalyzer.exceptions.AnalyzerException;

public interface Analyzer {
	
	public void runAnalysis(Date from, Date to) throws AnalyzerException;

}
