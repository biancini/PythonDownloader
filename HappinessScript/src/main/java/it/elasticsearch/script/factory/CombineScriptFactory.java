package it.elasticsearch.script.factory;

import it.elasticsearch.script.reduce.CombineScript;

import java.util.Map;

import org.elasticsearch.common.Nullable;
import org.elasticsearch.script.ExecutableScript;
import org.elasticsearch.script.NativeScriptFactory;

public class CombineScriptFactory implements NativeScriptFactory {

	@Override
	public ExecutableScript newScript(@Nullable Map<String, Object> params) {
		return new CombineScript(params);
	}

}
