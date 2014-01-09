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

	public GeolocalizedComputedHappiness(Map<String, Double> happiness, GeoPoint point) {
		super(happiness);

		this.lat = point.getLat();
		this.lng = point.getLon();
	}

	public GeolocalizedComputedHappiness(Map<String, Double> vals) {
		super(vals);

		this.lat = vals.get(LATITUDE_KEY);
		this.lat = vals.get(LONGITUDE_KEY);
	}

	public Map<String, Double> toMap() {
		Map<String, Double> returnVal = super.toMap();

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

}
