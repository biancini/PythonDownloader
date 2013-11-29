package fr.twitteranalyzer.model;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.elasticsearch.common.geo.GeoPoint;

public class ByPersonTweets {
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

		if (id != null) {
			jsonDocument.put("userid", id);
		}

		if (date != null) {
			jsonDocument.put("date", date);
		}

		if (location != null) {
			jsonDocument.put("location", location);
		}

		if (numFriends != -1) {
			jsonDocument.put("num_friends", numFriends);
		}

		if (coordinates != null) {
			jsonDocument.put("coordinates", coordinates);
		}

		if (happiness != -1F) {
			jsonDocument.put("happiness", happiness);
		}

		if (relevance != -1F) {
			jsonDocument.put("relevance", relevance);
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
