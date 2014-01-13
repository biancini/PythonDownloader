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

	public static final String FACET_TYPE = "facet";
	public static final String TYPE_CONTEXT = "_ctx";
	public static final String CLIENT_PARAM = "_client";

	private final ESLogger logger = Loggers.getLogger("happiness.script");

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
		additionalParams.put(TYPE_CONTEXT, this.context);
		additionalParams.put(CLIENT_PARAM, this.client);

		this.mapScript = FacetParamsManager.getSearchScript(mapScript, additionalParams, context);
		this.combineScript = combineScript;
		this.reduceScript = reduceScript;
	}

	@Override
	public InternalFacet buildFacet(String facetName) {
		logger.debug("Combining results with combineScript for facet {}.", facetName);
		Object facet = null;

		Map<String, Object> additionalParams = new HashMap<String, Object>();
		additionalParams.put(TYPE_CONTEXT, this.context);
		additionalParams.put(CLIENT_PARAM, this.client);
		additionalParams.put(FACET_TYPE, this.searchResults);

		ExecutableScript combineScript = FacetParamsManager.getExecutableScript(this.combineScript,
				additionalParams, context.scriptService());
		facet = combineScript.run();

		logger.trace("Computed combine facet: {}.", facet);
		return new HappinessInternalFacet(facetName, facet, reduceScript, context.scriptService(), client);
	}

	@Override
	public Collector collector() {
		return new HappinessFacetCollector(mapScript, searchResults);
	}

	protected SearchScript getMapScript() {
		return mapScript;
	}

	protected Map<String, Object> getCombineScript() {
		return combineScript;
	}

	protected Map<String, Object> getReduceScript() {
		return reduceScript;
	}
}