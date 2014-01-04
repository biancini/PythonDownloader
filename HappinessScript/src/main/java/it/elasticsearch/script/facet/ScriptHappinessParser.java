package it.elasticsearch.script.facet;

import java.io.IOException;
import java.util.Map;

import org.elasticsearch.client.Client;
import org.elasticsearch.common.component.AbstractComponent;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentParser;
import org.elasticsearch.script.ScriptService;
import org.elasticsearch.search.facet.FacetExecutor;
import org.elasticsearch.search.facet.FacetParser;
import org.elasticsearch.search.facet.FacetPhaseExecutionException;
import org.elasticsearch.search.internal.SearchContext;

public class ScriptHappinessParser extends AbstractComponent implements FacetParser {

	private final Client client;

	@Inject
	public ScriptHappinessParser(Settings settings, ScriptService scriptService, Client client) {
		super(settings);

		InternalHappinessFacet.registerStreams(scriptService, client);
		this.client = client;
	}

	@Override
	public String[] types() {
		return new String[] { HappinessFacet.TYPE };
	}

	@Override
	public FacetExecutor.Mode defaultMainMode() {
		return FacetExecutor.Mode.COLLECTOR;
	}

	@Override
	public FacetExecutor.Mode defaultGlobalMode() {
		return FacetExecutor.Mode.COLLECTOR;
	}

	@Override
	public FacetExecutor parse(String facetName, XContentParser parser, SearchContext context) throws IOException {
		String mapScript = null;
		String combineScript = null;
		String reduceScript = null;
		String scriptLang = null;
		Map<String, Object> params = null;
		Map<String, Object> reduceParams = null;
		XContentParser.Token token = null;
		String fieldName = null;

		while ((token = parser.nextToken()) != XContentParser.Token.END_OBJECT) {
			if (token == XContentParser.Token.FIELD_NAME) {
				fieldName = parser.currentName();
			} else if (token == XContentParser.Token.START_OBJECT) {
				if ("params".equals(fieldName)) {
					params = parser.map();
				} else if ("reduce_params".equals(fieldName)) {
					reduceParams = parser.map();
				}
			} else if (token.isValue()) {
				if ("map_script".equals(fieldName) || "mapScript".equals(fieldName)) {
					mapScript = parser.text();
				} else if ("combine_script".equals(fieldName) || "combineScript".equals(fieldName)) {
					combineScript = parser.text();
				} else if ("reduce_script".equals(fieldName) || "reduceScript".equals(fieldName)) {
					reduceScript = parser.text();
				} else if ("lang".equals(fieldName)) {
					scriptLang = parser.text();
				}
			}
		}

		if (mapScript == null) {
			throw new FacetPhaseExecutionException(facetName, "map_script field is required");
		}

		return new ScriptHappinessCollector(scriptLang, mapScript, combineScript, reduceScript, params, reduceParams,
				context, client);
	}

}