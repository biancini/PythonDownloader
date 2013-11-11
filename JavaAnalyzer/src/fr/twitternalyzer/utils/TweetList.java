package fr.twitternalyzer.utils;

import java.util.ArrayList;

import org.elasticsearch.action.admin.indices.analyze.AnalyzeRequest;
import org.elasticsearch.action.admin.indices.analyze.AnalyzeResponse;
import org.elasticsearch.client.IndicesAdminClient;

import fr.twitteranalyzer.BaseAnalyzer;

public class TweetList<E> extends ArrayList<E> {
	
	private static final long serialVersionUID = 1334569298679496397L;

	public String getAllElements(String separator, boolean analyzed) {
    	StringBuilder sb = new StringBuilder();
    	for (int i = 0; i < this.size(); i++) {
    		sb.append(this.get(i));
    		if (i < this.size() - 1) sb.append(separator);
    	}
    	
    	if (analyzed) {
    		String allTweetsText = sb.toString();
    		sb = new StringBuilder();
    		
	    	AnalyzeRequest ar = new AnalyzeRequest("twitter", allTweetsText).analyzer("tweettext");
			IndicesAdminClient indexClient = BaseAnalyzer.client.admin().indices();
			AnalyzeResponse aresp = indexClient.analyze(ar).actionGet();
			
			for (int i = 0; i < aresp.getTokens().size(); i++) {
	    		sb.append(aresp.getTokens().get(i).getTerm());
	    		if (i < aresp.getTokens().size() - 1) sb.append(" ");
	    	}
    	}
    	
    	return sb.toString();
	}
}
