package it.elasticsearch.scripts;

import java.io.IOException;
import java.util.Map;

import org.elasticsearch.common.Nullable;
import org.elasticsearch.script.ExecutableScript;
import org.elasticsearch.script.NativeScriptFactory;

public class HappinessScriptFactory implements NativeScriptFactory {

	@Override
	public ExecutableScript newScript(@Nullable Map<String, Object> params) {
		try {
			return new HappinessScript(params);
		} catch (IOException e) {
			return null;
		}
	}
}
