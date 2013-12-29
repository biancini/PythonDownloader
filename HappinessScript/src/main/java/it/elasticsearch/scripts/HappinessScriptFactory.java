package it.elasticsearch.scripts;

import java.io.IOException;
import java.util.Map;

import org.elasticsearch.common.Nullable;
import org.elasticsearch.common.logging.ESLogger;
import org.elasticsearch.common.logging.Loggers;
import org.elasticsearch.script.ExecutableScript;
import org.elasticsearch.script.NativeScriptFactory;

public class HappinessScriptFactory implements NativeScriptFactory {

	private final ESLogger logger = Loggers.getLogger("happiness.script");

	@Override
	public ExecutableScript newScript(@Nullable Map<String, Object> params) {
		try {
			return new HappinessScript(params);
		} catch (IOException e) {
			logger.error("Error while getting HappinessScript class: {}", e);
			return null;
		}
	}
}
