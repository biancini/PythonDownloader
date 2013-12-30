package it.elasticsearch.scripts;

import static org.fest.assertions.Assertions.assertThat;

import java.io.IOException;
import java.util.HashMap;
import java.util.Properties;

import org.junit.Test;

public class HappinessScriptTests {

	@Test
	public void shouldComputeHappinessWorkWithValidWords() throws IOException {
		// given
		String firstWord = "hello";
		String secondWord = "world";
		double firstHappiness = 7.0;
		double secondHappiness = 2.0;

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
		String firstWord = "hello";
		String secondWord = "world";
		double firstHappiness = 7.0;
		double defaultHappiness = 5.0;

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
		String firstWord = "hello";
		String secondWord = "world";
		double firstHappiness = 7.0;
		double defaultHappiness = 5.0;

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
}
