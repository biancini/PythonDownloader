package it.elasticsearch.utilities;

import static org.elasticsearch.common.collect.Maps.newHashMap;

import java.util.Map;

import org.elasticsearch.script.ExecutableScript;
import org.elasticsearch.script.ScriptService;
import org.elasticsearch.script.SearchScript;
import org.elasticsearch.search.internal.SearchContext;

public class FacetParamsManager {
	public static final String LANG_PARAM = "lang";
	public static final String SCRIPT_PARAM = "script";
	public static final String PARAMS_PARAM = "params";

	public static SearchScript getSearchScript(Map<String, Object> scriptTokens,
			Map<String, Object> additionalParams, SearchContext context) {

		SearchScript script = null;

		if (context != null && scriptTokens != null && scriptTokens.containsKey(SCRIPT_PARAM)) {
			String scriptString = (String) scriptTokens.get(SCRIPT_PARAM);
			String scriptLang = (String) scriptTokens.get(LANG_PARAM);

			ScriptService scriptService = context.scriptService();
			Map<String, Object> scriptParams = FacetParamsManager.initializeParams(scriptTokens, additionalParams);
			script = scriptService.search(context.lookup(), scriptLang, scriptString, scriptParams);
		}

		return script;
	}

	public static ExecutableScript getExecutableScript(Map<String, Object> scriptTokens,
			Map<String, Object> additionalParams, ScriptService scriptService) {

		ExecutableScript script = null;

		if (scriptTokens != null) {
			String scriptString = (String) scriptTokens.get(SCRIPT_PARAM);
			String scriptLang = (String) scriptTokens.get(LANG_PARAM);

			Map<String, Object> scriptParams = FacetParamsManager.initializeParams(scriptTokens, additionalParams);
			script = scriptService.executable(scriptLang, scriptString, scriptParams);
		}

		return script;
	}

	@SuppressWarnings("unchecked")
	public static Map<String, Object> initializeParams(Map<String, Object> scriptMap,
			Map<String, Object> additionalParams) {
		Map<String, Object> params = newHashMap();
		if (scriptMap.containsKey(PARAMS_PARAM)) {
			if (scriptMap.get(PARAMS_PARAM) != null) {
				params = (Map<String, Object>) scriptMap.get(PARAMS_PARAM);
			}
		}

		if (additionalParams != null) {
			params.putAll(additionalParams);
		}

		return params;
	}
}
