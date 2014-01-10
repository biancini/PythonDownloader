package it.elasticsearch.utilities;

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

	public static final String POINT_PRE = "{ \"type\": \"Feature\", \"geometry\": {\"type\": \"Point\", \"coordinates\": [";
	public static final String POINT_POST = "] }, \"properties\": { } }";

	private static Geometry getPoint(MfGeoJSONReaderForGoogle mfReader, double lat, double lng)
			throws JSONException {
		String pointJson = POINT_PRE + lng + "," + lat + POINT_POST;

		MfGeo mfGeo = mfReader.decode(pointJson);
		if (mfGeo.getGeoType().equals(MfGeo.GeoType.FEATURE)) {
			MfFeature mfFeature = (MfFeature) mfGeo;
			return mfFeature.getMfGeometry().getInternalGeometry();
		}

		throw new JSONException("Error while decodinf point geometry.");
	}

	public static boolean isPointIntoRegion(String geometry, double lat, double lng) throws JSONException {
		MfGeoFactory mfFactory = new MfGeoFactory() {
			public MfFeature createFeature(String id, MfGeometry geometry, JSONObject properties) {
				return new MyFeature(id, geometry, properties);
			}
		};

		MfGeoJSONReaderForGoogle mfReader = new MfGeoJSONReaderForGoogle(mfFactory);
		Geometry point = getPoint(mfReader, lat, lng);

		MfGeo mfGeo = mfReader.decode(geometry);
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
