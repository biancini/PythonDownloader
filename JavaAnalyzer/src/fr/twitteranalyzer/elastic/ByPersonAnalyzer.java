package fr.twitteranalyzer.elastic;

import java.text.ParseException;
import java.util.AbstractMap;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map.Entry;

import org.apache.hadoop.conf.Configuration;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.geo.GeoPoint;
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

import fr.twitteranalyzer.Analyzer;
import fr.twitteranalyzer.exceptions.AnalyzerException;
import fr.twitteranalyzer.model.ByPersonTweets;
import fr.twitteranalyzer.model.TweetsFields;
import fr.twitteranalyzer.utils.CoordinatesUtils;
import fr.twitteranalyzer.utils.DateUtils;

public class ByPersonAnalyzer extends ElasticAnalyzerImpl implements Analyzer {

	private static final String TOP_TWEETERS_FACETS = "top_tweeters";

	public ByPersonAnalyzer() throws AnalyzerException {
		client = getElasticSearchClient(ELASTICSEARCH_HOST, ELASTICSEARCH_PORT);
	}

	public String getJobName() {
		return "Elasticserch ByPersonAnalyzer";
	}

	public void additionalConfigurations(Configuration conf) {
		// Do nothing
	}

	public void runAnalysis(Date from, Date to) throws AnalyzerException {
		List<Entry<String, Integer>> tweetLeague = queryTopTweeters(from, to);
		System.out.println("Downloaded " + tweetLeague.size() + " twitters in the league.");

		int elements = tweetLeague.size();
		for (int i = 0; i < elements; ++i) {
			Entry<String, Integer> curUser = tweetLeague.get(i);
			// System.out.println("Getting " + curUser.getValue() +
			// " tweets of user " + curUser.getKey() + ".");
			ByPersonTweets tweetsByPerson = getAllTweetsForUserId(curUser.getKey(), curUser.getValue(), from, to);

			IndexRequestBuilder requestBuilder = client.prepareIndex(INDEXNAME, BYPERSONTYPE,
					tweetsByPerson.getId());
			requestBuilder.setSource(tweetsByPerson.toJsonDocument());
			requestBuilder.execute().actionGet();
		}
	}

	protected Client getElasticSearchClient(String hostname, int port) {
		Settings settings = ImmutableSettings.settingsBuilder().put("cluster.name", CUSTERNAME).build();

		TransportClient transportClient = new TransportClient(settings);
		transportClient = transportClient.addTransportAddress(new InetSocketTransportAddress(hostname, port));

		return transportClient;
	}

	public List<Entry<String, Integer>> queryTopTweeters(Date from, Date to) throws AnalyzerException {
		try {
			int hugenumber = 10000000;
			List<Entry<String, Integer>> topTweeters = new ArrayList<Entry<String, Integer>>();

			FilterBuilder filter = FilterBuilders.rangeFilter(TweetsFields.CREATEDAT.getFieldName())
					.from(DateUtils.firstSecondDate(from)).to(DateUtils.lastSecondDate(to));
			FacetBuilder facets = FacetBuilders.termsFacet(TOP_TWEETERS_FACETS)
					.field(TweetsFields.USERID.getFieldName()).size(hugenumber).facetFilter(filter);

			SearchRequestBuilder requestBuilder = client.prepareSearch(INDEXNAME).setTypes(TWEETSTYPE);
			requestBuilder.addFacet(facets);
			requestBuilder.setFrom(0);
			requestBuilder.setSize(0);
			requestBuilder.setExplain(false);
			SearchResponse response = requestBuilder.execute().actionGet();

			for (Facet facet : response.getFacets().facets()) {
				if (facet.getType().equals("terms")) {
					for (TermsFacet.Entry te : ((TermsFacet) facet).getEntries()) {
						SimpleEntry<String, Integer> simpleEntry = new AbstractMap.SimpleEntry<String, Integer>(te
								.getTerm().toString(), te.getCount());
						topTweeters.add(simpleEntry);
					}
				}
			}

			return topTweeters;
		} catch (Exception ex) {
			throw new AnalyzerException(ex.getMessage());
		}
	}

	public ByPersonTweets getAllTweetsForUserId(String user, long number, Date from, Date to)
			throws AnalyzerException {
		try {
			FilterBuilder dateFilter = FilterBuilders.rangeFilter(TweetsFields.CREATEDAT.getFieldName())
					.from(DateUtils.firstSecondDate(from)).to(DateUtils.lastSecondDate(to));
			FilterBuilder userFilter = FilterBuilders.termFilter(TweetsFields.USERID.getFieldName(), user);

			SearchRequestBuilder requestBuilder = client.prepareSearch(INDEXNAME).setTypes(TWEETSTYPE);
			requestBuilder.setFilter(FilterBuilders.andFilter(dateFilter).add(userFilter));
			requestBuilder.setFrom(0);
			requestBuilder.setSize((int) number);
			requestBuilder.setExplain(false);

			String[] fieldList = TweetsFields.getFieldList();
			for (String curField : fieldList) {
				requestBuilder.addField(curField);
			}
			SearchResponse response = requestBuilder.execute().actionGet();

			if (response.getHits().getTotalHits() < number) {
				String errMessage = "Downloaded tweets differ from total number expected";
				errMessage += " (" + response.getHits().getTotalHits() + " instead of " + number + ")";
				throw new AnalyzerException(errMessage);
			}

			ByPersonTweets tweetsByPerson = new ByPersonTweets(user);
			SearchHit[] hits = response.getHits().getHits();
			float happiness = 0F;
			float relevance = 0F;
			for (int i = 0; i < number; ++i) {
				setCommonAttributes(tweetsByPerson, hits, i);

				Double curHappiness = (Double) hits[i].field(TweetsFields.HAPPINESS.getFieldName()).getValue();
				happiness += curHappiness.floatValue();

				Double curRelevance = (Double) hits[i].field(TweetsFields.RELEVANCE.getFieldName()).getValue();
				relevance += curRelevance.floatValue();
			}

			tweetsByPerson.setHappiness(happiness / number);
			tweetsByPerson.setRelevance(relevance / number);

			return tweetsByPerson;
		} catch (Exception ex) {
			throw new AnalyzerException(ex.getMessage());
		}
	}

	private void setCommonAttributes(ByPersonTweets tweetsByPerson, SearchHit[] hits, int i) {
		try {
			if (tweetsByPerson.getDate() == null) {
				String strValue = hits[i].field(TweetsFields.CREATEDAT.getFieldName()).getValue();
				Date value = DateUtils.parseDate(strValue);
				tweetsByPerson.setDate(value);
			}
		} catch (ParseException e) {
			System.err.println("Error while parsing date.");
		}

		if (tweetsByPerson.getLocation() == null) {
			String value = hits[i].field(TweetsFields.LOCATION.getFieldName()).getValue().toString();
			tweetsByPerson.setLocation(value);
		}

		if (tweetsByPerson.getNumFriends() == -1) {
			Integer value = hits[i].field(TweetsFields.NUMFRIENDS.getFieldName()).getValue();
			tweetsByPerson.setNumFriends(value.intValue());
		}

		if (tweetsByPerson.getCoordinates() == null
				&& hits[i].field(TweetsFields.COORDINATES.getFieldName()) != null) {
			String strValue = (String) hits[i].field(TweetsFields.COORDINATES.getFieldName()).getValue();
			GeoPoint value = CoordinatesUtils.geopointFromString(strValue);
			tweetsByPerson.setCoordinates(value);
		}
	}

}
