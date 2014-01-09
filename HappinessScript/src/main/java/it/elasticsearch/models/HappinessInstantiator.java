package it.elasticsearch.models;

import java.util.Map;

public class HappinessInstantiator {

	public static ComputedHappiness instantiate(Map<String, Double> vals) {

		if (vals.containsKey(GeolocalizedComputedHappiness.LATITUDE_KEY)
				&& vals.containsKey(GeolocalizedComputedHappiness.LONGITUDE_KEY)) {
			return new GeolocalizedComputedHappiness(vals);
		}

		return new ComputedHappiness(vals);
	}

}
