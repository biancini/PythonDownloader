package it.elasticsearch.script;

import static org.fest.assertions.Assertions.assertThat;
import it.elasticsearch.models.ComputedHappiness;
import it.elasticsearch.script.facet.HappinessInternalFacet;

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

		List<ComputedHappiness> expectedResult = new ArrayList<ComputedHappiness>();
		expectedResult.add(new ComputedHappiness(score1, relevance1, numelems));

		// when
		Object combineResult = combineScript.run();

		// then
		assertThat(combineResult).isNotNull();
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

		double score = (score1 + score2) / 2;
		double relevance = (relevance1 + relevance2) / 2;
		List<ComputedHappiness> expectedResult = new ArrayList<ComputedHappiness>();
		expectedResult.add(new ComputedHappiness(score, relevance, numelems));

		// when
		Object combineResult = combineScript.run();

		// then
		assertThat(combineResult).isNotNull();
		assertThat(combineResult).isEqualTo(expectedResult);
	}

	@Test
	public void shouldNewScriptReturnWhenParamsIsNotNullAndMultipleElems() throws IOException {
		// given
		int numelems1 = 2;
		int numelems2 = 3;
		List<ComputedHappiness> listFacets = new ArrayList<ComputedHappiness>();
		listFacets.add(new ComputedHappiness(score1, relevance1, numelems1));
		listFacets.add(new ComputedHappiness(score2, relevance2, numelems2));

		Map<String, Object> params = new HashMap<String, Object>();
		params.put(HappinessInternalFacet.FACET_TYPE, listFacets);
		CombineScript combineScript = new CombineScript(params);

		int numelems = numelems1 + numelems2;
		double score = (score1 * numelems1 + score2 * numelems2) / numelems;
		double relevance = (relevance1 * numelems1 + relevance2 * numelems2) / numelems;
		List<ComputedHappiness> expectedResult = new ArrayList<ComputedHappiness>();
		expectedResult.add(new ComputedHappiness(score, relevance, numelems));

		// when
		Object combineResult = combineScript.run();

		// then
		assertThat(combineResult).isNotNull();
		assertThat(combineResult).isEqualTo(expectedResult);
	}

	@Test
	public void shouldUnwrapWorkForGoodInput() throws IOException {
		// given
		ComputedHappiness happiness1 = new ComputedHappiness(score1, relevance1);
		ComputedHappiness happiness2 = new ComputedHappiness(score2, relevance2);

		List<ComputedHappiness> listFacets = new ArrayList<ComputedHappiness>();
		listFacets.add(happiness1);
		listFacets.add(happiness2);

		List<Map<String, Object>> expectedResult = new ArrayList<Map<String, Object>>();
		expectedResult.add(happiness1.toMap());
		expectedResult.add(happiness2.toMap());

		CombineScript combineScript = new CombineScript(null);

		// when
		Object unwrapResult = combineScript.unwrap(listFacets);

		// then
		assertThat(unwrapResult).isNotNull();
		assertThat(unwrapResult).isEqualTo(expectedResult);
	}

}
