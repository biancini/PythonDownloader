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
	public void shouldInitializeFromMapWork() {
		// given
		Map<String, Object> inputMap = new HashMap<String, Object>();
		inputMap.put(ComputedHappiness.SCORE_KEY, score);
		inputMap.put(ComputedHappiness.RELEVANCE_KEY, relevance);

		// when
		ComputedHappiness happiness = new ComputedHappiness(inputMap);

		// then
		assertThat(happiness.getScore()).isEqualTo(score);
		assertThat(happiness.getRelevance()).isEqualTo(relevance);
	}

	@Test
	public void shouldToMapReturnValidMap() throws IOException {
		// given
		ComputedHappiness happiness = new ComputedHappiness(score, relevance);
		Map<String, Object> expectedMap = new HashMap<String, Object>();
		expectedMap.put(ComputedHappiness.SCORE_KEY, score);
		expectedMap.put(ComputedHappiness.RELEVANCE_KEY, relevance);

		// when
		Map<String, Object> map = happiness.toMap();

		// then
		assertThat(map).isNotNull();
		assertThat(map).isEqualTo(expectedMap);
	}
}