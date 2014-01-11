package it.elasticsearch.script;

import static org.fest.assertions.Assertions.assertThat;
import it.elasticsearch.models.ComputedHappiness;
import it.elasticsearch.models.GeolocalizedComputedHappiness;
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

public class GeolocalizedHappinessScriptTest {

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
	public void shouldRunReturnNullIfNoCoordinates() throws IOException {
		// given
		assertThat(happinessFile.exists()).isTrue();

		String tweetText = firstWord + " " + secondWord;
		Properties properties = new Properties();
		properties.put(HappinessWords.PARAM_FILENAME, happinessFile.getAbsolutePath());

		ComputedHappiness computedHappiness = new ComputedHappiness(5.0, 1.0);

		GeolocalizedHappinessScript mockGeoHappinessScript = Mockito.mock(GeolocalizedHappinessScript.class);
		Mockito.when(mockGeoHappinessScript.getTweetText()).thenReturn(tweetText);
		Mockito.when(mockGeoHappinessScript.run()).thenCallRealMethod();

		HappinessAnalyzer mockHappinessAnalyzer = Mockito.mock(HappinessAnalyzer.class);
		Mockito.when(mockHappinessAnalyzer.computeHappiness(Mockito.anyString(), Mockito.any(Properties.class)))
				.thenReturn(computedHappiness);

		mockGeoHappinessScript.properties = properties;
		mockGeoHappinessScript.analyzer = mockHappinessAnalyzer;
		mockGeoHappinessScript.logger = Loggers.getLogger("happiness.script");

		// when
		Object objHappiness = mockGeoHappinessScript.run();

		// then
		assertThat(objHappiness).isNull();
	}

	@Test
	public void shouldRunWorksWithValidCoordinates() throws IOException {
		// given
		assertThat(happinessFile.exists()).isTrue();

		String tweetText = firstWord + " " + secondWord;
		Properties properties = new Properties();
		properties.put(HappinessWords.PARAM_FILENAME, happinessFile.getAbsolutePath());

		GeoPoint coordinates = new GeoPoint(lat, lng);

		ComputedHappiness computedHappiness = new ComputedHappiness(5.0, 1.0);

		Map<String, Double> expectedHappiness = new HashMap<String, Double>();
		expectedHappiness.put(GeolocalizedComputedHappiness.SCORE_KEY, 5.0);
		expectedHappiness.put(GeolocalizedComputedHappiness.RELEVANCE_KEY, 1.0);
		expectedHappiness.put(GeolocalizedComputedHappiness.LATITUDE_KEY, lat);
		expectedHappiness.put(GeolocalizedComputedHappiness.LONGITUDE_KEY, lng);

		GeolocalizedHappinessScript mockGeoHappinessScript = Mockito.mock(GeolocalizedHappinessScript.class);
		Mockito.when(mockGeoHappinessScript.getTweetText()).thenReturn(tweetText);
		Mockito.when(mockGeoHappinessScript.getCoordinates()).thenReturn(coordinates);
		Mockito.when(mockGeoHappinessScript.run()).thenCallRealMethod();

		HappinessAnalyzer mockHappinessAnalyzer = Mockito.mock(HappinessAnalyzer.class);
		Mockito.when(mockHappinessAnalyzer.computeHappiness(Mockito.anyString(), Mockito.any(Properties.class)))
				.thenReturn(computedHappiness);

		mockGeoHappinessScript.properties = properties;
		mockGeoHappinessScript.analyzer = mockHappinessAnalyzer;
		mockGeoHappinessScript.logger = Loggers.getLogger("happiness.script");

		// when
		Object objHappiness = mockGeoHappinessScript.run();

		// then
		assertThat(objHappiness).isNotNull();
		assertThat(objHappiness instanceof Map<?, ?>).isTrue();
		assertThat(objHappiness).isEqualTo(expectedHappiness);
	}

}
