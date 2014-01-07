package it.elasticsearch.script.facet;

import static org.fest.assertions.Assertions.assertThat;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentParser;
import org.elasticsearch.common.xcontent.json.JsonXContent;
import org.elasticsearch.search.facet.FacetExecutor;
import org.elasticsearch.search.facet.FacetPhaseExecutionException;
import org.elasticsearch.search.internal.SearchContext;
import org.junit.Test;
import org.mockito.Mockito;

public class HappinessFacetParserTest {

	public static final String FACET_NAME = "facetname";

	@Test(expected = FacetPhaseExecutionException.class)
	public void shouldParseComplainIfNoMapScript() throws IOException {
		// given
		Settings settings = Mockito.mock(Settings.class);
		Mockito.when(settings.get(Mockito.anyString())).thenReturn("");
		Mockito.when(settings.getAsBoolean(Mockito.anyString(), Mockito.anyBoolean())).thenReturn(false);
		HappinessFacetParser happinessFacetParser = new HappinessFacetParser(settings, null, null);

		Map<String, Object> expectedCombineScript = new HashMap<String, Object>();
		expectedCombineScript.put("script", "happycombiner");
		expectedCombineScript.put("lang", "native");

		Map<String, Object> expectedReduceScript = new HashMap<String, Object>();
		expectedReduceScript.put("script", "happyreducer");
		expectedReduceScript.put("lang", "native");

		String jsonData = "{ ";
		jsonData += "\"combine_script\": { \"script\": \"" + expectedCombineScript.get("script")
				+ "\", \"lang\": \"" + expectedCombineScript.get("lang") + "\" }, ";
		jsonData += "\"reduce_script\": { \"script\": \"" + expectedReduceScript.get("script")
				+ "\", \"lang\": \"" + expectedReduceScript.get("lang") + "\" } ";
		jsonData += "}";

		XContentParser parser = JsonXContent.jsonXContent.createParser(jsonData);
		SearchContext context = null;

		// when
		happinessFacetParser.parse(FACET_NAME, parser, context);

		// then
	}

	@Test
	public void shouldParseProcessAllInput() throws IOException {
		// given
		Settings settings = Mockito.mock(Settings.class);
		Mockito.when(settings.get(Mockito.anyString())).thenReturn("");
		Mockito.when(settings.getAsBoolean(Mockito.anyString(), Mockito.anyBoolean())).thenReturn(false);
		HappinessFacetParser happinessFacetParser = new HappinessFacetParser(settings, null, null);

		Map<String, Object> expectedCombineScript = new HashMap<String, Object>();
		expectedCombineScript.put("script", "happycombiner");
		expectedCombineScript.put("lang", "native");

		Map<String, Object> expectedReduceScript = new HashMap<String, Object>();
		expectedReduceScript.put("script", "happyreducer");
		expectedReduceScript.put("lang", "native");

		String jsonData = "{ ";
		jsonData += "\"map_script\": { \"script\": \"happycombiner\", \"lang\": \"native\" }, ";
		jsonData += "\"combine_script\": { \"script\": \"" + expectedCombineScript.get("script")
				+ "\", \"lang\": \"" + expectedCombineScript.get("lang") + "\" }, ";
		jsonData += "\"reduce_script\": { \"script\": \"" + expectedReduceScript.get("script")
				+ "\", \"lang\": \"" + expectedReduceScript.get("lang") + "\" } ";
		jsonData += "}";

		XContentParser parser = JsonXContent.jsonXContent.createParser(jsonData);
		SearchContext context = null;

		// when
		FacetExecutor facetExecutor = happinessFacetParser.parse(FACET_NAME, parser, context);

		// then
		assertThat(happinessFacetParser).isInstanceOf(HappinessFacetParser.class);
		HappinessFacetExecutor hapinessFacetExecutor = (HappinessFacetExecutor) facetExecutor;
		assertThat(hapinessFacetExecutor.getCombineScript().entrySet()).containsOnly(
				expectedCombineScript.entrySet().toArray());
		assertThat(hapinessFacetExecutor.getReduceScript().entrySet()).containsOnly(
				expectedReduceScript.entrySet().toArray());
	}

	@Test
	public void shouldTypesReturnHappinessFacetType() throws IOException {
		// given
		Settings settings = Mockito.mock(Settings.class);
		Mockito.when(settings.get(Mockito.anyString())).thenReturn("");
		Mockito.when(settings.getAsBoolean(Mockito.anyString(), Mockito.anyBoolean())).thenReturn(false);
		HappinessFacetParser happinessFacetParser = new HappinessFacetParser(settings, null, null);

		// when
		String[] returnedTypes = happinessFacetParser.types();

		// then
		assertThat(returnedTypes).contains(HappinessFacet.TYPE);
	}

	@Test
	public void shouldDefaultMainModeReturnCollector() throws IOException {
		// given
		Settings settings = Mockito.mock(Settings.class);
		Mockito.when(settings.get(Mockito.anyString())).thenReturn("");
		Mockito.when(settings.getAsBoolean(Mockito.anyString(), Mockito.anyBoolean())).thenReturn(false);
		HappinessFacetParser happinessFacetParser = new HappinessFacetParser(settings, null, null);

		// when
		FacetExecutor.Mode mode = happinessFacetParser.defaultMainMode();

		// then
		assertThat(mode).isEqualTo(FacetExecutor.Mode.COLLECTOR);
	}

	@Test
	public void shouldDefaultGlobalModeReturnCollector() throws IOException {
		// given
		Settings settings = Mockito.mock(Settings.class);
		Mockito.when(settings.get(Mockito.anyString())).thenReturn("");
		Mockito.when(settings.getAsBoolean(Mockito.anyString(), Mockito.anyBoolean())).thenReturn(false);
		HappinessFacetParser happinessFacetParser = new HappinessFacetParser(settings, null, null);

		// when
		FacetExecutor.Mode mode = happinessFacetParser.defaultGlobalMode();

		// then
		assertThat(mode).isEqualTo(FacetExecutor.Mode.COLLECTOR);
	}
}
