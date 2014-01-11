package it.elasticsearch.script.factory;

import it.elasticsearch.script.GeolocalizedHappinessScript;

import java.io.IOException;
import java.util.Map;
import java.util.Properties;

import org.elasticsearch.ElasticSearchIllegalArgumentException;
import org.elasticsearch.common.Nullable;
import org.elasticsearch.script.ExecutableScript;
import org.elasticsearch.script.NativeScriptFactory;

public class GeolocalizedHappinessScriptFactory extends HappinessScriptFactory implements NativeScriptFactory {

	@Override
	public ExecutableScript newScript(@Nullable Map<String, Object> params) {
		try {
			Properties properties = getScriptProperties(params);
			return new GeolocalizedHappinessScript(properties);
		} catch (IOException e) {
			logger.error("Error while getting GeolocalizedHappinessScript class: {}", e);
			throw new ElasticSearchIllegalArgumentException();
		}
	}

}
