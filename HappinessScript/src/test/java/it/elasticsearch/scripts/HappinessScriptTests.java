package it.elasticsearch.scripts;

import static org.fest.assertions.Assertions.assertThat;
import it.elasticsearch.scripts.utilities.HappinessWords;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Properties;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

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
	public void shouldComputeHappinessWorkWithValidWords() throws IOException {
		// given
		String tweetText = firstWord + " " + secondWord;
		HashMap<String, Double> wordsHappiness = new HashMap<String, Double>();
		wordsHappiness.put(firstWord, firstHappiness);
		wordsHappiness.put(secondWord, secondHappiness);

		Properties properties = new Properties();
		double computedHappiness = (firstHappiness + secondHappiness) / 2;
		HappinessScript happinessScript = new HappinessScript(properties);

		// when
		double happiness = happinessScript.computeHappiness(tweetText, wordsHappiness);

		// then
		assertThat(happiness).isEqualTo(computedHappiness);
	}

	@Test
	public void shouldComputeHappinessWorkWithUnvalidWords() throws IOException {
		// given
		String tweetText = secondWord + " " + secondWord;
		HashMap<String, Double> wordsHappiness = new HashMap<String, Double>();
		wordsHappiness.put(firstWord, firstHappiness);

		Properties properties = new Properties();
		HappinessScript happinessScript = new HappinessScript(properties);

		// when
		double happiness = happinessScript.computeHappiness(tweetText, wordsHappiness);

		// then
		assertThat(happiness).isEqualTo(defaultHappiness);
	}

	@Test
	public void shouldComputeHappinessWorkWithValidAndUnvalidWords() throws IOException {
		// given
		String tweetText = firstWord + " " + secondWord;
		HashMap<String, Double> wordsHappiness = new HashMap<String, Double>();
		wordsHappiness.put(firstWord, firstHappiness);

		Properties properties = new Properties();
		double computedHappiness = (firstHappiness + defaultHappiness) / 2;
		HappinessScript happinessScript = new HappinessScript(properties);

		// when
		double happiness = happinessScript.computeHappiness(tweetText, wordsHappiness);

		// then
		assertThat(happiness).isEqualTo(computedHappiness);
	}

	@Test
	public void shouldComputeHappinessWork() throws IOException {
		// given
		assertThat(happinessFile.exists()).isTrue();

		String tweetText = firstWord + " " + secondWord;
		Properties properties = new Properties();
		properties.put(HappinessWords.PARAM_FILENAME, happinessFile.getAbsolutePath());

		double computedHappiness = (firstHappiness + secondHappiness) / 2;
		HappinessScript happinessScript = new HappinessScript(properties);

		// when
		double happiness = happinessScript.computeHappiness(tweetText);

		// then
		assertThat(happiness).isEqualTo(computedHappiness);
	}
}
