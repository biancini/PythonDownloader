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
		Map<String, Double> inputMap = new HashMap<String, Double>();
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
		Map<String, Double> inputMap = new HashMap<String, Double>();
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
		Map<String, Double> expectedMap = new HashMap<String, Double>();
		expectedMap.put(ComputedHappiness.SCORE_KEY, score);
		expectedMap.put(ComputedHappiness.RELEVANCE_KEY, relevance);
		expectedMap.put(GeolocalizedComputedHappiness.LATITUDE_KEY, lat);
		expectedMap.put(GeolocalizedComputedHappiness.LONGITUDE_KEY, lng);

		// when
		Map<String, Double> map = geoHappiness.toMap();

		// then
		assertThat(map).isNotNull();
		assertThat(map).isEqualTo(expectedMap);
	}
}