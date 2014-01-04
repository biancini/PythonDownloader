package it.elasticsearch.scripts.models;

import static org.fest.assertions.Assertions.assertThat;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

public class ComputedHappinessTest {

	private double score = 7.0;
	private double relevance = 1.0;

	@Test
	public void shouldToMapReturnValidMap() throws IOException {
		// given
		ComputedHappiness happiness = new ComputedHappiness(score, relevance);
		Map<String, Double> expectedMap = new HashMap<String, Double>();
		expectedMap.put(ComputedHappiness.SCORE_KEY, score);
		expectedMap.put(ComputedHappiness.RELEVANCE_KEY, relevance);

		// when
		Map<String, Double> map = happiness.toMap();

		// then
		assertThat(map).isNotNull();
		assertThat(map).isEqualTo(expectedMap);
	}
}