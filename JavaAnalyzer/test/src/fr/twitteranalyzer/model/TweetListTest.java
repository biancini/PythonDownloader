package fr.twitteranalyzer.model;

import static org.fest.assertions.Assertions.assertThat;

import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.junit.Test;

public class TweetListTest {

	public static final String elasticSearchHost = "localhost";
	public static final int elasticSearchPort = 9300;

	public static final String SEPARATOR = "\n";
	public static final String FIRST_ELEMENT = "Je suis le premier élément";
	public static final String SECOND_ELEMENT = "Je suis le deuxième élément";
	public static final String STEMMED_SEPARATOR = " ";
	public static final String FIRST_STEMMED_RESULT = "premi élément";
	public static final String SECOND_STEMMED_RESULT = "deuxiem élément";

	@Test
	public void shouldGetAllElementsNotAnalyzedReturnAllTweets() {
		// given
		TweetList tweetList = new TweetList(null);
		tweetList.add(FIRST_ELEMENT);
		tweetList.add(SECOND_ELEMENT);

		// when
		String returnValue = tweetList.getAllElements(SEPARATOR, false);

		// then
		assertThat(returnValue).isEqualTo(
				FIRST_ELEMENT + SEPARATOR + SECOND_ELEMENT);
	}

	@Test
	public void shouldGetAllElementsAnalyzedReturnAllTweets() {
		// given
		Client client = getElasticSearchClient();

		TweetList tweetList = new TweetList(client);
		tweetList.add(FIRST_ELEMENT);
		tweetList.add(SECOND_ELEMENT);

		// when
		String returnValue = tweetList.getAllElements(SEPARATOR, true);

		// then
		assertThat(returnValue).isEqualTo(
				FIRST_STEMMED_RESULT + STEMMED_SEPARATOR
						+ SECOND_STEMMED_RESULT);
	}

	private Client getElasticSearchClient() {
		Settings settings = ImmutableSettings.settingsBuilder()
				.put("cluster.name", "frenchtweets").build();

		TransportClient transportClient = new TransportClient(settings);
		transportClient = transportClient
				.addTransportAddress(new InetSocketTransportAddress(
						elasticSearchHost, elasticSearchPort));

		return transportClient;
	}

}
