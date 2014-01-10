package it.elasticsearch.script.factory;

import it.elasticsearch.script.ByStateCombineScript;

import java.util.Map;

import org.elasticsearch.common.Nullable;
import org.elasticsearch.script.ExecutableScript;
import org.elasticsearch.script.NativeScriptFactory;

public class ByStateCombineScriptFactory implements NativeScriptFactory {

	@Override
	public ExecutableScript newScript(@Nullable Map<String, Object> params) {
		return new ByStateCombineScript(params);
	}

}
