package it.elasticsearch.models;

import java.util.HashMap;
import java.util.Map;

import com.vividsolutions.jts.geom.Geometry;

public class USAState {
	public static final String STATEID_KEY = "id";
	public static final String STATENAME_KEY = "name";

	private String stateId = null;
	private String stateName = null;
	private Geometry stateGeometry = null;

	public USAState(String stateId, String stateName) {
		this.stateId = stateId;
		this.stateName = stateName;
	}

	public USAState(String stateId, String stateName, Geometry stateGeometry) {
		this.stateId = stateId;
		this.stateName = stateName;
		this.stateGeometry = stateGeometry;
	}

	public String getStateId() {
		return stateId;
	}

	public String getStateName() {
		return stateName;
	}

	public Geometry getStateGeometry() {
		return stateGeometry;
	}

	public Map<String, Object> toMap() {
		Map<String, Object> returnVal = new HashMap<String, Object>();
		returnVal.put(STATEID_KEY, stateId);
		returnVal.put(STATENAME_KEY, stateName);

		return returnVal;
	}

	@Override
	public String toString() {
		return toMap().toString();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof USAState) {
			USAState state = (USAState) obj;

			if (!stateId.equals(state.getStateId())) {
				return false;
			}

			if (!stateName.equals(state.getStateName())) {
				return false;
			}

			return true;
		}

		return false;
	}

}
