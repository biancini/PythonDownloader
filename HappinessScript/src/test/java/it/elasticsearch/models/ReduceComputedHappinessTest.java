package it.elasticsearch.models;

import static org.fest.assertions.Assertions.assertThat;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

public class ReduceComputedHappinessTest {

	private double score = 7.;
	private double relevance = 1.;
	private int numelems = 10;

	@Test
	public void shouldToStringProduceNicePrint() {
		// given
		Map<String, Object> inputMap = new HashMap<String, Object>();
		inputMap.put(ReduceComputedHappiness.SCORE_KEY, score);
		inputMap.put(ReduceComputedHappiness.RELEVANCE_KEY, relevance);
		inputMap.put(ReduceComputedHappiness.NUMELEMS_KEY, new Integer(numelems));

		// when
		ReduceComputedHappiness happiness = new ReduceComputedHappiness(score, relevance, numelems);

		// then
		assertThat(happiness.toString()).isEqualTo(inputMap.toString());
	}

	@Test
	public void shouldInitializeFromMapWork() {
		// given
		Map<String, Object> inputMap = new HashMap<String, Object>();
		inputMap.put(ComputedHappiness.SCORE_KEY, score);
		inputMap.put(ComputedHappiness.RELEVANCE_KEY, relevance);
		inputMap.put(ReduceComputedHappiness.NUMELEMS_KEY, new Integer(numelems));

		// when
		ReduceComputedHappiness happiness = new ReduceComputedHappiness(inputMap);

		// then
		assertThat(happiness.getScore()).isEqualTo(score);
		assertThat(happiness.getRelevance()).isEqualTo(relevance);
		assertThat(happiness.getNumelements()).isEqualTo(numelems);
	}

	@Test
	public void shouldToMapReturnValidMap() throws IOException {
		// given
		ReduceComputedHappiness happiness = new ReduceComputedHappiness(score, relevance, numelems);
		Map<String, Object> expectedMap = new HashMap<String, Object>();
		expectedMap.put(ComputedHappiness.SCORE_KEY, score);
		expectedMap.put(ComputedHappiness.RELEVANCE_KEY, relevance);
		expectedMap.put(ReduceComputedHappiness.NUMELEMS_KEY, new Integer(numelems));

		// when
		Map<String, Object> map = happiness.toMap();

		// then
		assertThat(map).isNotNull();
		assertThat(map).isEqualTo(expectedMap);
	}
}