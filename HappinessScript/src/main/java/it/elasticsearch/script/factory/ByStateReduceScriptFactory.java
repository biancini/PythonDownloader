package it.elasticsearch.script.factory;

import it.elasticsearch.script.ByStateReduceScript;

import java.util.Map;

import org.elasticsearch.common.Nullable;
import org.elasticsearch.script.ExecutableScript;
import org.elasticsearch.script.NativeScriptFactory;

public class ByStateReduceScriptFactory implements NativeScriptFactory {

	@Override
	public ExecutableScript newScript(@Nullable Map<String, Object> params) {
		return new ByStateReduceScript(params);
	}

}
