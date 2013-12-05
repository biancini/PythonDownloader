package fr.twitteranalyzer;

import java.util.Date;

import fr.twitteranalyzer.exceptions.UtilsException;

public interface Analyzer {

	public void runAnalysis(Date date) throws UtilsException;

}
