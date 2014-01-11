package it.elasticsearch.models;

import java.util.HashMap;
import java.util.Map;

public class ByStateReduceComputedHappiness extends ReduceComputedHappiness {

	public static final String STATE_KEY = "state";

	private String stateName = null;
	private String stateGeometry = null;

	public ByStateReduceComputedHappiness(String stateName) {
		super(0., 0., 0);
		this.stateName = stateName;
	}

	public ByStateReduceComputedHappiness(String stateName, String stateGeometry) {
		super(0., 0., 0);
		this.stateName = stateName;
		this.stateGeometry = stateGeometry;
	}

	public String getStateName() {
		return stateName;
	}

	public String getStateGeometry() {
		return stateGeometry;
	}

	public void addScoreAndRelevanceElements(double score, double relevance, int numelements) {
		this.score += score;
		this.relevance += relevance;
		this.numelements += numelements;
	}

	public void addScoreAndRelevance(double score, double relevance) {
		this.score += score;
		this.relevance += relevance;
		this.numelements++;
	}

	@Override
	public Map<String, Object> toMap() {
		Map<String, Object> returnVal = new HashMap<String, Object>();
		if (numelements != 0) {
			returnVal.put(SCORE_KEY, score / numelements);
			returnVal.put(RELEVANCE_KEY, relevance / numelements);
		} else {
			returnVal.put(SCORE_KEY, 0);
			returnVal.put(RELEVANCE_KEY, 0);
		}
		returnVal.put(NUMELEMS_KEY, numelements);
		returnVal.put(STATE_KEY, stateName);
		return returnVal;
	}
}
