package it.elasticsearch.models;

import java.util.HashMap;
import java.util.Map;

public class ComputedHappiness {

	public static final String SCORE_KEY = "score";
	public static final String RELEVANCE_KEY = "relevance";

	private double score = -1.;
	private double relevance = -1.;

	public ComputedHappiness(double score, double relevance) {
		this.score = score;
		this.relevance = relevance;
	}

	public ComputedHappiness(Map<String, Double> vals) {
		this.score = vals.get(SCORE_KEY);
		this.relevance = vals.get(RELEVANCE_KEY);
	}

	public Map<String, Double> toMap() {
		Map<String, Double> returnVal = new HashMap<String, Double>();
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
