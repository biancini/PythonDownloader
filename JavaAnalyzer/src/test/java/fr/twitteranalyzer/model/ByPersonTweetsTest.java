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

	@Test
	public void shouldSetterGetterWork() throws ParseException {
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

		// then
		assertThat(byPersonTweets.getId()).isEqualTo(USERID);
		assertThat(byPersonTweets.getDate()).isEqualTo(date);
		assertThat(byPersonTweets.getLocation()).isEqualTo(LOCATION);
		assertThat(byPersonTweets.getNumFriends()).isEqualTo(NUMFRIENDS);
		assertThat(byPersonTweets.getCoordinates()).isEqualTo(coordinates);
		assertThat(byPersonTweets.getHappiness()).isEqualTo(HAPPINESS);
		assertThat(byPersonTweets.getRelevance()).isEqualTo(RELEVANCE);
	}

	@Test
	public void shouldEmptyJsonBeCreated() throws ParseException {
		// given
		ByPersonTweets byPersonTweets = new ByPersonTweets(USERID);

		// when
		Map<String, Object> returnValue = byPersonTweets.toJsonDocument();

		// then
		assertThat(returnValue).isNotEmpty();
		assertThat(returnValue.get(ByPersonTweets.USERID_KEY)).isEqualTo(USERID);
		assertThat(returnValue.containsKey(ByPersonTweets.DATE_KEY)).isEqualTo(false);
		assertThat(returnValue.containsKey(ByPersonTweets.LOCATION_KEY)).isEqualTo(false);
		assertThat(returnValue.containsKey(ByPersonTweets.NUMFRIENDS_KEY)).isEqualTo(false);
		assertThat(returnValue.containsKey(ByPersonTweets.COORDINATES_KEY)).isEqualTo(false);
		assertThat(returnValue.containsKey(ByPersonTweets.HAPPINESS_KEY)).isEqualTo(false);
		assertThat(returnValue.containsKey(ByPersonTweets.RELEVANCE_KEY)).isEqualTo(false);
	}

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
		assertThat(returnValue.get(ByPersonTweets.USERID_KEY)).isEqualTo(USERID);
		assertThat(returnValue.get(ByPersonTweets.DATE_KEY)).isEqualTo(date);
		assertThat(returnValue.get(ByPersonTweets.LOCATION_KEY)).isEqualTo(LOCATION);
		assertThat(returnValue.get(ByPersonTweets.NUMFRIENDS_KEY)).isEqualTo(NUMFRIENDS);
		assertThat(returnValue.get(ByPersonTweets.COORDINATES_KEY)).isEqualTo(coordinates);
		assertThat(returnValue.get(ByPersonTweets.HAPPINESS_KEY)).isEqualTo(HAPPINESS);
		assertThat(returnValue.get(ByPersonTweets.RELEVANCE_KEY)).isEqualTo(RELEVANCE);
	}
}
