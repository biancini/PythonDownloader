package it.elasticsearch.plugin;

import it.elasticsearch.script.facet.HappinessFacetParser;
import it.elasticsearch.script.factory.ByStateCombineScriptFactory;
import it.elasticsearch.script.factory.CombineScriptFactory;
import it.elasticsearch.script.factory.HappinessScriptFactory;

import org.elasticsearch.plugins.AbstractPlugin;
import org.elasticsearch.script.ScriptModule;
import org.elasticsearch.search.facet.FacetModule;

public class HappinessPlugin extends AbstractPlugin {

	public static final String PLUGIN_NAME = "happiness-plugin";
	public static final String PLUGIN_DESCRIPTION = "Happiness plugin";

	public static final String HAPPINESS_SCRIPT = "hedonometer";
	public static final String COMBINE_SCRIPT = "happy-combiner";
	public static final String COMBINE_BYSTATE_SCRIPT = "happy-bystate-combiner";

	@Override
	public String name() {
		return PLUGIN_NAME;
	}

	@Override
	public String description() {
		return PLUGIN_DESCRIPTION;
	}

	public void onModule(ScriptModule module) {
		module.registerScript(HAPPINESS_SCRIPT, HappinessScriptFactory.class);
		module.registerScript(COMBINE_SCRIPT, CombineScriptFactory.class);
		module.registerScript(COMBINE_BYSTATE_SCRIPT, ByStateCombineScriptFactory.class);
	}

	public void onModule(FacetModule module) {
		module.addFacetProcessor(HappinessFacetParser.class);
	}
}