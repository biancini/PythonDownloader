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
import org.elasticsearch.common.geo.GeoPoint;
import org.elasticsearch.index.query.FilterBuilder;
import org.elasticsearch.index.query.FilterBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.facet.Facet;
import org.elasticsearch.search.facet.FacetBuilder;
import org.elasticsearch.search.facet.FacetBuilders;
import org.elasticsearch.search.facet.terms.TermsFacet;

import fr.twitteranalyzer.Analyzer;
import fr.twitteranalyzer.exceptions.UtilsException;
import fr.twitteranalyzer.model.ByPersonTweets;
import fr.twitteranalyzer.model.ElasticSearchConnection;
import fr.twitteranalyzer.model.TweetsFields;
import fr.twitteranalyzer.utils.CoordinatesUtils;
import fr.twitteranalyzer.utils.DateUtils;
import fr.twitteranalyzer.utils.ElasticSearchUtils;
import fr.twitteranalyzer.utils.LoggerUtils;

public class ByPersonAnalyzer extends ElasticAnalyzerImpl implements Analyzer {

	private static final String TERMS_PROPERTY = "terms";

	public ByPersonAnalyzer(ElasticSearchConnection source, ElasticSearchConnection destination)
			throws UtilsException {
		if (source == null) {
			source = new ElasticSearchConnection();
		}

		if (destination == null) {
			destination = new ElasticSearchConnection();
		}

		clientSource = ElasticSearchUtils.getElasticSearchClient(source);
		clientDestination = ElasticSearchUtils.getElasticSearchClient(destination);
	}

	public String getJobName() {
		return "Elasticserch ByPersonAnalyzer";
	}

	public void additionalConfigurations(Configuration conf) {
		// Do nothing
	}

	public void runAnalysis(Date date) throws UtilsException {
		List<Entry<Long, Integer>> tweetLeague = queryTopTweeters(date);
		LoggerUtils.info("Downloaded " + tweetLeague.size() + " twitters in the league.");

		int elements = tweetLeague.size();
		for (int i = 0; i < elements; ++i) {
			Entry<Long, Integer> curUser = tweetLeague.get(i);
			// LoggerUtils.writeLog("Getting " + curUser.getValue() +
			// " tweets of user " + curUser.getKey() + ".", false);
			ByPersonTweets tweetsByPerson = getAllTweetsForUserId(curUser.getKey(), curUser.getValue(), date);

			IndexRequestBuilder requestBuilder = clientDestination.prepareIndex(INDEX_NAME, BYPERSON_TYPE,
					tweetsByPerson.getId().toString());
			requestBuilder.setSource(tweetsByPerson.toJsonDocument());
			requestBuilder.execute().actionGet();
		}
	}

	protected List<Entry<Long, Integer>> queryTopTweeters(Date date) throws UtilsException {
		try {
			int hugenumber = 10000000;
			List<Entry<Long, Integer>> topTweeters = new ArrayList<Entry<Long, Integer>>();

			FilterBuilder filter = FilterBuilders.rangeFilter(TweetsFields.CREATEDAT.getFieldName())
					.from(DateUtils.firstSecondDate(date)).to(DateUtils.lastSecondDate(date));
			FacetBuilder facets = FacetBuilders.termsFacet(TOP_TWEETERS_FACETS)
					.field(TweetsFields.USERID.getFieldName()).size(hugenumber).facetFilter(filter);

			SearchRequestBuilder requestBuilder = clientSource.prepareSearch(INDEX_NAME).setTypes(TWEETS_TYPE);
			requestBuilder.addFacet(facets);
			requestBuilder.setFrom(0);
			requestBuilder.setSize(0);
			requestBuilder.setExplain(false);
			SearchResponse response = requestBuilder.execute().actionGet();

			for (Facet facet : response.getFacets().facets()) {
				if (facet.getType().equals(TERMS_PROPERTY)) {
					for (TermsFacet.Entry te : ((TermsFacet) facet).getEntries()) {
						Long userid = Long.parseLong(te.getTerm().toString());
						Integer numtweets = te.getCount();

						SimpleEntry<Long, Integer> simpleEntry = new AbstractMap.SimpleEntry<Long, Integer>(
								userid, numtweets);
						topTweeters.add(simpleEntry);
					}
				}
			}

			return topTweeters;
		} catch (Exception ex) {
			throw new UtilsException(ex.getMessage());
		}
	}

	protected ByPersonTweets getAllTweetsForUserId(Long user, long number, Date date) throws UtilsException {
		try {
			FilterBuilder dateFilter = FilterBuilders.rangeFilter(TweetsFields.CREATEDAT.getFieldName())
					.from(DateUtils.firstSecondDate(date)).to(DateUtils.lastSecondDate(date));
			FilterBuilder userFilter = FilterBuilders.termFilter(TweetsFields.USERID.getFieldName(), user);

			SearchRequestBuilder requestBuilder = clientSource.prepareSearch(INDEX_NAME).setTypes(TWEETS_TYPE);
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
				throw new UtilsException(errMessage);
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
			happiness /= number;
			relevance /= number;
			tweetsByPerson.setHappiness(happiness);
			tweetsByPerson.setRelevance(relevance);

			return tweetsByPerson;
		} catch (Exception ex) {
			throw new UtilsException(ex.getMessage());
		}
	}

	private void setCommonAttributes(ByPersonTweets tweetsByPerson, SearchHit[] hits, int i) {
		try {
			if (tweetsByPerson.getDate() == null) {
				String strValue = hits[i].field(TweetsFields.CREATEDAT.getFieldName()).getValue();
				Date value = DateUtils.parseDate(strValue);
				value = new Date(value.getTime() + 2 * DateUtils.HOUR);
				tweetsByPerson.setDate(value);
			}
		} catch (ParseException e) {
			LoggerUtils.error("Error while parsing date.", e);
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
