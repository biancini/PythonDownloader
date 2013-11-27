package fr.twitteranalyzer.utils;

import static org.fest.assertions.Assertions.assertThat;

import org.elasticsearch.common.geo.GeoPoint;
import org.junit.Test;

public class CoordinatesUtilsTest {

	private static final float LATITUDE = 48.822768F;
	private static final float LONGITUDE = 2.345388F;
	private static final String SEPARATOR = ",";

	@Test
	public void shouldFromStringToLatLngReturnRightValues() {
		// given
		String inputString = LATITUDE + SEPARATOR + LONGITUDE;

		// when
		float[] returnValue = CoordinatesUtils.fromStringToLatLng(inputString);

		// then
		assertThat(returnValue[0]).isEqualTo(LATITUDE);
		assertThat(returnValue[1]).isEqualTo(LONGITUDE);
	}

	@Test
	public void shouldGeopointFromStringReturnRightValues() {
		// given
		String inputString = LATITUDE + SEPARATOR + LONGITUDE;

		// when
		GeoPoint returnValue = CoordinatesUtils.geopointFromString(inputString);

		// then
		assertThat(returnValue.getLat()).isEqualTo(LATITUDE);
		assertThat(returnValue.getLon()).isEqualTo(LONGITUDE);
	}
}
