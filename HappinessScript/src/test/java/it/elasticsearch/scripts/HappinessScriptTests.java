package it.elasticsearch.scripts;

import static org.fest.assertions.Assertions.assertThat;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

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

		Map<String, Object> params = new HashMap<String, Object>();
		double computedHappiness = (firstHappiness + secondHappiness) / 2;
		HappinessScript happinessScript = new HappinessScript(params);

		// when
		double happiness = happinessScript.computeHappiness(tweetText, wordsHappiness);

		// then
		assertThat(happiness).isEqualTo(computedHappiness);
	}

	@Test
	public void shouldComputeHappinessWorkWithUnValidWords() throws IOException {
		// given
		String firstWord = "hello";
		String secondWord = "world";
		double firstHappiness = 7.0;

		String tweetText = secondWord + " " + secondWord;
		HashMap<String, Double> wordsHappiness = new HashMap<String, Double>();
		wordsHappiness.put(firstWord, firstHappiness);

		Map<String, Object> params = new HashMap<String, Object>();
		double expectedHappiness = 5.0;
		HappinessScript happinessScript = new HappinessScript(params);

		// when
		double happiness = happinessScript.computeHappiness(tweetText, wordsHappiness);

		// then
		assertThat(happiness).isEqualTo(expectedHappiness);
	}
}
