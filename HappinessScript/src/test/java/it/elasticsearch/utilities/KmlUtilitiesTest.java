package it.elasticsearch.utilities;

import static org.fest.assertions.Assertions.assertThat;
import it.elasticsearch.models.USAState;

import java.io.IOException;

import org.junit.Test;

public class KmlUtilitiesTest {

	private static final int NYSTATE_ARRAYINDEX = 20;

	@Test
	public void shouldIsPointIntoRegionWorkForEveryState() throws IOException {
		// given
		double lat = 40.75;
		double lng = -74.0;

		// when
		for (int i = 0; i < USAStatesList.USA_STATES_IDS.length; ++i) {
			String stateId = USAStatesList.USA_STATES_IDS[i];
			String stateName = USAStatesList.USA_STATES_NAMES[i];
			String stateGeometry = USAStatesList.USA_STATES_GEOMS[i];
			USAState state = new USAState(stateId, stateName, stateGeometry);

			KmlUtilities.isPointIntoRegion(state, lat, lng);
		}

		// then
	}

	@Test
	public void shouldIsPointIntoRegionWorkForPointInState() throws IOException {
		// given
		double lat = 40.75;
		double lng = -74.0;

		String nyStateId = USAStatesList.USA_STATES_IDS[NYSTATE_ARRAYINDEX];
		String nyStateName = USAStatesList.USA_STATES_NAMES[NYSTATE_ARRAYINDEX];
		String nyStateGeometry = USAStatesList.USA_STATES_GEOMS[NYSTATE_ARRAYINDEX];
		USAState nyState = new USAState(nyStateId, nyStateName, nyStateGeometry);

		// when
		boolean isPointInRegion = KmlUtilities.isPointIntoRegion(nyState, lat, lng);

		// then
		assertThat(isPointInRegion).isTrue();
	}

	@Test
	public void shouldIsPointIntoRegionWorkForValidPointOutOfStates() throws IOException {
		// given
		double lat = 40.75;
		double lng = 0.0;

		String nyStateId = USAStatesList.USA_STATES_IDS[NYSTATE_ARRAYINDEX];
		String nyStateName = USAStatesList.USA_STATES_NAMES[NYSTATE_ARRAYINDEX];
		String nyStateGeometry = USAStatesList.USA_STATES_GEOMS[NYSTATE_ARRAYINDEX];
		USAState nyState = new USAState(nyStateId, nyStateName, nyStateGeometry);

		// when
		boolean isPointInRegion = KmlUtilities.isPointIntoRegion(nyState, lat, lng);

		// then
		assertThat(isPointInRegion).isFalse();
	}
}
