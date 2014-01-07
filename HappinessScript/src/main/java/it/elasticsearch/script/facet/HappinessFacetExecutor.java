package it.elasticsearch.script.facet;

import it.elasticsearch.models.ComputedHappiness;
import it.elasticsearch.utilities.FacetParamsManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.elasticsearch.client.Client;
import org.elasticsearch.common.logging.ESLogger;
import org.elasticsearch.common.logging.Loggers;
import org.elasticsearch.script.ExecutableScript;
import org.elasticsearch.script.SearchScript;
import org.elasticsearch.search.facet.FacetExecutor;
import org.elasticsearch.search.facet.InternalFacet;
import org.elasticsearch.search.internal.SearchContext;

public class HappinessFacetExecutor extends FacetExecutor {

	private ESLogger logger = Loggers.getLogger("happiness.script");

	private SearchContext context = null;
	private SearchScript mapScript = null;
	private Map<String, Object> combineScript = null;
	private Map<String, Object> reduceScript = null;
	private List<ComputedHappiness> searchResults = null;

	private Client client = null;

	public HappinessFacetExecutor(Map<String, Object> mapScript, Map<String, Object> combineScript,
			Map<String, Object> reduceScript, SearchContext context, Client client) {

		this.context = context;
		this.client = client;
		this.searchResults = new ArrayList<ComputedHappiness>();

		Map<String, Object> additionalParams = new HashMap<String, Object>();
		additionalParams.put("_ctx", this.context);
		additionalParams.put("_client", this.client);

		this.mapScript = FacetParamsManager.getSearchScript(mapScript, additionalParams, context);
		this.combineScript = combineScript;
		this.reduceScript = reduceScript;
	}

	@Override
	public InternalFacet buildFacet(String facetName) {
		logger.debug("Combining results with combineScript for facet {}.", facetName);
		Object facet = null;

		Map<String, Object> additionalParams = new HashMap<String, Object>();
		additionalParams.put("_ctx", this.context);
		additionalParams.put("_client", this.client);
		additionalParams.put("facet", this.searchResults);

		ExecutableScript combineScript = FacetParamsManager.getExecutableScript(this.combineScript,
				additionalParams, context.scriptService());
		facet = combineScript.run();

		logger.debug("Computed combine facet: {}.", facet);
		return new HappinessInternalFacet(facetName, facet, reduceScript, context.scriptService(), client);
	}

	@Override
	public Collector collector() {
		return new HappinessFacetCollector(mapScript, searchResults);
	}
}