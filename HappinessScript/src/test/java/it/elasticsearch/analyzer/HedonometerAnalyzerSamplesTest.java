package it.elasticsearch.analyzer;

import static org.fest.assertions.Assertions.assertThat;
import it.elasticsearch.models.ComputedHappiness;
import it.elasticsearch.utilities.HappinessWords;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Properties;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;

public class HedonometerAnalyzerSamplesTest {

	@Rule
	public TemporaryFolder folder = new TemporaryFolder();

	private File happinessFile = null;
	private Properties properties = null;

	@Before
	public void createTestData() throws IOException {
		happinessFile = folder.newFile("hedonometer.txt");

		InputStream in = this.getClass().getClassLoader()
				.getResourceAsStream("it/elasticsearch/analyzer/hedonometer.txt");
		FileOutputStream out = new FileOutputStream(happinessFile);

		try {
			try {
				final byte[] buffer = new byte[1024];
				int n;

				while ((n = in.read(buffer)) != -1) {
					out.write(buffer, 0, n);
				}
			} finally {
				out.close();
			}
		} finally {
			in.close();
		}

		properties = new Properties();
		properties.setProperty("filename", happinessFile.getAbsolutePath());
		properties.setProperty("separator", "\t");
		properties.setProperty("column", "2");
		properties.setProperty("headers", "4");
	}

	@Ignore
	public void shouldSample1WorkWithNotOnlyRelevant() throws IOException {
		// given
		assertThat(happinessFile.exists()).isTrue();
		assertThat(properties).isNotNull();

		String tweetText = "RT @Country_Voices: She's a good hearted woman in love with a good timing man. She loves him in spite of his ways that she don't understand…";
		HashMap<String, Double> wordsHappiness = HappinessWords.getWordHappiness(properties);

		double computedHappiness = 5.8014285714285725;
		double computedRelevance = 0.9642857142857143;
		HedonometerAnalyzer hedonometer = new HedonometerAnalyzer();

		// when
		ComputedHappiness happiness = hedonometer.computeHappiness(tweetText, wordsHappiness);

		// then
		assertThat(happiness).isNotNull();
		assertThat(happiness.getScore()).isEqualTo(computedHappiness);
		assertThat(happiness.getRelevance()).isEqualTo(computedRelevance);
	}
}
