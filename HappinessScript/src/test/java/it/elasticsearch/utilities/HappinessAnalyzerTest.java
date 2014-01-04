package it.elasticsearch.utilities;

import static org.fest.assertions.Assertions.assertThat;
import it.elasticsearch.models.ComputedHappiness;
import it.elasticsearch.utilities.HappinessAnalyzer;

import java.io.IOException;
import java.util.HashMap;

import org.junit.Test;

public class HappinessAnalyzerTest {

	private String firstWord = "hello";
	private String secondWord = "world";
	private double firstHappiness = 7.0;
	private double secondHappiness = 2.0;
	private double defaultHappiness = 5.0;

	@Test
	public void shouldComputeHappinessReturnNullWhenWordsHappinessNull() throws IOException {
		// given
		String tweetText = firstWord + " " + secondWord;
		HashMap<String, Double> wordsHappiness = null;

		HappinessAnalyzer happinessAnalyzer = new HappinessAnalyzer();

		// when
		ComputedHappiness happiness = happinessAnalyzer.computeHappiness(tweetText, wordsHappiness);

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

		double computedHappiness = (firstHappiness + secondHappiness) / 2;
		double computedRelevance = 1.0;
		HappinessAnalyzer happinessAnalyzer = new HappinessAnalyzer();

		// when
		ComputedHappiness happiness = happinessAnalyzer.computeHappiness(tweetText, wordsHappiness);

		// then
		assertThat(happiness).isNotNull();
		assertThat(happiness.getScore()).isEqualTo(computedHappiness);
		assertThat(happiness.getRelevance()).isEqualTo(computedRelevance);
	}

	@Test
	public void shouldComputeHappinessWorkWithUnvalidWords() throws IOException {
		// given
		String tweetText = secondWord + " " + secondWord;
		HashMap<String, Double> wordsHappiness = new HashMap<String, Double>();
		wordsHappiness.put(firstWord, firstHappiness);

		double computedRelevance = 0.0;
		HappinessAnalyzer happinessAnalyzer = new HappinessAnalyzer();

		// when
		ComputedHappiness happiness = happinessAnalyzer.computeHappiness(tweetText, wordsHappiness);

		// then
		assertThat(happiness).isNotNull();
		assertThat(happiness.getScore()).isEqualTo(defaultHappiness);
		assertThat(happiness.getRelevance()).isEqualTo(computedRelevance);
	}

	@Test
	public void shouldComputeHappinessWorkWithValidAndUnvalidWords() throws IOException {
		// given
		String tweetText = firstWord + " " + secondWord;
		HashMap<String, Double> wordsHappiness = new HashMap<String, Double>();
		wordsHappiness.put(firstWord, firstHappiness);

		double computedHappiness = (firstHappiness + defaultHappiness) / 2;
		double computedRelevance = 0.5;
		HappinessAnalyzer happinessAnalyzer = new HappinessAnalyzer();

		// when
		ComputedHappiness happiness = happinessAnalyzer.computeHappiness(tweetText, wordsHappiness);

		// then
		assertThat(happiness).isNotNull();
		assertThat(happiness.getScore()).isEqualTo(computedHappiness);
		assertThat(happiness.getRelevance()).isEqualTo(computedRelevance);
	}
}
