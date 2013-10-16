package fr.twitteranalyzer;

import java.text.SimpleDateFormat;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map.Entry;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.FilterBuilder;
import org.elasticsearch.index.query.FilterBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.facet.Facet;
import org.elasticsearch.search.facet.FacetBuilders;
import org.elasticsearch.search.facet.terms.TermsFacet;


public class SampleAnalyzer extends Analyzer {
	
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

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		SampleAnalyzer analyzer = new SampleAnalyzer();
		
		Date filterDate = Calendar.getInstance().getTime();
		List<Entry<String, Integer>> tweetLeague = analyzer.queryTopTweeters(filterDate);
		
		System.out.println("Downloaded " + tweetLeague.size() + " twitters in the league.");
//		for (Entry<String, Integer> result : tweetLeague) {
//			System.out.println(result.getKey() + "\t" + result.getValue());
//		}
		
		for (int i = 0; i < 3; ++i) {
			Entry<String, Integer> curUser = tweetLeague.get(i);
			System.out.println("Getting " + curUser.getValue() + " tweets of user " + curUser.getKey() + ":");
			String tweets = analyzer.getAllTweetsForUserId(curUser.getKey(), curUser.getValue(), filterDate);
			System.out.println(tweets);
			System.out.println("");
		}

	}

}
