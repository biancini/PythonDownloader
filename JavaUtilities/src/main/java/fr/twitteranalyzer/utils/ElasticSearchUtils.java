package fr.twitteranalyzer.utils;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.geo.GeoPoint;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.search.SearchHit;
import org.geotools.geojson.geom.GeometryJSON;
import org.geotools.geometry.jts.GeometryBuilder;
import org.json.JSONArray;
import org.json.JSONObject;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

import fr.twitteranalyzer.exceptions.UtilsException;
import fr.twitteranalyzer.model.ElasticSearchConnection;
import fr.twitteranalyzer.model.FrenchDepartment;

public final class ElasticSearchUtils {

	public static final String CLUSTER_NAME_PROPERTY = "cluster.name";

	public static final String INDEX_NAME = "twitter";
	public static final String FRENCHDEPTS_TYPE = "french_depts";

	public static final String NOMREG_FIELD = "NOM_REG";
	public static final String KML_FIELD = "KML";

	private ElasticSearchUtils() {
		// Do nothing
	}

	public static Client getElasticSearchClient(ElasticSearchConnection connection) {
		String clusterName = connection.getClusterName();
		String elasticSearchHost = connection.getElasticSearchHost();
		int elasticSearchPort = connection.getElasticSearchPort();

		Settings settings = ImmutableSettings.settingsBuilder().put(CLUSTER_NAME_PROPERTY, clusterName).build();

		TransportClient transportClient = new TransportClient(settings);
		transportClient = transportClient.addTransportAddress(new InetSocketTransportAddress(elasticSearchHost,
				elasticSearchPort));

		return transportClient;
	}

	protected static List<FrenchDepartment> getGeometries(Client client) throws UtilsException {
		int hugenumber = 10000000;

		SearchRequestBuilder requestBuilder = client.prepareSearch(INDEX_NAME).setTypes(FRENCHDEPTS_TYPE);
		requestBuilder.setFrom(0);
		requestBuilder.setSize(hugenumber);
		requestBuilder.setExplain(false);

		requestBuilder.addField(NOMREG_FIELD);
		requestBuilder.addField(KML_FIELD);
		SearchResponse response = requestBuilder.execute().actionGet();

		SearchHit[] hits = response.getHits().getHits();
		if (hits == null) {
			throw new UtilsException("Error in searching geometries in ElasticSearch.");
		}

		List<FrenchDepartment> allGeometries = new ArrayList<FrenchDepartment>();
		for (int i = 0; i < hits.length; ++i) {
			String curRegion = (String) hits[i].field(NOMREG_FIELD).getValue();
			String curGeometryStr = (String) hits[i].field(KML_FIELD).getValue();

			FrenchDepartment curDepartment = new FrenchDepartment(curRegion);

			GeometryCollection curGeometries = cleanJsonString(curGeometryStr);
			curDepartment.setGeometry(curGeometries);
			allGeometries.add(curDepartment);
		}
		return allGeometries;
	}

	protected static GeometryCollection cleanJsonString(String curJsonString) throws UtilsException {
		if (curJsonString == null) {
			return null;
		}

		curJsonString = curJsonString.replaceAll("\\\\'", "\"");

		JSONObject object = new JSONObject(curJsonString);
		if (object.has("geometry")) {
			object = object.getJSONObject("geometry");
			GeometryCollection curGeometries = extractGeometryCollectionFromPolygon(object);
			return curGeometries;
		} else if (object.has("type") && object.getString("type").equals("GeometryCollection")) {
			GeometryCollection curGeometries = extractGeometryCollection(object);
			return curGeometries;
		}

		throw new UtilsException("Object of type not supported");
	}

	protected static GeometryCollection extractGeometryCollection(JSONObject object) throws UtilsException {
		JSONArray geometries = (JSONArray) object.getJSONArray("geometries");
		Geometry[] geometryArray = new Geometry[geometries.length()];

		for (int i = 0; i < geometries.length(); ++i) {
			Polygon curPolygon = extractPolygon(geometries.getJSONObject(i));
			geometryArray[i] = curPolygon;
		}

		GeometryCollection curGeometries = new GeometryCollection(geometryArray, new GeometryFactory());
		return curGeometries;
	}

	protected static GeometryCollection extractGeometryCollectionFromPolygon(JSONObject object)
			throws UtilsException {
		Polygon geometry = extractPolygon(object);
		Geometry[] geometryArray = new Geometry[] { geometry };
		GeometryCollection curGeometries = new GeometryCollection(geometryArray, new GeometryFactory());
		return curGeometries;
	}

	protected static Polygon extractPolygon(JSONObject object) throws UtilsException {
		try {
			GeometryJSON gJson = new GeometryJSON();

			JSONArray coordinates = (JSONArray) object.getJSONArray("coordinates").get(0);
			JSONArray firstPoint = coordinates.getJSONArray(0);
			coordinates.put(firstPoint);

			Reader reader = new StringReader(object.toString());
			Polygon geometry = gJson.readPolygon(reader);
			return geometry;
		} catch (IOException e) {
			throw new UtilsException(e);
		}
	}

	public static String getRegionOfPoint(Client client, GeoPoint point) throws UtilsException {
		List<FrenchDepartment> allRegions = getGeometries(client);

		GeometryBuilder builder = new GeometryBuilder();
		Point gPoint = builder.point(point.getLat(), point.getLon());

		for (FrenchDepartment curRegion : allRegions) {
			GeometryCollection geometryCollection = curRegion.getGeometry();
			for (int i = 0; i < geometryCollection.getNumGeometries(); ++i) {
				Geometry curGeometry = geometryCollection.getGeometryN(i);
				if (curGeometry.contains(gPoint)) {
					return curRegion.getDepartmentName();
				}
			}
		}

		throw new UtilsException("Point not part of any French region");
	}
}
