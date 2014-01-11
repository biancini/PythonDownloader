package it.elasticsearch.models;

import static org.fest.assertions.Assertions.assertThat;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

public class HappinessInstantiatorTest {

	private double score = 7.;
	private double relevance = 1.;
	private double lng = -100.;
	private double lat = 40.;

	@Test
	public void shouldInstantiateReturnGeolocalizedIfLatAndLng() {
		// given
		Map<String, Object> inputMap = new HashMap<String, Object>();
		inputMap.put(ComputedHappiness.SCORE_KEY, score);
		inputMap.put(ComputedHappiness.RELEVANCE_KEY, relevance);
		inputMap.put(GeolocalizedComputedHappiness.LATITUDE_KEY, lat);
		inputMap.put(GeolocalizedComputedHappiness.LONGITUDE_KEY, lng);

		GeolocalizedComputedHappiness expectedResult = new GeolocalizedComputedHappiness(inputMap);

		// when
		ComputedHappiness happiness = HappinessInstantiator.instantiate(inputMap);

		// then
		assertThat(happiness).isEqualTo(expectedResult);
	}

	@Test
	public void shouldInstantiateReturnBaseObjectIfNoAttributeInMap() {
		// given
		Map<String, Object> inputMap = new HashMap<String, Object>();
		inputMap.put(ComputedHappiness.SCORE_KEY, score);
		inputMap.put(ComputedHappiness.RELEVANCE_KEY, relevance);

		ComputedHappiness expectedResult = new ComputedHappiness(inputMap);

		// when
		ComputedHappiness happiness = HappinessInstantiator.instantiate(inputMap);

		// then
		assertThat(happiness).isEqualTo(expectedResult);
	}

}