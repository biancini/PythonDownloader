package it.elasticsearch.utilities;

import it.elasticsearch.models.ByStateReduceComputedHappiness;

import java.util.ArrayList;
import java.util.List;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.logging.ESLogger;
import org.elasticsearch.common.logging.Loggers;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONWriter;
import org.mapfish.geo.MfFeature;
import org.mapfish.geo.MfGeo;
import org.mapfish.geo.MfGeoFactory;
import org.mapfish.geo.MfGeoJSONReaderForGoogle;
import org.mapfish.geo.MfGeometry;
import org.mapfish.geo.MfGeometryCollection;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;

public class KmlUtilities {

	protected static ESLogger logger = Loggers.getLogger("happiness.script");

	public static final String TWITTER_INDEX = "twitter";
	public static final String USASTATES_TYPE = "usa_states";

	private static final String POINT_PRE = "{ \"type\": \"Feature\", \"geometry\": {\"type\": \"Point\", \"coordinates\": [";
	private static final String POINT_POST = "] }, \"properties\": { } }";

	public static List<ByStateReduceComputedHappiness> getUsaStates(Client esClient) {
		List<ByStateReduceComputedHappiness> usaStates = new ArrayList<ByStateReduceComputedHappiness>();

		SearchResponse response = esClient.prepareSearch(TWITTER_INDEX).setTypes(USASTATES_TYPE)
				.setQuery(QueryBuilders.matchAllQuery()).setSize(100).execute().actionGet();

		for (SearchHit curHit : response.getHits()) {
			String stateName = (String) curHit.getSource().get("name");
			String stateGeometry = (String) curHit.getSource().get("geometry");

			if (stateName == null || stateGeometry == null) {
				logger.warn("State name or geometry null, ignoring this state.");
			} else {
				usaStates.add(new ByStateReduceComputedHappiness(stateName, stateGeometry));
			}
		}

		return usaStates;
	}

	private static Geometry getPoint(MfGeoJSONReaderForGoogle mfReader, double lat, double lng)
			throws JSONException {
		String pointJson = POINT_PRE + lng + "," + lat + POINT_POST;

		MfGeo mfGeo = mfReader.decode(pointJson);
		if (mfGeo.getGeoType().equals(MfGeo.GeoType.FEATURE)) {
			MfFeature mfFeature = (MfFeature) mfGeo;
			return mfFeature.getMfGeometry().getInternalGeometry();
		}

		throw new JSONException("Error while decoding point geometry.");
	}

	public static boolean isPointIntoRegion(String geometry, double lat, double lng) {
		try {
			MfGeoFactory mfFactory = new MfGeoFactory() {
				public MfFeature createFeature(String id, MfGeometry geometry, JSONObject properties) {
					return new MyFeature(id, geometry, properties);
				}
			};

			MfGeoJSONReaderForGoogle mfReader = new MfGeoJSONReaderForGoogle(mfFactory);
			Geometry point = getPoint(mfReader, lat, lng);

			JSONObject json = new JSONObject(geometry);
			if (json.has("geometry")) {
				json = json.getJSONObject("geometry");
			}
			MfGeo mfGeo = mfReader.decode(json);

			if (mfGeo.getGeoType().equals(MfGeo.GeoType.GEOMETRYCOLLECTION)) {
				Geometry stateGC = ((MfGeometryCollection) mfGeo).getInternalGeometry();
				GeometryCollection stateGeometryCollection = (GeometryCollection) stateGC;

				for (int i = 0; i < stateGeometryCollection.getNumGeometries(); ++i) {
					Geometry stateGeometry = stateGeometryCollection.getGeometryN(i);
					if (stateGeometry.contains(point)) {
						return true;
					}
				}
			} else if (mfGeo.getGeoType().equals(MfGeo.GeoType.GEOMETRY)) {
				Geometry stateGeometry = ((MfGeometry) mfGeo).getInternalGeometry();
				if (stateGeometry.contains(point)) {
					return true;
				}
			}

			return false;
		} catch (JSONException e) {
			logger.error("Error while decoding geometry.", e);
			logger.debug("Current geometry: {}", geometry);
			throw e;
		}
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
