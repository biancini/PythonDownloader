package fr.twitteranalyzer.model;

import static org.fest.assertions.Assertions.assertThat;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import org.elasticsearch.common.geo.GeoPoint;
import org.junit.Test;

public class ByPersonTweetsTest {

	private static final Long USERID = 123456L;
	private static final String DATE = "1980-10-05";
	private static final String LOCATION = "Paris";
	private static final int NUMFRIENDS = 100;
	private static final double LATITUDE = 48.822768D;
	private static final double LONGITUDE = 2.345388D;
	private static final float HAPPINESS = 7.5F;
	private static final float RELEVANCE = 0.8F;

	private static final String DATE_KEY = "date";
	private static final String LOCATION_KEY = "location";
	private static final String NUMFRIENDS_KEY = "num_friends";
	private static final String COORDINATES_KEY = "coordinates";
	private static final String HAPPINESS_KEY = "happiness";
	private static final String RELEVANCE_KEY = "relevance";

	@Test
	public void shouldToJsonDocumentProduceRightHashMap() throws ParseException {
		// given
		SimpleDateFormat dateFormat = new SimpleDateFormat("YYYY-MM-DD");
		Date date = dateFormat.parse(DATE);
		GeoPoint coordinates = new GeoPoint(LATITUDE, LONGITUDE);

		ByPersonTweets byPersonTweets = new ByPersonTweets(USERID);
		byPersonTweets.setDate(date);
		byPersonTweets.setLocation(LOCATION);
		byPersonTweets.setNumFriends(NUMFRIENDS);
		byPersonTweets.setCoordinates(coordinates);
		byPersonTweets.setHappiness(HAPPINESS);
		byPersonTweets.setRelevance(RELEVANCE);

		// when
		Map<String, Object> returnValue = byPersonTweets.toJsonDocument();

		// then
		assertThat(returnValue).isNotEmpty();
		assertThat(returnValue.get(DATE_KEY)).isEqualTo(date);
		assertThat(returnValue.get(LOCATION_KEY)).isEqualTo(LOCATION);
		assertThat(returnValue.get(NUMFRIENDS_KEY)).isEqualTo(NUMFRIENDS);
		assertThat(returnValue.get(COORDINATES_KEY)).isEqualTo(coordinates);
		assertThat(returnValue.get(HAPPINESS_KEY)).isEqualTo(HAPPINESS);
		assertThat(returnValue.get(RELEVANCE_KEY)).isEqualTo(RELEVANCE);
	}
}
