package it.elasticsearch.models;

import java.util.HashMap;
import java.util.Map;

public class ComputedHappiness {

	public static final String SCORE_KEY = "score";
	public static final String RELEVANCE_KEY = "relevance";

	protected double score = -1.;
	protected double relevance = -1.;

	public ComputedHappiness() {
		this.score = 0.;
		this.relevance = 0.;
	}

	public ComputedHappiness(double score, double relevance) {
		this.score = score;
		this.relevance = relevance;
	}

	public ComputedHappiness(Map<String, Object> vals) {
		this.score = (Double) vals.get(SCORE_KEY);
		this.relevance = (Double) vals.get(RELEVANCE_KEY);
	}

	public Map<String, Object> toMap() {
		Map<String, Object> returnVal = new HashMap<String, Object>();
		returnVal.put(SCORE_KEY, score);
		returnVal.put(RELEVANCE_KEY, relevance);
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

			return true;
		}

		return false;
	}

}
