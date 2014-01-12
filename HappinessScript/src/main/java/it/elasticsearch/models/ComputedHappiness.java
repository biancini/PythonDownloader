package it.elasticsearch.models;

import java.util.HashMap;
import java.util.Map;

import org.elasticsearch.common.geo.GeoPoint;

public class ComputedHappiness {

	public static final String SCORE_KEY = "score";
	public static final String RELEVANCE_KEY = "relevance";
	public static final String NUMELEMENTS_KEY = "elements";
	public static final String LATITUDE_KEY = "lat";
	public static final String LONGITUDE_KEY = "lng";
	public static final String STATE_KEY = "state";

	protected double score = -1.;
	protected double relevance = -1.;
	protected int numelements = 0;

	protected boolean geolocalized = false;
	protected USAState state = null;

	private double lat = 0;
	private double lng = 0;

	public ComputedHappiness() {
		this.score = 0.;
		this.relevance = 0.;
		this.numelements = 0;
	}

	public ComputedHappiness(double score, double relevance) {
		this.score = score;
		this.relevance = relevance;
		this.numelements = 1;
	}

	public ComputedHappiness(double score, double relevance, int numelements) {
		this.score = score;
		this.relevance = relevance;
		this.numelements = numelements;
	}

	public ComputedHappiness(double score, double relevance, double lat, double lng) {
		this(score, relevance);

		this.geolocalized = true;
		this.lat = lat;
		this.lng = lng;
		this.numelements = 1;
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

		if (numelements != 1) {
			returnVal.put(NUMELEMENTS_KEY, numelements);
		}

		if (geolocalized) {
			returnVal.put(LATITUDE_KEY, lat);
			returnVal.put(LONGITUDE_KEY, lng);
		}

		if (state != null) {
			returnVal.put(STATE_KEY, state.toMap());
		}

		return returnVal;
	}

	public double getScore() {
		return score;
	}

	public double getRelevance() {
		return relevance;
	}

	public USAState getState() {
		return state;
	}

	public int getNumelements() {
		return numelements;
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

			if (happ.getNumelements() != numelements) {
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

			if (state != null && !state.equals(happ.getState())) {
				return false;
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

	public void setState(USAState state) {
		this.state = state;
	}

	public void addScoreAndRelevanceElements(ComputedHappiness happiness) {
		addScoreAndRelevanceElements(happiness.getScore(), happiness.getRelevance(), happiness.getNumelements());
	}

	public void addScoreAndRelevanceElements(double score, double relevance, int numelements) {
		double newScore = this.score * this.numelements + score * numelements;
		double newRelevance = this.relevance * this.numelements + relevance * numelements;

		this.numelements += numelements;
		this.score = newScore / this.numelements;
		this.relevance = newRelevance / this.numelements;
	}
}
