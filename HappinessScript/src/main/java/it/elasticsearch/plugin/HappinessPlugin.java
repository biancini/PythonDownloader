package it.elasticsearch.plugin;

import it.elasticsearch.scripts.HappinessScriptFactory;

import org.elasticsearch.plugins.AbstractPlugin;
import org.elasticsearch.script.ScriptModule;

public class HappinessPlugin extends AbstractPlugin {

	/**
	 * The name of the plugin.
	 * <p/>
	 * This name will be used by elasticsearch in the log file to refer to this
	 * plugin.
	 * 
	 * @return plugin name.
	 */
	@Override
	public String name() {
		return "happiness-script";
	}

	/**
	 * The description of the plugin.
	 * 
	 * @return plugin description
	 */
	@Override
	public String description() {
		return "Happiness script";
	}

	public void onModule(ScriptModule module) {
		module.registerScript("happinessscript", HappinessScriptFactory.class);
	}
}