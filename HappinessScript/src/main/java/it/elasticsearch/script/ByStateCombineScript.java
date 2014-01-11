package it.elasticsearch.script;

import it.elasticsearch.models.ByStateReduceComputedHappiness;
import it.elasticsearch.models.ComputedHappiness;
import it.elasticsearch.script.facet.HappinessInternalFacet;
import it.elasticsearch.utilities.KmlUtilities;

import java.util.List;
import java.util.Map;

import org.elasticsearch.client.Client;
import org.elasticsearch.common.logging.ESLogger;
import org.elasticsearch.common.logging.Loggers;
import org.elasticsearch.script.AbstractExecutableScript;

public class ByStateCombineScript extends AbstractExecutableScript {

	protected ESLogger logger = Loggers.getLogger("happiness.script");
	private Map<String, Object> params = null;

	public ByStateCombineScript(Map<String, Object> params) {
		super();
		logger.trace("Initializing bystate combine script with params: {}.", params);
		this.params = params;
	}

	@Override
	@SuppressWarnings("unchecked")
	public Object run() {
		List<ComputedHappiness> searchResults = (List<ComputedHappiness>) params
				.get(HappinessInternalFacet.FACET_TYPE);

		Client esClient = (Client) params.get("_client");
		List<ByStateReduceComputedHappiness> usaStates = KmlUtilities.getUsaStates(esClient);

		for (ComputedHappiness curHappiness : searchResults) {
			if (!curHappiness.isGeolocalized()) {
				continue;
			}

			double curLat = curHappiness.getLatitude();
			double curLng = curHappiness.getLongitude();

			for (ByStateReduceComputedHappiness curState : usaStates) {
				if (KmlUtilities.isPointIntoRegion(curState.getStateGeometry(), curLat, curLng)) {
					curState.addScoreAndRelevance(curHappiness.getScore(), curHappiness.getRelevance());
				}
			}
		}

		return usaStates;
	}
}