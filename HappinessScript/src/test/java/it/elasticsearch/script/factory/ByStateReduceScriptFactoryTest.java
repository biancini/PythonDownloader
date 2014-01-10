package it.elasticsearch.script.factory;

import static org.fest.assertions.Assertions.assertThat;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.elasticsearch.script.ExecutableScript;
import org.junit.Test;

public class ByStateReduceScriptFactoryTest {

	@Test
	public void shouldNewScriptReturnDefaultWhenParamsIsNull() throws IOException {
		// given
		Map<String, Object> params = null;
		ByStateReduceScriptFactory reduceScriptFactory = new ByStateReduceScriptFactory();

		// when
		ExecutableScript reduceScript = reduceScriptFactory.newScript(params);

		// then
		assertThat(reduceScript).isNotNull();
	}

	@Test
	public void shouldNewScriptReturnWhenParamsIsNotNull() throws IOException {
		// given
		Map<String, Object> params = new HashMap<String, Object>();
		ByStateCombineScriptFactory reduceScriptFactory = new ByStateCombineScriptFactory();

		// when
		ExecutableScript reduceScript = reduceScriptFactory.newScript(params);

		// then
		assertThat(reduceScript).isNotNull();
	}

}
