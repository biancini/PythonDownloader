package it.elasticsearch.models;

import java.util.Map;

import org.elasticsearch.common.geo.GeoPoint;

public class GeolocalizedComputedHappiness extends ComputedHappiness {

	public static final String LATITUDE_KEY = "lat";
	public static final String LONGITUDE_KEY = "lng";

	private double lat = 0;
	private double lng = 0;

	public GeolocalizedComputedHappiness(double score, double relevance, double lat, double lng) {
		super(score, relevance);
		this.lat = lat;
		this.lng = lng;
	}

	public GeolocalizedComputedHappiness(Map<String, Object> happiness, GeoPoint point) {
		super(happiness);

		this.lat = point.getLat();
		this.lng = point.getLon();
	}

	public GeolocalizedComputedHappiness(Map<String, Object> vals) {
		super(vals);

		this.lat = (Double) vals.get(LATITUDE_KEY);
		this.lng = (Double) vals.get(LONGITUDE_KEY);
	}

	@Override
	public Map<String, Object> toMap() {
		Map<String, Object> returnVal = super.toMap();

		returnVal.put(LATITUDE_KEY, lat);
		returnVal.put(LONGITUDE_KEY, lng);

		return returnVal;
	}

	public double getLatitude() {
		return lat;
	}

	public double getLongitude() {
		return lng;
	}

	@Override
	public String toString() {
		return toMap().toString();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof GeolocalizedComputedHappiness) {
			GeolocalizedComputedHappiness happ = (GeolocalizedComputedHappiness) obj;

			if (!super.equals(obj)) {
				return false;
			}

			if (happ.getLatitude() != lat) {
				return false;
			}
			if (happ.getLongitude() != lng) {
				return false;
			}

			return true;
		}

		return false;
	}

}
