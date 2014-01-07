package it.elasticsearch.script;

import it.elasticsearch.models.ComputedHappiness;
import it.elasticsearch.models.ReduceComputedHappiness;
import it.elasticsearch.script.facet.HappinessInternalFacet;

import java.util.List;
import java.util.Map;

import org.elasticsearch.common.logging.ESLogger;
import org.elasticsearch.common.logging.Loggers;
import org.elasticsearch.script.AbstractExecutableScript;

public class CombineScript extends AbstractExecutableScript {

	protected ESLogger logger = Loggers.getLogger("happiness.script");
	private Map<String, Object> params = null;

	public CombineScript(Map<String, Object> params) {
		super();
		logger.trace("Initializing combine script with params: {}.", params);
		this.params = params;
	}

	@Override
	@SuppressWarnings("unchecked")
	public Object run() {
		List<ComputedHappiness> searchResults = (List<ComputedHappiness>) params
				.get(HappinessInternalFacet.FACET_TYPE);

		double score = 0.;
		double relevance = 0.;
		int elems = searchResults.size();

		logger.debug("Number of elements in search results: {}.", elems);

		for (ComputedHappiness curHappiness : searchResults) {
			score += curHappiness.getScore();
			relevance += curHappiness.getRelevance();
		}

		score /= elems;
		relevance /= elems;

		ReduceComputedHappiness combined = new ReduceComputedHappiness(score, relevance, elems);
		return combined.toMap();
	}
}