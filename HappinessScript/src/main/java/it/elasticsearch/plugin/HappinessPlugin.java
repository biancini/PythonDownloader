package it.elasticsearch.plugin;

import it.elasticsearch.script.facet.ScriptHappinessParser;
import it.elasticsearch.script.search.HappinessScriptFactory;

import org.elasticsearch.plugins.AbstractPlugin;
import org.elasticsearch.script.ScriptModule;
import org.elasticsearch.search.facet.FacetModule;

public class HappinessPlugin extends AbstractPlugin {

	@Override
	public String name() {
		return "happiness-script";
	}

	@Override
	public String description() {
		return "Happiness script";
	}

	public void onModule(ScriptModule module) {
		module.registerScript("happinessscript", HappinessScriptFactory.class);
	}

	public void onModule(FacetModule module) {
		module.addFacetProcessor(ScriptHappinessParser.class);
	}
}