package it.elasticsearch.models;

import static org.fest.assertions.Assertions.assertThat;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

public class GeolocalizedComputedHappinessTest {

	private double score = 7.;
	private double relevance = 1.;
	private double lat = -100;
	private double lng = 40;

	@Test
	public void shouldToStringProduceNicePrint() {
		// given
		Map<String, Object> inputMap = new HashMap<String, Object>();
		inputMap.put(GeolocalizedComputedHappiness.SCORE_KEY, score);
		inputMap.put(GeolocalizedComputedHappiness.RELEVANCE_KEY, relevance);
		inputMap.put(GeolocalizedComputedHappiness.LATITUDE_KEY, lat);
		inputMap.put(GeolocalizedComputedHappiness.LONGITUDE_KEY, lng);

		// when
		GeolocalizedComputedHappiness geoHappiness = new GeolocalizedComputedHappiness(score, relevance, lat, lng);

		// then
		assertThat(geoHappiness.toString()).isEqualTo(inputMap.toString());
	}

	@Test
	public void shouldInitializeFromMapWork() {
		// given
		Map<String, Object> inputMap = new HashMap<String, Object>();
		inputMap.put(GeolocalizedComputedHappiness.SCORE_KEY, score);
		inputMap.put(GeolocalizedComputedHappiness.RELEVANCE_KEY, relevance);
		inputMap.put(GeolocalizedComputedHappiness.LATITUDE_KEY, lat);
		inputMap.put(GeolocalizedComputedHappiness.LONGITUDE_KEY, lng);

		// when
		GeolocalizedComputedHappiness geoHappiness = new GeolocalizedComputedHappiness(inputMap);

		// then
		assertThat(geoHappiness.getScore()).isEqualTo(score);
		assertThat(geoHappiness.getRelevance()).isEqualTo(relevance);
	}

	@Test
	public void shouldToMapReturnValidMap() throws IOException {
		// given
		GeolocalizedComputedHappiness geoHappiness = new GeolocalizedComputedHappiness(score, relevance, lat, lng);
		Map<String, Object> expectedMap = new HashMap<String, Object>();
		expectedMap.put(ComputedHappiness.SCORE_KEY, score);
		expectedMap.put(ComputedHappiness.RELEVANCE_KEY, relevance);
		expectedMap.put(GeolocalizedComputedHappiness.LATITUDE_KEY, lat);
		expectedMap.put(GeolocalizedComputedHappiness.LONGITUDE_KEY, lng);

		// when
		Map<String, Object> map = geoHappiness.toMap();

		// then
		assertThat(map).isNotNull();
		assertThat(map).isEqualTo(expectedMap);
	}
}