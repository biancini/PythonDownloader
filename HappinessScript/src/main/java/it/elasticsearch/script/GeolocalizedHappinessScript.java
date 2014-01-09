package it.elasticsearch.script;

import it.elasticsearch.models.GeolocalizedComputedHappiness;

import java.io.IOException;
import java.util.Map;
import java.util.Properties;

import org.elasticsearch.common.geo.GeoPoint;

public class GeolocalizedHappinessScript extends HappinessScript {

	public static final String COORDINATES_FIELDNAME = "coordinates";

	public GeolocalizedHappinessScript(Properties properties) throws IOException {
		super(properties);
	}

	// protected GeoPoint getCoordinates() {
	// GeoPoints coordinates = (GeoPoints) doc().get(COORDINATES_FIELDNAME);
	// return (coordinates != null) ? coordinates.getValue() : null;
	// }

	protected GeoPoint getCoordinates() {
		String strCoords = (String) source().get(COORDINATES_FIELDNAME);
		if (strCoords == null || strCoords.indexOf(',') < 0) {
			return null;
		}
		String[] coords = strCoords.split(",");
		GeoPoint geopoint = new GeoPoint(Double.parseDouble(coords[0]), Double.parseDouble(coords[1]));
		return geopoint;
	}

	@Override
	@SuppressWarnings("unchecked")
	public Object run() {
		GeoPoint coordinates = getCoordinates();

		if (coordinates != null) {
			Map<String, Double> happiness = (Map<String, Double>) super.run();

			GeolocalizedComputedHappiness geoHappiness = new GeolocalizedComputedHappiness(happiness, coordinates);
			return geoHappiness.toMap();
		}

		return null;
	}
}