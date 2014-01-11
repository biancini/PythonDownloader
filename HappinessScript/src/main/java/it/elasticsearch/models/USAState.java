package it.elasticsearch.models;

import java.util.HashMap;
import java.util.Map;

public class USAState {

	public static final String STATEID_KEY = "id";
	public static final String STATENAME_KEY = "name";
	public static final String STATEGEOMETRY_KEY = "geometry";

	private String stateId = null;
	private String stateName = null;
	private String stateGeometry = null;

	public USAState(String stateId, String stateName) {
		this.stateId = stateId;
		this.stateName = stateName;
	}

	public USAState(String stateId, String stateName, String stateGeometry) {
		this.stateId = stateId;
		this.stateName = stateName;
		this.stateGeometry = stateGeometry;
	}

	public USAState(Map<String, Object> vals) {
		this.stateId = (String) vals.get(STATEID_KEY);
		this.stateName = (String) vals.get(STATENAME_KEY);

		if (vals.containsKey(STATEGEOMETRY_KEY)) {
			this.stateGeometry = (String) vals.get(STATEGEOMETRY_KEY);
		}
	}

	public String getStateId() {
		return stateId;
	}

	public String getStateName() {
		return stateName;
	}

	public String getStateGeometry() {
		return stateGeometry;
	}

	public Map<String, Object> toMap() {
		Map<String, Object> returnVal = new HashMap<String, Object>();
		returnVal.put(STATEID_KEY, stateId);
		returnVal.put(STATENAME_KEY, stateName);

		// if (stateGeometry != null) {
		// returnVal.put(STATEGEOMETRY_KEY, stateGeometry);
		// }

		return returnVal;
	}
}
