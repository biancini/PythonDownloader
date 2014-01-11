package it.elasticsearch.script;

import it.elasticsearch.models.ByStateReduceComputedHappiness;
import it.elasticsearch.script.facet.HappinessInternalFacet;
import it.elasticsearch.utilities.KmlUtilities;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.elasticsearch.client.Client;
import org.elasticsearch.common.logging.ESLogger;
import org.elasticsearch.common.logging.Loggers;
import org.elasticsearch.script.AbstractExecutableScript;

public class ByStateReduceScript extends AbstractExecutableScript {

	protected ESLogger logger = Loggers.getLogger("happiness.script");
	private Map<String, Object> params = null;

	public ByStateReduceScript(Map<String, Object> params) {
		super();
		logger.trace("Initializing bystate reduce script with params: {}.", params);
		this.params = params;
	}

	@Override
	@SuppressWarnings("unchecked")
	public Object run() {
		List<ByStateReduceComputedHappiness> combineResults = (List<ByStateReduceComputedHappiness>) params
				.get(HappinessInternalFacet.FACETS_TYPE);

		Client esClient = (Client) params.get("_client");
		List<ByStateReduceComputedHappiness> usaStates = KmlUtilities.getUsaStates(esClient);
		Map<String, Map<String, Object>> reduced = new HashMap<String, Map<String, Object>>();

		for (ByStateReduceComputedHappiness curState : usaStates) {
			for (ByStateReduceComputedHappiness curHappiness : combineResults) {
				if (curState.getStateName().equals(curHappiness.getStateName())) {
					curState.addScoreAndRelevanceElements(curHappiness.getScore(), curHappiness.getRelevance(),
							curHappiness.getNumelements());
				}

				Map<String, Object> curMap = curState.toMap();
				reduced.put(curState.getStateName(), curMap);
			}
		}

		return reduced;
	}
}