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
import org.elasticsearch.search.facet.FacetBuilders;
import org.elasticsearch.search.facet.terms.TermsFacet;

public class Analyzer {
	
	protected String elasticSearchHost = "localhost";
	protected int elasticSearchPort = 9300;
	protected Client client = null;
	
	public Analyzer() {
		client = getElasticSearchClient(elasticSearchHost, elasticSearchPort);
	}
	
	protected Client getElasticSearchClient(String hostname, int port) {
		Settings settings = ImmutableSettings.settingsBuilder()
				.put("cluster.name", "frenchtweets")
				.build();
		
		TransportClient transportClient = new TransportClient(settings);
		transportClient = transportClient.addTransportAddress(new InetSocketTransportAddress(hostname, port));
		
		return transportClient;
	}
	
	public List<Entry<String, Integer>> queryTopTweeters(Date filterDate) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		String strDate = dateFormat.format(filterDate);
		
		int hugenumber = 10000000;
		List<Entry<String, Integer>> topTweeters = new ArrayList<Entry<String, Integer>>();
		
		SearchResponse response = client.prepareSearch("twitter")
		        .setTypes("tweets")
		        .setFilter(FilterBuilders.rangeFilter("created_at").from(strDate + " 00:00:00").to(strDate + " 23:59:59"))
		        .addFacet(FacetBuilders.termsFacet("top_tweeters").field("userid").size(hugenumber))
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
	}
	
	public String getAllTweetsForUserId(String user, int number, Date filterDate) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		String strDate = dateFormat.format(filterDate);
		
		FilterBuilder dateFilter = FilterBuilders.rangeFilter("created_at").from(strDate + " 00:00:00").to(strDate + " 23:59:59");
		FilterBuilder userFilter = FilterBuilders.termFilter("userid", user);
		
		SearchResponse response = client.prepareSearch("twitter")
		        .setTypes("tweets")
		        .setFilter(FilterBuilders.andFilter(dateFilter).add(userFilter))
		        .addField("text")
		        .setFrom(0)
		        .setSize(number)
		        .setExplain(false)
		        .execute()
		        .actionGet();
		
		String allTweetText = "";
		
		SearchHit[] hits = response.getHits().getHits();
		for (SearchHit hit : hits) {
			if (!allTweetText.equals("")) allTweetText += "\n\n";
			allTweetText += hit.field("text").getValue().toString();
		}
		
		return allTweetText;
	}

}
