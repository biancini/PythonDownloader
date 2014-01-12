package it.elasticsearch.script;

import static org.fest.assertions.Assertions.assertThat;
import it.elasticsearch.models.ComputedHappiness;
import it.elasticsearch.models.USAState;
import it.elasticsearch.utilities.HappinessAnalyzer;
import it.elasticsearch.utilities.HappinessWords;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.elasticsearch.common.geo.GeoPoint;
import org.elasticsearch.common.logging.Loggers;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mockito;

public class HappinessScriptTest {

	@Rule
	public TemporaryFolder folder = new TemporaryFolder();

	private File happinessFile = null;
	private String firstWord = "hello";
	private String secondWord = "world";
	private double firstHappiness = 7.0;
	private double secondHappiness = 2.0;

	private double lat = -100;
	private double lng = 40;

	@Before
	public void createTestData() throws IOException {
		happinessFile = folder.newFile("happiness.txt");
		BufferedWriter out = new BufferedWriter(new FileWriter(happinessFile));
		out.write(firstWord + "\t" + firstHappiness + "\n");
		out.write(secondWord + "\t" + secondHappiness + "\n");
		out.flush();
		out.close();
	}

	@Test
	public void shouldRunReturnNullIfNoTextInSource() throws IOException {
		// given
		assertThat(happinessFile.exists()).isTrue();

		Properties properties = new Properties();
		properties.put(HappinessWords.PARAM_FILENAME, happinessFile.getAbsolutePath());

		HappinessScript mockHappinessScript = Mockito.mock(HappinessScript.class);
		Mockito.when(mockHappinessScript.run()).thenCallRealMethod();
		Mockito.when(mockHappinessScript.getTweetText()).thenReturn(null);
		mockHappinessScript.logger = Loggers.getLogger("happiness.script");

		// when
		Object happiness = mockHappinessScript.run();

		// then
		assertThat(happiness).isNull();
	}

	@Test
	public void shouldRunWork() throws IOException {
		// given
		assertThat(happinessFile.exists()).isTrue();

		String tweetText = firstWord + " " + secondWord;
		Properties properties = new Properties();
		properties.put(HappinessWords.PARAM_FILENAME, happinessFile.getAbsolutePath());

		ComputedHappiness computedHappiness = new ComputedHappiness(5.0, 1.0);

		Map<String, Double> expectedHappiness = new HashMap<String, Double>();
		expectedHappiness.put(ComputedHappiness.SCORE_KEY, 5.0);
		expectedHappiness.put(ComputedHappiness.RELEVANCE_KEY, 1.0);

		HappinessScript mockHappinessScript = Mockito.mock(HappinessScript.class);
		Mockito.when(mockHappinessScript.getTweetText()).thenReturn(tweetText);
		Mockito.when(mockHappinessScript.run()).thenCallRealMethod();

		HappinessAnalyzer mockHappinessAnalyzer = Mockito.mock(HappinessAnalyzer.class);
		Mockito.when(mockHappinessAnalyzer.computeHappiness(Mockito.anyString(), Mockito.any(Properties.class)))
				.thenReturn(computedHappiness);

		mockHappinessScript.properties = properties;
		mockHappinessScript.analyzer = mockHappinessAnalyzer;
		mockHappinessScript.logger = Loggers.getLogger("happiness.script");

		// when
		Object objHappiness = mockHappinessScript.run();

		// then
		assertThat(objHappiness).isNotNull();
		assertThat(objHappiness).isInstanceOf(ComputedHappiness.class);
		assertThat(((ComputedHappiness) objHappiness).toMap()).isEqualTo(expectedHappiness);
	}

	@Test
	public void shouldRunReturnNullWhenNoWordHappiness() throws IOException {
		// given
		assertThat(happinessFile.exists()).isTrue();

		String tweetText = firstWord + " " + secondWord;
		Properties properties = new Properties();

		HappinessScript mockHappinessScript = Mockito.mock(HappinessScript.class);
		Mockito.when(mockHappinessScript.getTweetText()).thenReturn(tweetText);

		HappinessAnalyzer mockHappinessAnalyzer = Mockito.mock(HappinessAnalyzer.class);
		Mockito.when(mockHappinessAnalyzer.computeHappiness(Mockito.anyString(), Mockito.any(Properties.class)))
				.thenReturn(null);

		Mockito.when(mockHappinessScript.run()).thenCallRealMethod();
		mockHappinessScript.properties = properties;
		mockHappinessScript.analyzer = mockHappinessAnalyzer;
		mockHappinessScript.logger = Loggers.getLogger("happiness.script");

		// when
		Object objHappiness = mockHappinessScript.run();

		// then
		assertThat(objHappiness).isNull();
	}

	@Test
	public void shouldRunWorksWithGeolocalizedScript() throws IOException {
		// given
		assertThat(happinessFile.exists()).isTrue();

		String tweetText = firstWord + " " + secondWord;
		Properties properties = new Properties();
		properties.put(HappinessWords.PARAM_FILENAME, happinessFile.getAbsolutePath());

		Map<String, Object> params = new HashMap<String, Object>();
		params.put(HappinessScript.PARAM_GEOLOCALIZED, "true");

		GeoPoint coordinates = new GeoPoint(lat, lng);

		ComputedHappiness computedHappiness = new ComputedHappiness(5.0, 1.0, lat, lng);

		Map<String, Double> expectedHappiness = new HashMap<String, Double>();
		expectedHappiness.put(ComputedHappiness.SCORE_KEY, 5.0);
		expectedHappiness.put(ComputedHappiness.RELEVANCE_KEY, 1.0);
		expectedHappiness.put(ComputedHappiness.LATITUDE_KEY, lat);
		expectedHappiness.put(ComputedHappiness.LONGITUDE_KEY, lng);

		HappinessScript mockGeoHappinessScript = Mockito.mock(HappinessScript.class);

		HappinessAnalyzer mockHappinessAnalyzer = Mockito.mock(HappinessAnalyzer.class);
		Mockito.when(mockHappinessAnalyzer.computeHappiness(Mockito.anyString(), Mockito.any(Properties.class)))
				.thenReturn(computedHappiness);

		Mockito.when(mockGeoHappinessScript.getTweetText()).thenReturn(tweetText);
		Mockito.when(mockGeoHappinessScript.getCoordinates()).thenReturn(coordinates);
		Mockito.when(mockGeoHappinessScript.run()).thenCallRealMethod();

		mockGeoHappinessScript.properties = properties;
		mockGeoHappinessScript.params = params;
		mockGeoHappinessScript.analyzer = mockHappinessAnalyzer;
		mockGeoHappinessScript.logger = Loggers.getLogger("happiness.script");
		// when
		Object objHappiness = mockGeoHappinessScript.run();

		// then
		assertThat(objHappiness).isNotNull();
		assertThat(objHappiness).isInstanceOf(ComputedHappiness.class);
		assertThat(((ComputedHappiness) objHappiness).toMap()).isEqualTo(expectedHappiness);
	}

	public void shouldRunReturnNullIfGeolocalizedAndNoCoordinates() throws IOException {
		// given
		assertThat(happinessFile.exists()).isTrue();

		String tweetText = firstWord + " " + secondWord;
		Properties properties = new Properties();
		properties.put(HappinessWords.PARAM_FILENAME, happinessFile.getAbsolutePath());

		Map<String, Object> params = new HashMap<String, Object>();
		params.put(HappinessScript.PARAM_GEOLOCALIZED, "true");

		ComputedHappiness computedHappiness = new ComputedHappiness(5.0, 1.0);

		HappinessScript mockGeoHappinessScript = Mockito.mock(HappinessScript.class);
		Mockito.when(mockGeoHappinessScript.getTweetText()).thenReturn(tweetText);
		Mockito.when(mockGeoHappinessScript.run()).thenCallRealMethod();

		HappinessAnalyzer mockHappinessAnalyzer = Mockito.mock(HappinessAnalyzer.class);
		Mockito.when(mockHappinessAnalyzer.computeHappiness(Mockito.anyString(), Mockito.any(Properties.class)))
				.thenReturn(computedHappiness);

		mockGeoHappinessScript.properties = properties;
		mockGeoHappinessScript.params = params;
		mockGeoHappinessScript.analyzer = mockHappinessAnalyzer;
		mockGeoHappinessScript.logger = Loggers.getLogger("happiness.script");

		// when
		Object objHappiness = mockGeoHappinessScript.run();

		// then
		assertThat(objHappiness).isNull();
	}

	@Test
	public void shouldRunWorksWithGeolocalizedScriptAndState() throws IOException {
		// given
		assertThat(happinessFile.exists()).isTrue();

		String stateId = "NY";
		String stateName = "New York";
		USAState state = new USAState(stateId, stateName);

		String tweetText = firstWord + " " + secondWord;
		Properties properties = new Properties();
		properties.put(HappinessWords.PARAM_FILENAME, happinessFile.getAbsolutePath());

		Map<String, Object> params = new HashMap<String, Object>();
		params.put(HappinessScript.PARAM_GEOLOCALIZED, "true");
		params.put(HappinessScript.PARAM_USASTATE, "true");

		GeoPoint coordinates = new GeoPoint(lat, lng);

		ComputedHappiness computedHappiness = new ComputedHappiness(5.0, 1.0, lat, lng);
		computedHappiness.setState(state);

		Map<String, Object> expectedState = new HashMap<String, Object>();
		expectedState.put(USAState.STATEID_KEY, stateId);
		expectedState.put(USAState.STATENAME_KEY, stateName);

		Map<String, Object> expectedHappiness = new HashMap<String, Object>();
		expectedHappiness.put(ComputedHappiness.SCORE_KEY, 5.0);
		expectedHappiness.put(ComputedHappiness.RELEVANCE_KEY, 1.0);
		expectedHappiness.put(ComputedHappiness.LATITUDE_KEY, lat);
		expectedHappiness.put(ComputedHappiness.LONGITUDE_KEY, lng);
		expectedHappiness.put(ComputedHappiness.STATE_KEY, expectedState);

		HappinessScript mockGeoHappinessScript = Mockito.mock(HappinessScript.class);

		HappinessAnalyzer mockHappinessAnalyzer = Mockito.mock(HappinessAnalyzer.class);
		Mockito.when(mockHappinessAnalyzer.computeHappiness(Mockito.anyString(), Mockito.any(Properties.class)))
				.thenReturn(computedHappiness);

		Mockito.when(mockGeoHappinessScript.getTweetText()).thenReturn(tweetText);
		Mockito.when(mockGeoHappinessScript.getCoordinates()).thenReturn(coordinates);
		Mockito.when(mockGeoHappinessScript.run()).thenCallRealMethod();

		mockGeoHappinessScript.properties = properties;
		mockGeoHappinessScript.params = params;
		mockGeoHappinessScript.analyzer = mockHappinessAnalyzer;
		mockGeoHappinessScript.logger = Loggers.getLogger("happiness.script");
		// when
		Object objHappiness = mockGeoHappinessScript.run();

		// then
		assertThat(objHappiness).isNotNull();
		assertThat(objHappiness).isInstanceOf(ComputedHappiness.class);
		assertThat(((ComputedHappiness) objHappiness).toMap()).isEqualTo(expectedHappiness);
	}

	@Test
	public void shouldUnwrapWorkForGoodInput() throws IOException {
		// given
		String stateId = "NY";
		String stateName = "New York";
		USAState state = new USAState(stateId, stateName);

		Map<String, Object> expectedState = new HashMap<String, Object>();
		expectedState.put(USAState.STATEID_KEY, stateId);
		expectedState.put(USAState.STATENAME_KEY, stateName);

		Map<String, Object> expectedHappiness = new HashMap<String, Object>();
		expectedHappiness.put(ComputedHappiness.SCORE_KEY, 5.0);
		expectedHappiness.put(ComputedHappiness.RELEVANCE_KEY, 1.0);
		expectedHappiness.put(ComputedHappiness.LATITUDE_KEY, lat);
		expectedHappiness.put(ComputedHappiness.LONGITUDE_KEY, lng);
		expectedHappiness.put(ComputedHappiness.STATE_KEY, expectedState);

		ComputedHappiness computedHappiness = new ComputedHappiness(5.0, 1.0, lat, lng);
		computedHappiness.setState(state);

		HappinessScript happinessScript = new HappinessScript(null, null);

		// when
		Object unwrapResult = happinessScript.unwrap(computedHappiness);

		// then
		assertThat(unwrapResult).isNotNull();
		assertThat(unwrapResult).isEqualTo(expectedHappiness);
	}

}
