package it.elasticsearch.script.factory;

import it.elasticsearch.script.reduce.ReduceScript;

import java.util.Map;

import org.elasticsearch.common.Nullable;
import org.elasticsearch.script.ExecutableScript;
import org.elasticsearch.script.NativeScriptFactory;

public class ReduceScriptFactory implements NativeScriptFactory {

	@Override
	public ExecutableScript newScript(@Nullable Map<String, Object> params) {
		return new ReduceScript(params);
	}

}
