package it.elasticsearch.script.reduce;

import static org.fest.assertions.Assertions.assertThat;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.elasticsearch.script.ExecutableScript;
import org.junit.Test;

public class CombineScriptFactoryTest {

	@Test
	public void shouldNewScriptReturnDefaultWhenParamsIsNull() throws IOException {
		// given
		Map<String, Object> params = null;
		CombineScriptFactory combineScriptFactory = new CombineScriptFactory();

		// when
		ExecutableScript combineScript = combineScriptFactory.newScript(params);

		// then
		assertThat(combineScript).isNotNull();
	}

	@Test
	public void shouldNewScriptReturnWhenParamsIsNotNull() throws IOException {
		// given
		Map<String, Object> params = new HashMap<String, Object>();
		CombineScriptFactory combineScriptFactory = new CombineScriptFactory();

		// when
		ExecutableScript combineScript = combineScriptFactory.newScript(params);

		// then
		assertThat(combineScript).isNotNull();
	}

}
