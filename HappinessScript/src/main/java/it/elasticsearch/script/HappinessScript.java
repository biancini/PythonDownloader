package it.elasticsearch.script;

import it.elasticsearch.models.ComputedHappiness;
import it.elasticsearch.models.USAState;
import it.elasticsearch.utilities.Analyzer;
import it.elasticsearch.utilities.HappinessAnalyzer;
import it.elasticsearch.utilities.KmlUtilities;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.elasticsearch.common.Nullable;
import org.elasticsearch.common.geo.GeoPoint;
import org.elasticsearch.common.logging.ESLogger;
import org.elasticsearch.common.logging.Loggers;
import org.elasticsearch.script.AbstractSearchScript;

public class HappinessScript extends AbstractSearchScript {

	public static final String TEXT_FIELDNAME = "text";
	public static final String COORDINATES_FIELDNAME = "coordinates";

	public static final String PARAM_GEOLOCALIZED = "geolocalized";
	public static final String PARAM_USASTATE = "usa-state";

	private final ESLogger logger = Loggers.getLogger("happiness.script");
	protected Map<String, Object> params = null;
	protected Properties properties = null;
	protected Analyzer analyzer = null;

	public HappinessScript(@Nullable Map<String, Object> params, Properties properties) throws IOException {
		logger.debug("Initializing happiness script.");
		this.params = params;
		this.properties = properties;
		this.analyzer = new HappinessAnalyzer();
	}

	protected String getTweetText() {
		String tweetText = (String) source().get(TEXT_FIELDNAME);
		return tweetText;
	}

	// protected GeoPoint getCoordinates() {
	// GeoPoints coordinates = (GeoPoints) doc().get(COORDINATES_FIELDNAME);
	// return (coordinates != null) ? coordinates.getValue() : null;
	// }

	protected GeoPoint getCoordinates() {
		String strCoords = (String) source().get(COORDINATES_FIELDNAME);
		if (strCoords == null || strCoords.indexOf(',') < 0) {
			return null;
		}
		String[] coords = strCoords.split(",");
		GeoPoint geopoint = new GeoPoint(Double.parseDouble(coords[0]), Double.parseDouble(coords[1]));
		return geopoint;
	}

	// protected String getTweetId() {
	// ScriptDocValues.Strings fieldValue = (ScriptDocValues.Strings)
	// doc().get(ID_FIELDNAME);
	// if (fieldValue == null || fieldValue.getValues() == null) {
	// return null;
	// }
	//
	// String tweetId = fieldValue.getValue();
	// return tweetId;
	// }

	@Override
	public Object run() {
		String tweetText = getTweetText();

		if (tweetText == null) {
			return null;
		}

		if (logger != null) {
			logger.trace("Evaluating happiness on text: {}", tweetText);
		}

		ComputedHappiness happiness = analyzer.computeHappiness(tweetText, properties);

		if (happiness == null) {
			if (logger != null) {
				logger.error("Returned null value from compute happiness.");
			}
			return null;
		}

		if (params != null && "true".equalsIgnoreCase((String) params.get(PARAM_GEOLOCALIZED))) {
			GeoPoint coordinates = getCoordinates();

			if (coordinates != null) {
				happiness.setCoordinates(coordinates);

				if ("true".equalsIgnoreCase((String) params.get(PARAM_USASTATE))) {
					List<USAState> usaStates = KmlUtilities.getUsaStates();
					for (USAState curState : usaStates) {
						if (KmlUtilities.isPointIntoRegion(curState, coordinates.getLat(), coordinates.getLon())) {
							happiness.setState(curState);
						}
					}
				}
			}
		}

		if (logger != null) {
			logger.trace("Computed happiness: {}.", happiness);
		}

		return happiness;
	}

	@Override
	public Object unwrap(Object value) {
		if (value instanceof ComputedHappiness) {
			return ((ComputedHappiness) value).toMap();
		}
		return super.unwrap(value);
	}

}