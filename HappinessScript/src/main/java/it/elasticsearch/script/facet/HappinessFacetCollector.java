package it.elasticsearch.script.facet;

import it.elasticsearch.models.ComputedHappiness;
import it.elasticsearch.models.HappinessInstantiator;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.lucene.index.AtomicReaderContext;
import org.elasticsearch.common.logging.ESLogger;
import org.elasticsearch.common.logging.Loggers;
import org.elasticsearch.script.SearchScript;
import org.elasticsearch.search.facet.FacetExecutor.Collector;

public class HappinessFacetCollector extends Collector {

	private ESLogger logger = Loggers.getLogger("happiness.script");
	private SearchScript mapScript = null;
	private List<ComputedHappiness> searchResults = null;

	public HappinessFacetCollector(SearchScript mapScript, List<ComputedHappiness> searchResults) {
		this.mapScript = mapScript;
		this.searchResults = searchResults;
	}

	@Override
	public void postCollection() {
		// Do Nothing
	}

	@Override
	@SuppressWarnings("unchecked")
	public void collect(int doc) throws IOException {
		logger.trace("Executing collect on id = {}.", doc);

		mapScript.setNextDocId(doc);
		Map<String, Double> scriptResult = (Map<String, Double>) mapScript.run();
		if (scriptResult != null) {
			searchResults.add(HappinessInstantiator.instantiate(scriptResult));
		}
	}

	@Override
	public void setNextReader(AtomicReaderContext context) throws IOException {
		mapScript.setNextReader(context);
	}

	protected List<ComputedHappiness> getSearchResults() {
		return searchResults;
	}
}
