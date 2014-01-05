package it.elasticsearch.script.facet;

import it.elasticsearch.utilities.FacetParamsManager;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.lucene.index.AtomicReaderContext;
import org.apache.lucene.search.Scorer;
import org.elasticsearch.client.Client;
import org.elasticsearch.script.ExecutableScript;
import org.elasticsearch.script.SearchScript;
import org.elasticsearch.search.facet.FacetExecutor;
import org.elasticsearch.search.facet.InternalFacet;
import org.elasticsearch.search.internal.SearchContext;

public class ScriptHappinessCollector extends FacetExecutor {

	private final SearchScript mapScript;
	private final ExecutableScript combineScript;
	private final Map<String, Object> reduceScript;

	private Client client = null;

	public ScriptHappinessCollector(Map<String, Object> mapScript, Map<String, Object> combineScript,
			Map<String, Object> reduceScript, SearchContext context, Client client) {

		this.client = client;

		Map<String, Object> additionalParams = new HashMap<String, Object>();
		additionalParams.put("_ctx", context);
		additionalParams.put("_client", client);

		this.mapScript = FacetParamsManager.getSearchScript(mapScript, additionalParams, context);
		this.combineScript = FacetParamsManager.getExecutableScript(combineScript, additionalParams,
				context.scriptService());
		this.reduceScript = reduceScript;
	}

	@Override
	public InternalFacet buildFacet(String facetName) {
		Object facet = null;

		if (combineScript != null) {
			facet = combineScript.run();
			// } else {
			// facet = mapParams.get("facet");
		}

		return new InternalHappinessFacet(facetName, facet, reduceScript, null, client);
	}

	@Override
	public Collector collector() {
		return new Collector();
	}

	class Collector extends FacetExecutor.Collector {

		@Override
		public void postCollection() {
		}

		@Override
		public void collect(int doc) throws IOException {
			mapScript.setNextDocId(doc);
			mapScript.run();
		}

		@Override
		public void setNextReader(AtomicReaderContext context) throws IOException {
			mapScript.setNextReader(context);
		}

		@Override
		public void setScorer(Scorer scorer) throws IOException {
			mapScript.setScorer(scorer);
		}
	}
}