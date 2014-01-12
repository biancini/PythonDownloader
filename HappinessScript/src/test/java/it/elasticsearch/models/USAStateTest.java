package it.elasticsearch.models;

import static org.fest.assertions.Assertions.assertThat;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

public class USAStateTest {

	String stateId = "NY";
	String stateName = "New York";

	@Test
	public void shouldToStringProduceNicePrint() {
		// given
		Map<String, Object> inputMap = new HashMap<String, Object>();
		inputMap.put(USAState.STATEID_KEY, stateId);
		inputMap.put(USAState.STATENAME_KEY, stateName);

		// when
		USAState state = new USAState(stateId, stateName);

		// then
		assertThat(state.toString()).isEqualTo(inputMap.toString());
	}

	@Test
	public void shouldToMapReturnValidMap() throws IOException {
		// given
		Map<String, Object> expectedMap = new HashMap<String, Object>();
		expectedMap.put(USAState.STATEID_KEY, stateId);
		expectedMap.put(USAState.STATENAME_KEY, stateName);

		USAState state = new USAState(stateId, stateName);

		// when
		Map<String, Object> map = state.toMap();

		// then
		assertThat(map).isNotNull();
		assertThat(map).isEqualTo(expectedMap);
	}

}