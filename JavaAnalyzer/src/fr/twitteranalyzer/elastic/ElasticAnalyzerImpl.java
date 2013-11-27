package fr.twitteranalyzer.elastic;

import java.util.Date;

import org.elasticsearch.client.Client;

import fr.twitteranalyzer.Analyzer;
import fr.twitteranalyzer.exceptions.AnalyzerException;

public abstract class ElasticAnalyzerImpl implements Analyzer {

	public static final String elasticSearchHost = "localhost";
	public static final int elasticSearchPort = 9300;
	protected static Client client = null;

	public static Client getClient() {
		return client;
	}

	public abstract void runAnalysis(Date from, Date to)
			throws AnalyzerException;

}
