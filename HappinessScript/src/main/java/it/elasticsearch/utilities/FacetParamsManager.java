package it.elasticsearch.utilities;

import static org.elasticsearch.common.collect.Maps.newHashMap;

import java.util.Map;

import org.elasticsearch.script.ExecutableScript;
import org.elasticsearch.script.ScriptService;
import org.elasticsearch.script.SearchScript;
import org.elasticsearch.search.internal.SearchContext;

public class FacetParamsManager {

	public static SearchScript getSearchScript(Map<String, Object> scriptTokens,
			Map<String, Object> additionalParams, SearchContext context) {

		ScriptService scriptService = context.scriptService();
		SearchScript script = null;

		if (scriptTokens != null && scriptTokens.containsKey("script")) {
			String scriptString = (String) scriptTokens.get("script");
			String scriptLang = (String) scriptTokens.get("lang");

			Map<String, Object> scriptParams = FacetParamsManager.initializeParams(scriptTokens, additionalParams);
			script = scriptService.search(context.lookup(), scriptLang, scriptString, scriptParams);
		}

		return script;
	}

	public static ExecutableScript getExecutableScript(Map<String, Object> scriptTokens,
			Map<String, Object> additionalParams, ScriptService scriptService) {

		ExecutableScript script = null;

		if (scriptTokens != null) {
			String scriptString = (String) scriptTokens.get("script");
			String scriptLang = (String) scriptTokens.get("lang");

			Map<String, Object> scriptParams = FacetParamsManager.initializeParams(scriptTokens, additionalParams);
			script = scriptService.executable(scriptLang, scriptString, scriptParams);
		}

		return script;
	}

	@SuppressWarnings("unchecked")
	public static Map<String, Object> initializeParams(Map<String, Object> scriptMap,
			Map<String, Object> additionalParams) {
		Map<String, Object> params = newHashMap();
		if (scriptMap.containsKey("params")) {
			if (scriptMap.get("params") != null) {
				params = (Map<String, Object>) scriptMap.get("params");
			}
		}

		if (additionalParams != null) {
			params.putAll(additionalParams);
		}

		return params;
	}
}
