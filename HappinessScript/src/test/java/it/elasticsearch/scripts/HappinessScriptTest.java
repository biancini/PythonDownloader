package it.elasticsearch.scripts;

import static org.fest.assertions.Assertions.assertThat;
import it.elasticsearch.scripts.utilities.Analyzer;
import it.elasticsearch.scripts.utilities.HappinessAnalyzer;
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

public class HappinessScriptTest {

	@Rule
	public TemporaryFolder folder = new TemporaryFolder();

	private File happinessFile = null;
	private String firstWord = "hello";
	private String secondWord = "world";
	private double firstHappiness = 7.0;
	private double secondHappiness = 2.0;

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
	@SuppressWarnings("unchecked")
	public void shouldRunWork() throws IOException {
		// given
		assertThat(happinessFile.exists()).isTrue();

		String tweetText = firstWord + " " + secondWord;
		Properties properties = new Properties();
		properties.put(HappinessWords.PARAM_FILENAME, happinessFile.getAbsolutePath());

		Map<String, Double> computedHappiness = new HashMap<String, Double>();
		computedHappiness.put(Analyzer.SCORE_KEY, 5.0);
		computedHappiness.put(Analyzer.RELEVANCE_KEY, 1.0);

		HappinessScript mockHappinessScript = Mockito.mock(HappinessScript.class);
		Mockito.when(mockHappinessScript.getTweetText()).thenReturn(tweetText);

		HappinessAnalyzer mockHappinessAnalyzer = Mockito.mock(HappinessAnalyzer.class);
		Mockito.when(mockHappinessAnalyzer.computeHappiness(Mockito.anyString(), Mockito.any(Properties.class)))
				.thenReturn(computedHappiness);

		Mockito.when(mockHappinessScript.run()).thenCallRealMethod();
		mockHappinessScript.properties = properties;
		mockHappinessScript.analyzer = mockHappinessAnalyzer;
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
	public void shouldRunReturnNullWhenComputedHappinessScoreIsNotPresent() throws IOException {
		// given
		assertThat(happinessFile.exists()).isTrue();

		String tweetText = firstWord + " " + secondWord;
		Properties properties = new Properties();
		properties.put(HappinessWords.PARAM_FILENAME, "/tmp/not_existent_file.txts");

		Map<String, Double> computedHappiness = new HashMap<String, Double>();
		computedHappiness.put(Analyzer.RELEVANCE_KEY, 1.0);
		HappinessScript mockHappinessScript = Mockito.mock(HappinessScript.class);
		Mockito.when(mockHappinessScript.getTweetText()).thenReturn(tweetText);

		HappinessAnalyzer mockHappinessAnalyzer = Mockito.mock(HappinessAnalyzer.class);
		Mockito.when(mockHappinessAnalyzer.computeHappiness(Mockito.anyString(), Mockito.any(Properties.class)))
				.thenReturn(computedHappiness);

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
	public void shouldRunReturnNullWhenComputedHappinessRelevanceIsNotPresent() throws IOException {
		// given
		assertThat(happinessFile.exists()).isTrue();

		String tweetText = firstWord + " " + secondWord;
		Properties properties = new Properties();
		properties.put(HappinessWords.PARAM_FILENAME, "/tmp/not_existent_file.txts");

		Map<String, Double> computedHappiness = new HashMap<String, Double>();
		computedHappiness.put(Analyzer.SCORE_KEY, 5.0);
		SourceLookup source = Mockito.mock(SourceLookup.class);
		Mockito.when(source.get(HappinessScript.TEXT_FIELDNAME)).thenReturn(tweetText);
		HappinessScript mockHappinessScript = Mockito.mock(HappinessScript.class);
		Mockito.when(mockHappinessScript.getTweetText()).thenReturn(HappinessScript.TEXT_FIELDNAME);

		HappinessAnalyzer mockHappinessAnalyzer = Mockito.mock(HappinessAnalyzer.class);
		Mockito.when(mockHappinessAnalyzer.computeHappiness(Mockito.anyString(), Mockito.any(Properties.class)))
				.thenReturn(computedHappiness);

		Mockito.when(mockHappinessScript.run()).thenCallRealMethod();
		mockHappinessScript.properties = properties;
		mockHappinessScript.analyzer = mockHappinessAnalyzer;
		mockHappinessScript.logger = Loggers.getLogger("happiness.script");

		// when
		Object objHappiness = mockHappinessScript.run();

		// then
		assertThat(objHappiness).isNull();
	}
}
