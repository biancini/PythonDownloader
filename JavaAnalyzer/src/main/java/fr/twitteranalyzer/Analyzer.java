package fr.twitteranalyzer;

import java.util.Date;

import fr.twitteranalyzer.exceptions.AnalyzerException;

public interface Analyzer {

	public void runAnalysis(Date date) throws AnalyzerException;

}
