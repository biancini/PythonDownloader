package it.elasticsearch.plugin;

import it.elasticsearch.script.HappinessFacetParser;
import it.elasticsearch.script.factory.CombineScriptFactory;
import it.elasticsearch.script.factory.HappinessScriptFactory;
import it.elasticsearch.script.factory.ReduceScriptFactory;

import org.elasticsearch.plugins.AbstractPlugin;
import org.elasticsearch.script.ScriptModule;
import org.elasticsearch.search.facet.FacetModule;

public class HappinessPlugin extends AbstractPlugin {

	public static final String PLUGIN_NAME = "happiness-plugin";
	public static final String PLUGIN_DESCRIPTION = "Happiness plugin";

	public static final String HAPPINESS_SCRIPT = "hedonometer";
	public static final String COMBINE_SCRIPT = "happycombiner";
	public static final String REDUCE_SCRIPT = "happyreducer";

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
		module.registerScript(REDUCE_SCRIPT, ReduceScriptFactory.class);
	}

	public void onModule(FacetModule module) {
		module.addFacetProcessor(HappinessFacetParser.class);
	}
}