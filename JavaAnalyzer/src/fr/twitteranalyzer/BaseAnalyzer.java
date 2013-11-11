package fr.twitteranalyzer;

import java.util.Date;

import org.elasticsearch.client.Client;

import fr.twitteranalyzer.exceptions.AnalyzerException;

public abstract class BaseAnalyzer implements IAnalyzer {

	public static final String elasticSearchHost = "localhost";
	public static final int elasticSearchPort = 9300;
	public static Client client = null;
	
	public abstract void runAnalysis(Date from, Date to) throws AnalyzerException;

}
