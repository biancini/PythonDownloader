package it.elasticsearch.models;

import static org.fest.assertions.Assertions.assertThat;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

public class ComputedHappinessTest {

	private double score = 7.;
	private double relevance = 1.;

	@Test
	public void shouldToStringProduceNicePrint() {
		// given
		Map<String, Object> inputMap = new HashMap<String, Object>();
		inputMap.put(ComputedHappiness.SCORE_KEY, score);
		inputMap.put(ComputedHappiness.RELEVANCE_KEY, relevance);

		// when
		ComputedHappiness happiness = new ComputedHappiness(score, relevance);

		// then
		assertThat(happiness.toString()).isEqualTo(inputMap.toString());
	}

	@Test
	public void shouldToMapReturnValidMap() throws IOException {
		// given
		Map<String, Object> expectedMap = new HashMap<String, Object>();
		expectedMap.put(ComputedHappiness.SCORE_KEY, score);
		expectedMap.put(ComputedHappiness.RELEVANCE_KEY, relevance);

		ComputedHappiness happiness = new ComputedHappiness(score, relevance);

		// when
		Map<String, Object> map = happiness.toMap();

		// then
		assertThat(map).isNotNull();
		assertThat(map).isEqualTo(expectedMap);
	}

	@Test
	public void shouldToMapReturnValidMapForGeolocalized() throws IOException {
		// given
		double lat = 40.75;
		double lng = -74.0;

		Map<String, Object> expectedMap = new HashMap<String, Object>();
		expectedMap.put(ComputedHappiness.SCORE_KEY, score);
		expectedMap.put(ComputedHappiness.RELEVANCE_KEY, relevance);
		expectedMap.put(ComputedHappiness.LATITUDE_KEY, lat);
		expectedMap.put(ComputedHappiness.LONGITUDE_KEY, lng);

		ComputedHappiness happiness = new ComputedHappiness(score, relevance, lat, lng);

		// when
		Map<String, Object> map = happiness.toMap();

		// then
		assertThat(map).isNotNull();
		assertThat(map).isEqualTo(expectedMap);
	}

	@Test
	public void shouldToMapReturnValidMapForGeolocalizedWithState() throws IOException {
		// given
		double lat = 40.75;
		double lng = -74.0;
		String stateId = "NY";
		String stateName = "New York";

		Map<String, Object> expectedState = new HashMap<String, Object>();
		expectedState.put(USAState.STATEID_KEY, stateId);
		expectedState.put(USAState.STATENAME_KEY, stateName);

		Map<String, Object> expectedMap = new HashMap<String, Object>();
		expectedMap.put(ComputedHappiness.SCORE_KEY, score);
		expectedMap.put(ComputedHappiness.RELEVANCE_KEY, relevance);
		expectedMap.put(ComputedHappiness.LATITUDE_KEY, lat);
		expectedMap.put(ComputedHappiness.LONGITUDE_KEY, lng);
		expectedMap.put(ComputedHappiness.STATE_KEY, expectedState);

		USAState state = new USAState(stateId, stateName);
		ComputedHappiness happiness = new ComputedHappiness(score, relevance, lat, lng);
		happiness.setState(state);

		// when
		Map<String, Object> map = happiness.toMap();

		// then
		assertThat(map).isNotNull();
		assertThat(map).isEqualTo(expectedMap);
	}
}