package it.elasticsearch.script;

import it.elasticsearch.models.ComputedHappiness;
import it.elasticsearch.script.facet.HappinessInternalFacet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.elasticsearch.common.logging.ESLogger;
import org.elasticsearch.common.logging.Loggers;
import org.elasticsearch.script.AbstractExecutableScript;

public class ByStateCombineScript extends AbstractExecutableScript {

	protected ESLogger logger = Loggers.getLogger("happiness.script");
	protected Map<String, Object> params = null;

	public ByStateCombineScript(Map<String, Object> params) {
		this.params = params;
	}

	@Override
	@SuppressWarnings("unchecked")
	public Object run() {
		List<ComputedHappiness> combineResults = (List<ComputedHappiness>) params
				.get(HappinessInternalFacet.FACET_TYPE);

		logger.debug("Number of elements in combine results: {}.", combineResults.size());

		logger.debug("Constructing map by State for curHappiness.");
		Map<String, ComputedHappiness> map = new HashMap<String, ComputedHappiness>();
		for (ComputedHappiness curHappiness : combineResults) {
			if (curHappiness.getState() == null || curHappiness.getState().getStateId() == null) {
				continue;
			}

			if (map.containsKey(curHappiness.getState().getStateId())) {
				ComputedHappiness happiness = map.get(curHappiness.getState().getStateId());
				happiness.addScoreAndRelevanceElements(happiness);
			} else {
				map.put(curHappiness.getState().getStateId(), curHappiness);
			}
		}

		logger.debug("Building reduced list to output.");
		List<ComputedHappiness> reduced = new ArrayList<ComputedHappiness>();
		reduced.addAll(map.values());
		return reduced;
	}

	@Override
	@SuppressWarnings("unchecked")
	public Object unwrap(Object value) {
		if (value instanceof List) {
			List<Map<String, Object>> unwrapped = new ArrayList<Map<String, Object>>();
			List<ComputedHappiness> list = (List<ComputedHappiness>) value;

			for (ComputedHappiness curHappiness : list) {
				Map<String, Object> curMap = curHappiness.toMap();
				curMap.remove(ComputedHappiness.LATITUDE_KEY);
				curMap.remove(ComputedHappiness.LONGITUDE_KEY);
				unwrapped.add(curMap);
			}

			return unwrapped;
		}

		return super.unwrap(value);
	}
}