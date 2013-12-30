package it.elasticsearch.plugin;

import it.elasticsearch.scripts.HappinessScriptFactory;

import org.elasticsearch.plugins.AbstractPlugin;
import org.elasticsearch.script.ScriptModule;

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
}