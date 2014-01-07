package it.elasticsearch.models;

import java.util.Map;

public class ReduceComputedHappiness extends ComputedHappiness {

	public static final String NUMELEMS_KEY = "elements";

	private double numelements = -1;

	public ReduceComputedHappiness(Map<String, Double> vals) {
		super(vals);
		this.numelements = vals.get(NUMELEMS_KEY);
	}

	public ReduceComputedHappiness(double score, double relevance, int numelements) {
		super(score, relevance);
		this.numelements = numelements;
	}

	public double getNumelements() {
		return numelements;
	}

	public Map<String, Double> toMap() {
		Map<String, Double> returnVal = super.toMap();
		returnVal.put(NUMELEMS_KEY, numelements);
		return returnVal;
	}

	@Override
	public String toString() {
		return toMap().toString();
	}
}
