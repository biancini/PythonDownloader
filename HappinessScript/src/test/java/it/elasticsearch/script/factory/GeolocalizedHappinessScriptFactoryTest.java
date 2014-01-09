package it.elasticsearch.script.factory;

import static org.fest.assertions.Assertions.assertThat;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.elasticsearch.ElasticSearchIllegalArgumentException;
import org.elasticsearch.script.ExecutableScript;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class GeolocalizedHappinessScriptFactoryTest {

	@Rule
	public TemporaryFolder folder = new TemporaryFolder();

	private File propertiesFile = null;

	@Before
	public void createTestData() throws IOException {
		propertiesFile = folder.newFile("happiness.properties");
		BufferedWriter out = new BufferedWriter(new FileWriter(propertiesFile));
		out.write("filename=/etc/elasticsearch/hedonometer.txt\n");
		out.write("separator=\t\n");
		out.write("column=2\n");
		out.write("headers=4\n");
		out.close();
	}

	@Test
	public void shouldNewScriptReturnDefaultWhenParamsIsNull() throws IOException {
		// given
		Map<String, Object> params = null;
		GeolocalizedHappinessScriptFactory geoHappinessScriptFactory = new GeolocalizedHappinessScriptFactory();

		// when
		ExecutableScript happinessScript = geoHappinessScriptFactory.newScript(params);

		// then
		assertThat(happinessScript).isNotNull();
	}

	@Test
	public void shouldNewScriptReturnWhenParamsIsNotNull() throws IOException {
		// given
		Map<String, Object> params = new HashMap<String, Object>();
		GeolocalizedHappinessScriptFactory geoHappinessScriptFactory = new GeolocalizedHappinessScriptFactory();

		// when
		ExecutableScript happinessScript = geoHappinessScriptFactory.newScript(params);

		// then
		assertThat(happinessScript).isNotNull();
	}

	@Test
	public void shouldNewScriptReturnWhenPropertyParam() throws IOException {
		// given
		Map<String, Object> params = new HashMap<String, Object>();
		params.put(HappinessScriptFactory.PARAM_PROPERTIES, propertiesFile.getAbsolutePath());
		GeolocalizedHappinessScriptFactory geoHappinessScriptFactory = new GeolocalizedHappinessScriptFactory();

		// when
		ExecutableScript happinessScript = geoHappinessScriptFactory.newScript(params);

		// then
		assertThat(happinessScript).isNotNull();
	}

	@Test(expected = ElasticSearchIllegalArgumentException.class)
	public void shouldNewScriptThrowIllegalArgumentExceptionWhenInvalidPropertyFile() throws IOException {
		// given
		Map<String, Object> params = new HashMap<String, Object>();
		params.put(HappinessScriptFactory.PARAM_PROPERTIES, "/tmp/file_not_existent.properties");
		GeolocalizedHappinessScriptFactory geoHappinessScriptFactory = new GeolocalizedHappinessScriptFactory();

		// when
		geoHappinessScriptFactory.newScript(params);

		// then
	}
}
