package fr.twitteranalyzer.model;

import static org.fest.assertions.Assertions.assertThat;

import org.junit.Test;

public class TweetFieldsTest {

	@Test
	public void shouldGetFieldsListReturnANonEmptyList() {
		// given

		// when
		String[] returnValue = TweetsFields.getFieldList();

		// then
		assertThat(returnValue).isNotEmpty();
	}

}
