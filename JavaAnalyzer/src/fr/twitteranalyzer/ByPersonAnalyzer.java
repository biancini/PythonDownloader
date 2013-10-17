package fr.twitteranalyzer;
import java.text.SimpleDateFormat;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map.Entry;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.index.query.FilterBuilder;
import org.elasticsearch.index.query.FilterBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.facet.Facet;
import org.elasticsearch.search.facet.FacetBuilder;
import org.elasticsearch.search.facet.FacetBuilders;
import org.elasticsearch.search.facet.terms.TermsFacet;

import fr.twitteranalyzer.exceptions.AnalyzerException;

public class ByPersonAnalyzer implements Analyzer {
	
	protected String elasticSearchHost = "localhost";
	protected int elasticSearchPort = 9300;
	protected Client client = null;
	
	public ByPersonAnalyzer() {
		client = getElasticSearchClient(elasticSearchHost, elasticSearchPort);
	}
	
	public void runAnalysis(Date from, Date to) throws AnalyzerException {
		List<Entry<String, Integer>> tweetLeague = queryTopTweeters(from, to);
		System.out.println("Downloaded " + tweetLeague.size() + " twitters in the league.");
		
		int elements = 10;
		//int elements = tweetLeague.size();
		
		for (int i = 0; i < elements; ++i) {
			Entry<String, Integer> curUser = tweetLeague.get(i);
			System.out.println("Getting " + curUser.getValue() + " tweets of user " + curUser.getKey() + ":");
			String tweets = getAllTweetsForUserId(curUser.getKey(), curUser.getValue(), from, to);
			//System.out.println(tweets);
			System.out.println("Total text length: " + tweets.length() + " characters.");
		}
	}
	
	protected Client getElasticSearchClient(String hostname, int port) {
		Settings settings = ImmutableSettings.settingsBuilder()
				.put("cluster.name", "frenchtweets")
				.build();
		
		TransportClient transportClient = new TransportClient(settings);
		transportClient = transportClient.addTransportAddress(new InetSocketTransportAddress(hostname, port));
		
		return transportClient;
	}
	
	public List<Entry<String, Integer>> queryTopTweeters(Date from, Date to) throws AnalyzerException {
		try {
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
			String strDateFrom = dateFormat.format(from);
			String strDateTo = dateFormat.format(to);
			
			int hugenumber = 10000000;
			List<Entry<String, Integer>> topTweeters = new ArrayList<Entry<String, Integer>>();
			
			FilterBuilder filter = FilterBuilders.rangeFilter("created_at").from(strDateFrom + " 00:00:00").to(strDateTo + " 23:59:59");
			FacetBuilder facets = FacetBuilders.termsFacet("top_tweeters").field("userid").size(hugenumber).facetFilter(filter);
			
			SearchResponse response = client.prepareSearch("twitter")
			        .setTypes("tweets")
			        .addFacet(facets)
			        .setFrom(0)
			        .setSize(0)
			        .setExplain(false)
			        .execute()
			        .actionGet();
			
			for (Facet facet : response.getFacets().facets()) {
				if (facet.getType().equals("terms")) { 
					for (TermsFacet.Entry te : ((TermsFacet) facet).getEntries()) {
						topTweeters.add(new AbstractMap.SimpleEntry<String, Integer>(te.getTerm().toString(), te.getCount()));
					}
				}
			}
			
			return topTweeters;
		} catch(Exception ex) {
			throw new AnalyzerException(ex.getMessage());
		}
	}
	
	public String getAllTweetsForUserId(String user, long number, Date from, Date to) throws AnalyzerException {
		try {
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
			String strDateFrom = dateFormat.format(from);
			String strDateTo = dateFormat.format(to);
		
			FilterBuilder dateFilter = FilterBuilders.rangeFilter("created_at").from(strDateFrom + " 00:00:00").to(strDateTo + " 23:59:59");
			FilterBuilder userFilter = FilterBuilders.termFilter("userid", user);
		
			SearchResponse response = client.prepareSearch("twitter")
					.setTypes("tweets")
					.setFilter(FilterBuilders.andFilter(dateFilter).add(userFilter))
					.addField("text")
					.setFrom(0)
					.setSize((int)number)
					.setExplain(false)
					.execute()
					.actionGet();
		
			if (response.getHits().getTotalHits() != number) {
				String errMessage = "Downloaded tweets differ from total number expected";
				errMessage += " (" + response.getHits().getTotalHits() + " instead of " + number + ")";
				throw new AnalyzerException(errMessage);
			}
			
			String allTweetText = "";
			
			SearchHit[] hits = response.getHits().getHits();
			for (SearchHit hit : hits) {
				if (!allTweetText.equals("")) allTweetText += "\n\n";
				allTweetText += hit.field("text").getValue().toString();
			}
			
			return allTweetText;
		} catch(Exception ex) {
			throw new AnalyzerException(ex.getMessage());
		}
	}
}
