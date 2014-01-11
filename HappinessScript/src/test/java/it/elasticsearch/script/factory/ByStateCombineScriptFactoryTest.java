package it.elasticsearch.script.factory;

import static org.fest.assertions.Assertions.assertThat;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.elasticsearch.script.ExecutableScript;
import org.junit.Test;

public class ByStateCombineScriptFactoryTest {

	@Test
	public void shouldNewScriptReturnDefaultWhenParamsIsNull() throws IOException {
		// given
		Map<String, Object> params = null;
		ByStateCombineScriptFactory combineScriptFactory = new ByStateCombineScriptFactory();

		// when
		ExecutableScript combineScript = combineScriptFactory.newScript(params);

		// then
		assertThat(combineScript).isNotNull();
	}

	@Test
	public void shouldNewScriptReturnWhenParamsIsNotNull() throws IOException {
		// given
		Map<String, Object> params = new HashMap<String, Object>();
		ByStateCombineScriptFactory combineScriptFactory = new ByStateCombineScriptFactory();

		// when
		ExecutableScript combineScript = combineScriptFactory.newScript(params);

		// then
		assertThat(combineScript).isNotNull();
	}

}
