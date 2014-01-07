package it.elasticsearch.script;

import static org.fest.assertions.Assertions.assertThat;
import it.elasticsearch.models.ReduceComputedHappiness;
import it.elasticsearch.script.HappinessInternalFacet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

public class ReduceScriptTest {

	private double score1 = 7.;
	private double relevance1 = 1.;
	private int numelems1 = 10;
	private double score2 = 5.;
	private double relevance2 = 0.5;
	private int numelems2 = 5;

	@Test
	public void shouldRunWorkWhenOneElementPassed() throws IOException {
		// given
		List<ReduceComputedHappiness> listFacets = new ArrayList<ReduceComputedHappiness>();
		listFacets.add(new ReduceComputedHappiness(score1, relevance1, numelems1));

		Map<String, Object> params = new HashMap<String, Object>();
		params.put(HappinessInternalFacet.FACETS_TYPE, listFacets);
		ReduceScript reduceScript = new ReduceScript(params);

		Map<String, Double> expectedResult = new HashMap<String, Double>();
		expectedResult.put(ReduceComputedHappiness.SCORE_KEY, score1);
		expectedResult.put(ReduceComputedHappiness.RELEVANCE_KEY, relevance1);
		expectedResult.put(ReduceComputedHappiness.NUMELEMS_KEY, new Double(numelems1));

		// when
		Object combineResult = reduceScript.run();

		// then
		assertThat(combineResult).isNotNull();
		assertThat(combineResult instanceof Map<?, ?>).isTrue();
		assertThat(combineResult).isEqualTo(expectedResult);
	}

	@Test
	public void shouldNewScriptReturnWhenParamsIsNotNull() throws IOException {
		// given
		List<ReduceComputedHappiness> listFacets = new ArrayList<ReduceComputedHappiness>();
		listFacets.add(new ReduceComputedHappiness(score1, relevance1, numelems1));
		listFacets.add(new ReduceComputedHappiness(score2, relevance2, numelems2));

		Map<String, Object> params = new HashMap<String, Object>();
		params.put(HappinessInternalFacet.FACETS_TYPE, listFacets);
		ReduceScript reduceScript = new ReduceScript(params);

		Map<String, Double> expectedResult = new HashMap<String, Double>();
		expectedResult.put(ReduceComputedHappiness.SCORE_KEY, (score1 * numelems1 + score2 * numelems2)
				/ (numelems1 + numelems2));
		expectedResult.put(ReduceComputedHappiness.RELEVANCE_KEY,
				(relevance1 * numelems1 + relevance2 * numelems2) / (numelems1 + numelems2));
		expectedResult.put(ReduceComputedHappiness.NUMELEMS_KEY, new Double(numelems1 + numelems2));

		// when
		Object combineResult = reduceScript.run();

		// then
		assertThat(combineResult).isNotNull();
		assertThat(combineResult instanceof Map<?, ?>).isTrue();
		assertThat(combineResult).isEqualTo(expectedResult);
	}

}
