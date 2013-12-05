package fr.twitteranalyzer.utils;

import java.util.List;

import org.elasticsearch.common.geo.GeoPoint;

public final class CoordinatesUtils {

	private CoordinatesUtils() {
		// Do nothing
	}

	public static String geopointToString(GeoPoint value) {
		if (value == null) {
			return null;
		}

		String strValue = Double.toString(value.getLat());
		strValue += ",";
		strValue += Double.toString(value.getLon());
		return strValue;
	}

	public static GeoPoint geopointFromString(String strValue) {
		float[] latLng = fromStringToLatLng(strValue);
		GeoPoint value = new GeoPoint(latLng[0], latLng[1]);
		return value;
	}

	public static float[] fromStringToLatLng(String strValue) {
		String[] strValues = strValue.split(",");

		float[] floatValues = new float[2];
		floatValues[0] = Float.parseFloat(strValues[0]);
		floatValues[1] = Float.parseFloat(strValues[1]);

		return floatValues;
	}

	public static boolean isPointInKml(GeoPoint point, List<GeoPoint> coordinates) {
		for (GeoPoint coordinate : coordinates) {
			System.out.println(coordinate.getLat());
			System.out.println(coordinate.getLon());
		}

		return false;
	}
}
