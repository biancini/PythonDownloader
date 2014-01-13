package it.elasticsearch.utilities;

import it.elasticsearch.models.USAState;

import java.util.ArrayList;
import java.util.List;

import org.elasticsearch.common.logging.ESLogger;
import org.elasticsearch.common.logging.Loggers;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONWriter;
import org.mapfish.geo.MfFeature;
import org.mapfish.geo.MfGeo;
import org.mapfish.geo.MfGeoFactory;
import org.mapfish.geo.MfGeoJSONReaderForGoogle;
import org.mapfish.geo.MfGeometry;
import org.mapfish.geo.MfGeometryCollection;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;

public class KmlUtilities {

	protected static final ESLogger logger = Loggers.getLogger("happiness.script");

	public static final String TWITTER_INDEX = "twitter";
	public static final String USASTATES_TYPE = "usa_states";

	private static final List<USAState> usaStates = initializeUsaStates();

	private static List<USAState> initializeUsaStates() {
		List<USAState> newUsaStates = new ArrayList<USAState>();

		for (int i = 0; i < USAStatesList.USA_STATES_IDS.length; ++i) {
			String stateId = USAStatesList.USA_STATES_IDS[i];
			String stateName = USAStatesList.USA_STATES_NAMES[i];
			String stateGeometry = USAStatesList.USA_STATES_GEOMS[i];

			if (stateName == null || stateGeometry == null) {
				logger.warn("State name or geometry null, ignoring this state.");
			} else {
				Geometry geometry = fromStringToGeometry(stateGeometry);
				newUsaStates.add(new USAState(stateId, stateName, geometry));
			}
		}

		return newUsaStates;
	}

	public synchronized static List<USAState> getUsaStates() {
		return usaStates;
	}

	/*
	 * public static List<USAState> getUsaStates(Client esClient) {
	 * List<USAState> usaStates = new ArrayList<USAState>();
	 * 
	 * SearchResponse response =
	 * esClient.prepareSearch(TWITTER_INDEX).setTypes(USASTATES_TYPE)
	 * .setQuery(QueryBuilders
	 * .matchAllQuery()).setSize(100).execute().actionGet();
	 * 
	 * for (SearchHit curHit : response.getHits()) { String stateName = (String)
	 * curHit.getSource().get("name"); String stateGeometry = (String)
	 * curHit.getSource().get("geometry");
	 * 
	 * if (stateName == null || stateGeometry == null) {
	 * logger.warn("State name or geometry null, ignoring this state."); } else
	 * { usaStates.add(new USAState(stateName, stateGeometry)); } }
	 * 
	 * return usaStates; }
	 */

	private static Geometry getPoint(double lat, double lng) throws JSONException {
		GeometryFactory factory = new GeometryFactory();
		Point point = factory.createPoint(new Coordinate(lng, lat));
		return point;
	}

	public static Geometry fromStringToGeometry(String geometry) {
		MfGeoFactory mfFactory = new MfGeoFactory() {
			public MfFeature createFeature(String id, MfGeometry geometry, JSONObject properties) {
				return new MyFeature(id, geometry, properties);
			}
		};

		MfGeoJSONReaderForGoogle mfReader = new MfGeoJSONReaderForGoogle(mfFactory);

		JSONObject json = new JSONObject(geometry);
		if (json.has("geometry")) {
			json = json.getJSONObject("geometry");
		}

		MfGeo mfGeo = mfReader.decode(json);

		if (mfGeo.getGeoType().equals(MfGeo.GeoType.GEOMETRYCOLLECTION)) {
			Geometry geo = ((MfGeometryCollection) mfGeo).getInternalGeometry();
			return geo;
		} else if (mfGeo.getGeoType().equals(MfGeo.GeoType.GEOMETRY)) {
			Geometry geo = ((MfGeometry) mfGeo).getInternalGeometry();
			return geo;
		}

		throw new IllegalArgumentException("State geometry is not valid.");
	}

	public static boolean isPointIntoRegion(USAState state, double lat, double lng) {
		Geometry geometry = state.getStateGeometry();
		Geometry point = getPoint(lat, lng);

		for (int i = 0; i < geometry.getNumGeometries(); ++i) {
			Geometry stateGeometry = geometry.getGeometryN(i);
			if (point.within(stateGeometry)) {
				return true;
			}
		}

		return false;
	}

	private static class MyFeature extends MfFeature {
		private String id = null;
		private MfGeometry geometry = null;

		// private JSONObject properties = null;

		public MyFeature(String id, MfGeometry geometry, JSONObject properties) {
			this.id = id;
			this.geometry = geometry;
			// this.properties = properties;
		}

		public String getFeatureId() {
			return id;
		}

		public MfGeometry getMfGeometry() {
			return geometry;
		}

		public void toJSON(JSONWriter builder) throws JSONException {
			throw new RuntimeException("Not implemented");
		}
	}

}
