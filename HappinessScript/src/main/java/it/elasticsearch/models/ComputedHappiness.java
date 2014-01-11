package it.elasticsearch.models;

import java.util.HashMap;
import java.util.Map;

import org.elasticsearch.common.geo.GeoPoint;

public class ComputedHappiness {

	public static final String SCORE_KEY = "score";
	public static final String RELEVANCE_KEY = "relevance";
	public static final String LATITUDE_KEY = "lat";
	public static final String LONGITUDE_KEY = "lng";

	protected double score = -1.;
	protected double relevance = -1.;

	protected boolean geolocalized = false;

	private double lat = 0;
	private double lng = 0;

	public ComputedHappiness() {
		this.score = 0.;
		this.relevance = 0.;
	}

	public ComputedHappiness(double score, double relevance) {
		this.score = score;
		this.relevance = relevance;
	}

	public ComputedHappiness(double score, double relevance, double lat, double lng) {
		this(score, relevance);

		this.geolocalized = true;
		this.lat = lat;
		this.lng = lng;
	}

	public ComputedHappiness(Map<String, Object> vals) {
		this.score = (Double) vals.get(SCORE_KEY);
		this.relevance = (Double) vals.get(RELEVANCE_KEY);

		if (vals.containsKey(LATITUDE_KEY) && vals.containsKey(LONGITUDE_KEY)) {
			this.geolocalized = true;
			this.lat = (Double) vals.get(LATITUDE_KEY);
			this.lng = (Double) vals.get(LONGITUDE_KEY);
		}
	}

	public boolean isGeolocalized() {
		return geolocalized;
	}

	public double getLatitude() {
		return lat;
	}

	public double getLongitude() {
		return lng;
	}

	public Map<String, Object> toMap() {
		Map<String, Object> returnVal = new HashMap<String, Object>();
		returnVal.put(SCORE_KEY, score);
		returnVal.put(RELEVANCE_KEY, relevance);

		if (geolocalized) {
			returnVal.put(LATITUDE_KEY, lat);
			returnVal.put(LONGITUDE_KEY, lng);
		}

		return returnVal;
	}

	public double getScore() {
		return score;
	}

	public double getRelevance() {
		return relevance;
	}

	@Override
	public String toString() {
		return toMap().toString();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof ComputedHappiness) {
			ComputedHappiness happ = (ComputedHappiness) obj;

			if (happ.getScore() != score) {
				return false;
			}
			if (happ.getRelevance() != relevance) {
				return false;
			}

			if (happ.isGeolocalized() != geolocalized) {
				return false;
			}
			if (geolocalized) {
				if (happ.getLatitude() != lat) {
					return false;
				}
				if (happ.getLongitude() != lng) {
					return false;
				}
			}

			return true;
		}

		return false;
	}

	public void setCoordinates(GeoPoint coordinates) {
		if (coordinates == null) {
			geolocalized = false;
			lat = 0.;
			lng = 0.;
		} else {
			geolocalized = true;
			lat = coordinates.getLat();
			lng = coordinates.getLon();
		}
	}

}
