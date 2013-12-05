package fr.twitteranalyzer.elastic;

import java.util.Date;

import org.elasticsearch.client.Client;

import fr.twitteranalyzer.Analyzer;
import fr.twitteranalyzer.exceptions.UtilsException;
import fr.twitteranalyzer.model.ElasticSearchConnection;
import fr.twitteranalyzer.utils.ElasticSearchUtils;

public abstract class ElasticAnalyzerImpl implements Analyzer {

	public static final String TOP_TWEETERS_FACETS = "top_tweeters";

	public static final String INDEX_NAME = ElasticSearchUtils.INDEX_NAME;
	public static final String TWEETS_TYPE = "tweets";
	public static final String BYPERSON_TYPE = "byperson";

	protected ElasticSearchConnection sourceConnection = null;
	protected ElasticSearchConnection destinationConnection = null;

	protected static Client clientSource = null;
	protected static Client clientDestination = null;

	public abstract void runAnalysis(Date date) throws UtilsException;

}
