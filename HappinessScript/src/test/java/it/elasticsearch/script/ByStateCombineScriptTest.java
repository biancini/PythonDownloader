package it.elasticsearch.script;

import static org.fest.assertions.Assertions.assertThat;
import it.elasticsearch.models.ComputedHappiness;
import it.elasticsearch.models.USAState;
import it.elasticsearch.script.facet.HappinessInternalFacet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.elasticsearch.common.geo.GeoPoint;
import org.junit.Test;

public class ByStateCombineScriptTest {

	private double score1 = 7.;
	private double relevance1 = 1.;
	private double score2 = 5.;
	private double relevance2 = 0.5;
	private double score3 = 4.;
	private double relevance3 = 0.33;

	private String stateId1 = "NY";
	private String stateName1 = "New York";
	private String stateId2 = "CA";
	private String stateName2 = "California";

	@Test
	public void shouldRunWorkWhenTwoDifferentStatesPassed() throws IOException {
		// given
		USAState state1 = new USAState(stateId1, stateName1);
		USAState state2 = new USAState(stateId2, stateName2);

		ComputedHappiness happiness1 = new ComputedHappiness(score1, relevance1, 10., 10.);
		happiness1.setState(state1);

		ComputedHappiness happiness2 = new ComputedHappiness(score2, relevance2, 10., 10.);
		happiness2.setState(state2);

		List<ComputedHappiness> listFacets = new ArrayList<ComputedHappiness>();
		listFacets.add(happiness1);
		listFacets.add(happiness2);

		Map<String, Object> params = new HashMap<String, Object>();
		params.put(HappinessInternalFacet.FACET_TYPE, listFacets);
		ByStateCombineScript combineScript = new ByStateCombineScript(params);

		List<ComputedHappiness> expectedResult = new ArrayList<ComputedHappiness>();
		expectedResult.add(happiness1);
		expectedResult.add(happiness2);

		// when
		Object combineResult = combineScript.run();

		// then
		assertThat(combineResult).isNotNull();
		assertThat(combineResult).isEqualTo(expectedResult);
	}

	@Test
	public void shouldRunWorkWhenTwoEqualStatesPassed() throws IOException {
		// given
		USAState state1 = new USAState(stateId1, stateName1);

		ComputedHappiness happiness1 = new ComputedHappiness(score1, relevance1, 10., 10.);
		happiness1.setState(state1);

		ComputedHappiness happiness2 = new ComputedHappiness(score2, relevance2, 10., 10.);
		happiness2.setState(state1);

		List<ComputedHappiness> listFacets = new ArrayList<ComputedHappiness>();
		listFacets.add(happiness1);
		listFacets.add(happiness2);

		Map<String, Object> params = new HashMap<String, Object>();
		params.put(HappinessInternalFacet.FACET_TYPE, listFacets);
		ByStateCombineScript combineScript = new ByStateCombineScript(params);

		double expectedScore = (score1 + score2) / 2;
		double expectedRelevance = (relevance1 + relevance2) / 2;
		int expectedNumelems = 2;
		ComputedHappiness expectedHappiness = new ComputedHappiness(expectedScore, expectedRelevance,
				expectedNumelems);
		expectedHappiness.setCoordinates(new GeoPoint(10., 10.));
		expectedHappiness.setState(state1);

		List<ComputedHappiness> expectedResult = new ArrayList<ComputedHappiness>();
		expectedResult.add(expectedHappiness);

		// when
		Object combineResult = combineScript.run();

		// then
		assertThat(combineResult).isNotNull();
		assertThat(combineResult).isEqualTo(expectedResult);
	}

	@Test
	public void shouldRunWorkWhenTwoEqualAndOneDifferentStatesPassed() throws IOException {
		// given
		USAState state1 = new USAState(stateId1, stateName1);
		USAState state2 = new USAState(stateId2, stateName2);

		ComputedHappiness happiness1 = new ComputedHappiness(score1, relevance1, 10., 10.);
		happiness1.setState(state1);

		ComputedHappiness happiness2 = new ComputedHappiness(score2, relevance2, 10., 10.);
		happiness2.setState(state1);

		ComputedHappiness happiness3 = new ComputedHappiness(score3, relevance3, 10., 10.);
		happiness3.setState(state2);

		List<ComputedHappiness> listFacets = new ArrayList<ComputedHappiness>();
		listFacets.add(happiness1);
		listFacets.add(happiness2);
		listFacets.add(happiness3);

		Map<String, Object> params = new HashMap<String, Object>();
		params.put(HappinessInternalFacet.FACET_TYPE, listFacets);
		ByStateCombineScript combineScript = new ByStateCombineScript(params);

		double expectedScore = (score1 + score2) / 2;
		double expectedRelevance = (relevance1 + relevance2) / 2;
		int expectedNumelems = 2;
		ComputedHappiness expectedHappiness = new ComputedHappiness(expectedScore, expectedRelevance,
				expectedNumelems);
		expectedHappiness.setCoordinates(new GeoPoint(10., 10.));
		expectedHappiness.setState(state1);

		List<ComputedHappiness> expectedResult = new ArrayList<ComputedHappiness>();
		expectedResult.add(expectedHappiness);
		expectedResult.add(happiness3);

		// when
		Object combineResult = combineScript.run();

		// then
		assertThat(combineResult).isNotNull();
		assertThat(combineResult).isEqualTo(expectedResult);
	}

	@Test
	public void shouldUnwrapWorkForGoodInput() throws IOException {
		// given
		USAState state1 = new USAState(stateId1, stateName1);
		USAState state2 = new USAState(stateId2, stateName2);

		ComputedHappiness happiness1 = new ComputedHappiness(score1, relevance1, 10., 10.);
		happiness1.setState(state1);

		ComputedHappiness happiness2 = new ComputedHappiness(score2, relevance2, 10., 10.);
		happiness2.setState(state2);

		List<ComputedHappiness> listFacets = new ArrayList<ComputedHappiness>();
		listFacets.add(happiness1);
		listFacets.add(happiness2);

		List<Map<String, Object>> expectedResult = new ArrayList<Map<String, Object>>();

		Map<String, Object> map1 = happiness1.toMap();
		map1.remove(ComputedHappiness.LATITUDE_KEY);
		map1.remove(ComputedHappiness.LONGITUDE_KEY);
		expectedResult.add(map1);

		Map<String, Object> map2 = happiness2.toMap();
		map2.remove(ComputedHappiness.LATITUDE_KEY);
		map2.remove(ComputedHappiness.LONGITUDE_KEY);
		expectedResult.add(map2);

		ByStateCombineScript combineScript = new ByStateCombineScript(null);

		// when
		Object unwrapResult = combineScript.unwrap(listFacets);

		// then
		assertThat(unwrapResult).isNotNull();
		assertThat(unwrapResult).isEqualTo(expectedResult);
	}

}
