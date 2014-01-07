package it.elasticsearch.script;

import it.elasticsearch.models.ReduceComputedHappiness;

import java.util.List;
import java.util.Map;

import org.elasticsearch.common.logging.ESLogger;
import org.elasticsearch.common.logging.Loggers;
import org.elasticsearch.script.AbstractExecutableScript;

public class ReduceScript extends AbstractExecutableScript {

	protected ESLogger logger = Loggers.getLogger("happiness.script");
	private Map<String, Object> params = null;

	public ReduceScript(Map<String, Object> params) {
		super();
		logger.trace("Initializing reduce script with params: {}.", params);
		this.params = params;
	}

	@Override
	@SuppressWarnings("unchecked")
	public Object run() {
		List<ReduceComputedHappiness> combineResults = (List<ReduceComputedHappiness>) params
				.get(HappinessInternalFacet.FACETS_TYPE);

		double score = 0.;
		double relevance = 0.;
		int elems = 0;

		logger.debug("Number of elements in combine results: {}.", combineResults.size());

		for (ReduceComputedHappiness curHappiness : combineResults) {
			score += curHappiness.getScore() * curHappiness.getNumelements();
			relevance += curHappiness.getRelevance() * curHappiness.getNumelements();
			elems += curHappiness.getNumelements();
		}

		score /= elems;
		relevance /= elems;

		ReduceComputedHappiness reduced = new ReduceComputedHappiness(score, relevance, elems);
		return reduced.toMap();
	}
}