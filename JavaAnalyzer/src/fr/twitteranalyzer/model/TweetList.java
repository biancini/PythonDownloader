package fr.twitteranalyzer.model;

import java.util.ArrayList;

import org.elasticsearch.action.admin.indices.analyze.AnalyzeRequest;
import org.elasticsearch.action.admin.indices.analyze.AnalyzeResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.IndicesAdminClient;

public class TweetList extends ArrayList<String> {

	private static final long serialVersionUID = 1334569298679496397L;

	private Client client = null;

	public TweetList(Client client) {
		this.client = client;
	}

	public String getAllElements(String separator, boolean analyzed) {
		StringBuilder stringBuilder = new StringBuilder();

		for (int i = 0; i < this.size(); i++) {
			stringBuilder.append(this.get(i));
			if (i < this.size() - 1) {
				stringBuilder.append(separator);
			}
		}

		if (analyzed) {
			String allTweetsText = stringBuilder.toString();
			stringBuilder = new StringBuilder();

			AnalyzeRequest analyzerRequest = new AnalyzeRequest("twitter",
					allTweetsText).analyzer("tweettext");
			IndicesAdminClient indexClient = client.admin().indices();
			AnalyzeResponse analyzerResponse = indexClient.analyze(
					analyzerRequest).actionGet();

			for (int i = 0; i < analyzerResponse.getTokens().size(); i++) {
				stringBuilder.append(analyzerResponse.getTokens().get(i)
						.getTerm());

				if (i < analyzerResponse.getTokens().size() - 1) {
					stringBuilder.append(" ");
				}
			}
		}

		return stringBuilder.toString();
	}
}
