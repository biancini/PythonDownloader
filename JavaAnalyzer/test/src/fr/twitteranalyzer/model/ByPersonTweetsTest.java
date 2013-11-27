package fr.twitteranalyzer.model;

import static org.fest.assertions.Assertions.assertThat;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import org.elasticsearch.common.geo.GeoPoint;
import org.junit.Test;

public class ByPersonTweetsTest {

	public static final String ID = "123456";
	public static final String DATE = "1980-10-05";
	public static final String LOCATION = "Paris";
	public static final int NUMFRIENDS = 100;
	public static final double LATITUDE = 48.822768D;
	public static final double LONGITUDE = 2.345388D;
	public static final float HAPPINESS = 7.5F;
	public static final float RELEVANCE = 0.8F;

	public static final String DATE_KEY = "date";
	public static final String LOCATION_KEY = "location";
	public static final String NUMFRIENDS_KEY = "num_friends";
	public static final String COORDINATES_KEY = "coordinates";
	public static final String HAPPINESS_KEY = "happiness";
	public static final String RELEVANCE_KEY = "relevance";

	@Test
	public void shouldToJsonDocumentProduceRightHashMap() throws ParseException {
		// given
		SimpleDateFormat dateFormat = new SimpleDateFormat("YYYY-MM-DD");
		Date date = dateFormat.parse(DATE);
		GeoPoint coordinates = new GeoPoint(LATITUDE, LONGITUDE);

		ByPersonTweets byPersonTweets = new ByPersonTweets(ID);
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
