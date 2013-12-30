package it.elasticsearch.scripts;

import static org.fest.assertions.Assertions.assertThat;
import it.elasticsearch.scripts.utilities.HappinessWords;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.elasticsearch.common.logging.Loggers;
import org.elasticsearch.search.lookup.SourceLookup;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mockito;

public class HappinessScriptTests {

	@Rule
	public TemporaryFolder folder = new TemporaryFolder();

	private File happinessFile = null;
	private String firstWord = "hello";
	private String secondWord = "world";
	private double firstHappiness = 7.0;
	private double secondHappiness = 2.0;
	private double defaultHappiness = 5.0;

	@Before
	public void createTestData() throws IOException {
		happinessFile = folder.newFile("happiness.txt");
		BufferedWriter out = new BufferedWriter(new FileWriter(happinessFile));
		out.write(firstWord + "\t" + firstHappiness + "\n");
		out.write(secondWord + "\t" + secondHappiness + "\n");
		out.close();
	}

	@Test
	public void shouldComputeHappinessReturnNullWhenWordsHappinessNull() throws IOException {
		// given
		String tweetText = firstWord + " " + secondWord;
		HashMap<String, Double> wordsHappiness = null;

		Properties properties = new Properties();
		HappinessScript happinessScript = new HappinessScript(properties);

		// when
		Map<String, Double> happiness = happinessScript.computeHappiness(tweetText, wordsHappiness);

		// then
		assertThat(happiness).isNull();
	}

	@Test
	public void shouldComputeHappinessWorkWithValidWords() throws IOException {
		// given
		String tweetText = firstWord + " " + secondWord;
		HashMap<String, Double> wordsHappiness = new HashMap<String, Double>();
		wordsHappiness.put(firstWord, firstHappiness);
		wordsHappiness.put(secondWord, secondHappiness);

		Properties properties = new Properties();
		double computedHappiness = (firstHappiness + secondHappiness) / 2;
		double computedRelevance = 1.0;
		HappinessScript happinessScript = new HappinessScript(properties);

		// when
		Map<String, Double> happiness = happinessScript.computeHappiness(tweetText, wordsHappiness);

		// then
		assertThat(happiness).isNotNull();
		assertThat(happiness.get(HappinessScript.SCORE_KEY)).isEqualTo(computedHappiness);
		assertThat(happiness.get(HappinessScript.RELEVANCE_KEY)).isEqualTo(computedRelevance);
	}

	@Test
	public void shouldComputeHappinessWorkWithUnvalidWords() throws IOException {
		// given
		String tweetText = secondWord + " " + secondWord;
		HashMap<String, Double> wordsHappiness = new HashMap<String, Double>();
		wordsHappiness.put(firstWord, firstHappiness);

		Properties properties = new Properties();
		double computedRelevance = 0.0;
		HappinessScript happinessScript = new HappinessScript(properties);

		// when
		Map<String, Double> happiness = happinessScript.computeHappiness(tweetText, wordsHappiness);

		// then
		assertThat(happiness).isNotNull();
		assertThat(happiness.get(HappinessScript.SCORE_KEY)).isEqualTo(defaultHappiness);
		assertThat(happiness.get(HappinessScript.RELEVANCE_KEY)).isEqualTo(computedRelevance);
	}

	@Test
	public void shouldComputeHappinessWorkWithValidAndUnvalidWords() throws IOException {
		// given
		String tweetText = firstWord + " " + secondWord;
		HashMap<String, Double> wordsHappiness = new HashMap<String, Double>();
		wordsHappiness.put(firstWord, firstHappiness);

		Properties properties = new Properties();
		double computedHappiness = (firstHappiness + defaultHappiness) / 2;
		double computedRelevance = 0.5;
		HappinessScript happinessScript = new HappinessScript(properties);

		// when
		Map<String, Double> happiness = happinessScript.computeHappiness(tweetText, wordsHappiness);

		// then
		assertThat(happiness).isNotNull();
		assertThat(happiness.get(HappinessScript.SCORE_KEY)).isEqualTo(computedHappiness);
		assertThat(happiness.get(HappinessScript.RELEVANCE_KEY)).isEqualTo(computedRelevance);
	}

	@Test
	public void shouldComputeHappinessWork() throws IOException {
		// given
		assertThat(happinessFile.exists()).isTrue();

		String tweetText = firstWord + " " + secondWord;
		Properties properties = new Properties();
		properties.put(HappinessWords.PARAM_FILENAME, happinessFile.getAbsolutePath());
		double computedHappiness = (firstHappiness + secondHappiness) / 2;
		double computedRelevance = 1.0;
		HappinessScript happinessScript = new HappinessScript(properties);

		// when
		Map<String, Double> happiness = happinessScript.computeHappiness(tweetText);

		// then
		assertThat(happiness).isNotNull();
		assertThat(happiness.get(HappinessScript.SCORE_KEY)).isEqualTo(computedHappiness);
		assertThat(happiness.get(HappinessScript.RELEVANCE_KEY)).isEqualTo(computedRelevance);
	}

	@Test
	public void shouldRunReturnNullIfNoTextInSource() throws IOException {
		// given
		assertThat(happinessFile.exists()).isTrue();

		Properties properties = new Properties();
		properties.put(HappinessWords.PARAM_FILENAME, happinessFile.getAbsolutePath());

		SourceLookup source = Mockito.mock(SourceLookup.class);
		Mockito.when(source.get(HappinessScript.TEXT_FIELDNAME)).thenReturn(null);
		HappinessScript mockHappinessScript = Mockito.mock(HappinessScript.class);
		Mockito.when(mockHappinessScript.run()).thenCallRealMethod();
		Mockito.when(mockHappinessScript.getSource()).thenReturn(source);

		// when
		Object happiness = mockHappinessScript.run();

		// then
		assertThat(happiness).isNull();
	}

	@Test
	@SuppressWarnings("unchecked")
	public void shouldRunWork() throws IOException {
		// given
		assertThat(happinessFile.exists()).isTrue();

		String tweetText = firstWord + " " + secondWord;
		Properties properties = new Properties();
		properties.put(HappinessWords.PARAM_FILENAME, happinessFile.getAbsolutePath());

		Map<String, Double> computedHappiness = new HashMap<String, Double>();
		computedHappiness.put(HappinessScript.SCORE_KEY, 5.0);
		computedHappiness.put(HappinessScript.RELEVANCE_KEY, 1.0);

		SourceLookup source = Mockito.mock(SourceLookup.class);
		Mockito.when(source.get(HappinessScript.TEXT_FIELDNAME)).thenReturn(tweetText);
		HappinessScript mockHappinessScript = Mockito.mock(HappinessScript.class);
		Mockito.when(mockHappinessScript.getSource()).thenReturn(source);
		Mockito.when(mockHappinessScript.computeHappiness(Mockito.anyString())).thenReturn(computedHappiness);

		Mockito.when(mockHappinessScript.run()).thenCallRealMethod();
		mockHappinessScript.properties = properties;
		mockHappinessScript.logger = Loggers.getLogger("happiness.script");

		// when
		Object objHappiness = mockHappinessScript.run();

		// then
		assertThat(objHappiness).isNotNull();
		assertThat(objHappiness instanceof Map<?, ?>).isTrue();
		Map<String, Double> happiness = (Map<String, Double>) objHappiness;
		assertThat(happiness).isEqualTo(computedHappiness);
	}

	@Test
	public void shouldRunReturnNullWhenNoWordHappiness() throws IOException {
		// given
		assertThat(happinessFile.exists()).isTrue();

		String tweetText = firstWord + " " + secondWord;
		Properties properties = new Properties();

		SourceLookup source = Mockito.mock(SourceLookup.class);
		Mockito.when(source.get(HappinessScript.TEXT_FIELDNAME)).thenReturn(tweetText);
		HappinessScript mockHappinessScript = Mockito.mock(HappinessScript.class);
		Mockito.when(mockHappinessScript.getSource()).thenReturn(source);
		Mockito.when(mockHappinessScript.computeHappiness(Mockito.anyString())).thenReturn(null);

		Mockito.when(mockHappinessScript.run()).thenCallRealMethod();
		mockHappinessScript.properties = properties;
		mockHappinessScript.logger = Loggers.getLogger("happiness.script");

		// when
		Object objHappiness = mockHappinessScript.run();

		// then
		assertThat(objHappiness).isNull();
	}

	@Test
	public void shouldRunReturnNullWhenComputedHappinessScoreIsNotPresent() throws IOException {
		// given
		assertThat(happinessFile.exists()).isTrue();

		String tweetText = firstWord + " " + secondWord;
		Properties properties = new Properties();
		properties.put(HappinessWords.PARAM_FILENAME, "/tmp/not_existent_file.txts");

		Map<String, Double> computedHappiness = new HashMap<String, Double>();
		computedHappiness.put(HappinessScript.RELEVANCE_KEY, 1.0);
		SourceLookup source = Mockito.mock(SourceLookup.class);
		Mockito.when(source.get(HappinessScript.TEXT_FIELDNAME)).thenReturn(tweetText);
		HappinessScript mockHappinessScript = Mockito.mock(HappinessScript.class);
		Mockito.when(mockHappinessScript.getSource()).thenReturn(source);
		Mockito.when(mockHappinessScript.computeHappiness(Mockito.anyString())).thenReturn(computedHappiness);

		Mockito.when(mockHappinessScript.run()).thenCallRealMethod();
		mockHappinessScript.properties = properties;
		mockHappinessScript.logger = Loggers.getLogger("happiness.script");

		// when
		Object objHappiness = mockHappinessScript.run();

		// then
		assertThat(objHappiness).isNull();
	}

	@Test
	public void shouldRunReturnNullWhenComputedHappinessRelevanceIsNotPresent() throws IOException {
		// given
		assertThat(happinessFile.exists()).isTrue();

		String tweetText = firstWord + " " + secondWord;
		Properties properties = new Properties();
		properties.put(HappinessWords.PARAM_FILENAME, "/tmp/not_existent_file.txts");

		Map<String, Double> computedHappiness = new HashMap<String, Double>();
		computedHappiness.put(HappinessScript.SCORE_KEY, 5.0);
		SourceLookup source = Mockito.mock(SourceLookup.class);
		Mockito.when(source.get(HappinessScript.TEXT_FIELDNAME)).thenReturn(tweetText);
		HappinessScript mockHappinessScript = Mockito.mock(HappinessScript.class);
		Mockito.when(mockHappinessScript.getSource()).thenReturn(source);
		Mockito.when(mockHappinessScript.computeHappiness(Mockito.anyString())).thenReturn(computedHappiness);

		Mockito.when(mockHappinessScript.run()).thenCallRealMethod();
		mockHappinessScript.properties = properties;
		mockHappinessScript.logger = Loggers.getLogger("happiness.script");

		// when
		Object objHappiness = mockHappinessScript.run();

		// then
		assertThat(objHappiness).isNull();
	}
}
