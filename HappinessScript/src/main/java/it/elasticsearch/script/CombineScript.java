package it.elasticsearch.script;

import it.elasticsearch.models.ComputedHappiness;
import it.elasticsearch.script.facet.HappinessInternalFacet;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.elasticsearch.common.logging.ESLogger;
import org.elasticsearch.common.logging.Loggers;
import org.elasticsearch.script.AbstractExecutableScript;

public class CombineScript extends AbstractExecutableScript {

	protected final ESLogger logger = Loggers.getLogger("happiness.script");
	protected Map<String, Object> params = null;

	public CombineScript(Map<String, Object> params) {
		this.params = params;
	}

	@Override
	@SuppressWarnings("unchecked")
	public Object run() {
		List<ComputedHappiness> combineResults = (List<ComputedHappiness>) params
				.get(HappinessInternalFacet.FACET_TYPE);

		logger.debug("Number of elements in combine results: {}.", combineResults.size());
		ComputedHappiness reducedHappiness = null;

		for (ComputedHappiness curHappiness : combineResults) {
			if (reducedHappiness == null) {
				reducedHappiness = curHappiness;
			} else {
				reducedHappiness.addScoreAndRelevanceElements(curHappiness);
			}
		}

		List<ComputedHappiness> reduced = new ArrayList<ComputedHappiness>();
		reduced.add(reducedHappiness);
		return reduced;
	}

	@Override
	@SuppressWarnings("unchecked")
	public Object unwrap(Object value) {
		if (value instanceof List) {
			List<Map<String, Object>> unwrapped = new ArrayList<Map<String, Object>>();
			List<ComputedHappiness> list = (List<ComputedHappiness>) value;

			for (ComputedHappiness curHappiness : list) {
				unwrapped.add(curHappiness.toMap());
			}

			return unwrapped;
		}

		return super.unwrap(value);
	}
}