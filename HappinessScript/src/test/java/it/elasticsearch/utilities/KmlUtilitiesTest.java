package it.elasticsearch.utilities;

import static org.fest.assertions.Assertions.assertThat;
import it.elasticsearch.models.USAState;

import java.io.IOException;
import java.util.List;

import org.junit.Ignore;
import org.junit.Test;

import com.vividsolutions.jts.geom.Geometry;

@Ignore
public class KmlUtilitiesTest {

	private static final int NYSTATE_ARRAYINDEX = 20;

	@Test
	public void shouldGetUsaStateWork() throws IOException {
		// given
		int numStates = USAStatesList.USA_STATES_IDS.length;

		// when
		List<USAState> usaStates = KmlUtilities.getUsaStates();

		// then
		assertThat(usaStates.size()).isEqualTo(numStates);
	}

	@Test
	public void shouldIsPointIntoRegionWorkForEveryState() throws IOException {
		// given
		double lat = 40.75;
		double lng = -74.0;

		// when
		for (int i = 0; i < USAStatesList.USA_STATES_IDS.length; ++i) {
			String stateId = USAStatesList.USA_STATES_IDS[i];
			String stateName = USAStatesList.USA_STATES_NAMES[i];
			Geometry stateGeometry = KmlUtilities.fromStringToGeometry(USAStatesList.USA_STATES_GEOMS[i]);
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
		Geometry nyStateGeometry = KmlUtilities
				.fromStringToGeometry(USAStatesList.USA_STATES_GEOMS[NYSTATE_ARRAYINDEX]);
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
		Geometry nyStateGeometry = KmlUtilities
				.fromStringToGeometry(USAStatesList.USA_STATES_GEOMS[NYSTATE_ARRAYINDEX]);
		USAState nyState = new USAState(nyStateId, nyStateName, nyStateGeometry);

		// when
		boolean isPointInRegion = KmlUtilities.isPointIntoRegion(nyState, lat, lng);

		// then
		assertThat(isPointInRegion).isFalse();
	}
}
