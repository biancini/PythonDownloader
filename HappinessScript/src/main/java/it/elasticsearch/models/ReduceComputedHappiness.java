package it.elasticsearch.models;

import java.util.Map;

public class ReduceComputedHappiness extends ComputedHappiness {

	public static final String NUMELEMS_KEY = "elements";

	protected int numelements = -1;

	public ReduceComputedHappiness(Map<String, Object> vals) {
		super(vals);
		this.numelements = (Integer) vals.get(NUMELEMS_KEY);
	}

	public ReduceComputedHappiness(double score, double relevance, int numelements) {
		super(score, relevance);
		this.numelements = numelements;
	}

	public int getNumelements() {
		return numelements;
	}

	@Override
	public Map<String, Object> toMap() {
		Map<String, Object> returnVal = super.toMap();
		returnVal.put(NUMELEMS_KEY, numelements);
		return returnVal;
	}

	@Override
	public String toString() {
		return toMap().toString();
	}
}
