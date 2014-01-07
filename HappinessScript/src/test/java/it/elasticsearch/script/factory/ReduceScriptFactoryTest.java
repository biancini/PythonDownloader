package it.elasticsearch.script.factory;

import static org.fest.assertions.Assertions.assertThat;

import it.elasticsearch.script.factory.CombineScriptFactory;
import it.elasticsearch.script.factory.ReduceScriptFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.elasticsearch.script.ExecutableScript;
import org.junit.Test;

public class ReduceScriptFactoryTest {

	@Test
	public void shouldNewScriptReturnDefaultWhenParamsIsNull() throws IOException {
		// given
		Map<String, Object> params = null;
		ReduceScriptFactory reduceScriptFactory = new ReduceScriptFactory();

		// when
		ExecutableScript reduceScript = reduceScriptFactory.newScript(params);

		// then
		assertThat(reduceScript).isNotNull();
	}

	@Test
	public void shouldNewScriptReturnWhenParamsIsNotNull() throws IOException {
		// given
		Map<String, Object> params = new HashMap<String, Object>();
		CombineScriptFactory reduceScriptFactory = new CombineScriptFactory();

		// when
		ExecutableScript reduceScript = reduceScriptFactory.newScript(params);

		// then
		assertThat(reduceScript).isNotNull();
	}

}
