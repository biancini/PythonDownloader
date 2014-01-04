package it.elasticsearch.scripts.models;

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

	public Map<String, Double> toMap() {
		Map<String, Double> returnVal = new HashMap<String, Double>();
		returnVal.put(SCORE_KEY, score);
		returnVal.put(RELEVANCE_KEY, relevance);
		return returnVal;
	}

	public double getScore() {
		return score;
	}

	protected void setScore(double score) {
		this.score = score;
	}

	public double getRelevance() {
		return relevance;
	}

	protected void setRelevance(double relevance) {
		this.relevance = relevance;
	}

}
