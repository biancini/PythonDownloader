package fr.twitteranalyzer.elastic;

import java.util.Date;

import org.elasticsearch.client.Client;

import fr.twitteranalyzer.Analyzer;
import fr.twitteranalyzer.exceptions.AnalyzerException;

public abstract class ElasticAnalyzerImpl implements Analyzer {

	public static final String CUSTERNAME = "frenchtweets";
	public static final String ELASTICSEARCH_HOST = "localhost";
	public static final int ELASTICSEARCH_PORT = 9300;

	public static final String CLUSTER_NAME_PROPERTY = "cluster.name";
	public static final String TOP_TWEETERS_FACETS = "top_tweeters";

	public static final String INDEX_NAME = "twitter";
	public static final String TWEETS_TYPE = "tweets";
	public static final String BYPERSON_TYPE = "byperson";

	protected static Client client = null;

	public static Client getClient() {
		return client;
	}

	public abstract void runAnalysis(Date date) throws AnalyzerException;

}
