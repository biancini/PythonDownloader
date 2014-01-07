package it.elasticsearch.script;

import static org.fest.assertions.Assertions.assertThat;
import it.elasticsearch.models.ComputedHappiness;
import it.elasticsearch.models.ReduceComputedHappiness;
import it.elasticsearch.script.HappinessInternalFacet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

public class CombineScriptTest {

	private double score1 = 7.;
	private double relevance1 = 1.;
	private double score2 = 5.;
	private double relevance2 = 0.5;

	@Test
	public void shouldRunWorkWhenOneElementPassed() throws IOException {
		// given
		int numelems = 1;
		List<ComputedHappiness> listFacets = new ArrayList<ComputedHappiness>();
		listFacets.add(new ComputedHappiness(score1, relevance1));

		Map<String, Object> params = new HashMap<String, Object>();
		params.put(HappinessInternalFacet.FACET_TYPE, listFacets);
		CombineScript combineScript = new CombineScript(params);

		Map<String, Double> expectedResult = new HashMap<String, Double>();
		expectedResult.put(ReduceComputedHappiness.SCORE_KEY, score1);
		expectedResult.put(ReduceComputedHappiness.RELEVANCE_KEY, relevance1);
		expectedResult.put(ReduceComputedHappiness.NUMELEMS_KEY, new Double(numelems));

		// when
		Object combineResult = combineScript.run();

		// then
		assertThat(combineResult).isNotNull();
		assertThat(combineResult instanceof Map<?, ?>).isTrue();
		assertThat(combineResult).isEqualTo(expectedResult);
	}

	@Test
	public void shouldNewScriptReturnWhenParamsIsNotNull() throws IOException {
		// given
		int numelems = 2;
		List<ComputedHappiness> listFacets = new ArrayList<ComputedHappiness>();
		listFacets.add(new ComputedHappiness(score1, relevance1));
		listFacets.add(new ComputedHappiness(score2, relevance2));

		Map<String, Object> params = new HashMap<String, Object>();
		params.put(HappinessInternalFacet.FACET_TYPE, listFacets);
		CombineScript combineScript = new CombineScript(params);

		Map<String, Double> expectedResult = new HashMap<String, Double>();
		expectedResult.put(ReduceComputedHappiness.SCORE_KEY, (score1 + score2) / 2);
		expectedResult.put(ReduceComputedHappiness.RELEVANCE_KEY, (relevance1 + relevance2) / 2);
		expectedResult.put(ReduceComputedHappiness.NUMELEMS_KEY, new Double(numelems));

		// when
		Object combineResult = combineScript.run();

		// then
		assertThat(combineResult).isNotNull();
		assertThat(combineResult instanceof Map<?, ?>).isTrue();
		assertThat(combineResult).isEqualTo(expectedResult);
	}

}
