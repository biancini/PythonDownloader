package fr.twitteranalyzer.model;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.elasticsearch.common.geo.GeoPoint;

public class ByPersonTweets {
	public static final String USERID_KEY = "userid";
	public static final String DATE_KEY = "date";
	public static final String LOCATION_KEY = "location";
	public static final String NUMFRIENDS_KEY = "num_friends";
	public static final String COORDINATES_KEY = "coordinates";
	public static final String HAPPINESS_KEY = "happiness";
	public static final String RELEVANCE_KEY = "relevance";

	Long id = null;
	Date date = null;
	String location = null;
	int numFriends = -1;
	GeoPoint coordinates = null;
	float happiness = -1F;
	float relevance = -1F;

	public ByPersonTweets(Long id) {
		this.id = id;
	}

	public Map<String, Object> toJsonDocument() {
		Map<String, Object> jsonDocument = new HashMap<String, Object>();

		jsonDocument.put(USERID_KEY, id);

		if (date != null) {
			jsonDocument.put(DATE_KEY, date);
		}

		if (location != null) {
			jsonDocument.put(LOCATION_KEY, location);
		}

		if (numFriends != -1) {
			jsonDocument.put(NUMFRIENDS_KEY, numFriends);
		}

		if (coordinates != null) {
			jsonDocument.put(COORDINATES_KEY, coordinates);
		}

		if (happiness != -1F) {
			jsonDocument.put(HAPPINESS_KEY, happiness);
		}

		if (relevance != -1F) {
			jsonDocument.put(RELEVANCE_KEY, relevance);
		}

		return jsonDocument;
	}

	public Long getId() {
		return id;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public int getNumFriends() {
		return numFriends;
	}

	public void setNumFriends(int numFriends) {
		this.numFriends = numFriends;
	}

	public GeoPoint getCoordinates() {
		return coordinates;
	}

	public void setCoordinates(GeoPoint coordinates) {
		this.coordinates = coordinates;
	}

	public float getHappiness() {
		return happiness;
	}

	public void setHappiness(float happiness) {
		this.happiness = happiness;
	}

	public float getRelevance() {
		return relevance;
	}

	public void setRelevance(float relevance) {
		this.relevance = relevance;
	}

}
