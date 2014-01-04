package it.elasticsearch.script.search;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;

import org.elasticsearch.ElasticSearchIllegalArgumentException;
import org.elasticsearch.common.Nullable;
import org.elasticsearch.common.logging.ESLogger;
import org.elasticsearch.common.logging.Loggers;
import org.elasticsearch.script.ExecutableScript;
import org.elasticsearch.script.NativeScriptFactory;

public class HappinessScriptFactory implements NativeScriptFactory {

	private final ESLogger logger = Loggers.getLogger("happiness.script");
	public static final String PROPERTIES_FILENAME = "/etc/elasticsearch/happiness.properties";
	public static final String PARAM_PROPERTIES = "properties";

	@Override
	public ExecutableScript newScript(@Nullable Map<String, Object> params) {
		try {
			String fileName = PROPERTIES_FILENAME;

			if (params != null) {
				String paramsFilename = (String) params.get(PARAM_PROPERTIES);
				if (paramsFilename != null) {
					fileName = paramsFilename;
				}
			}

			Properties properties = new Properties();
			properties.load(new FileInputStream(fileName));

			return new HappinessScript(properties);
		} catch (IOException e) {
			logger.error("Error while getting HappinessScript class: {}", e);
			throw new ElasticSearchIllegalArgumentException();
		}
	}
}
