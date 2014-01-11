package it.elasticsearch.script.facet;

import static org.fest.assertions.Assertions.assertThat;
import it.elasticsearch.models.ComputedHappiness;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.elasticsearch.script.SearchScript;
import org.junit.Test;
import org.mockito.Mockito;

public class HappinessFacetCollectorTest {

	@Test
	public void shouldCollectAddComputedHappinessToResults() throws IOException {
		// given
		ComputedHappiness mapHappiness = new ComputedHappiness(1., 5.);

		SearchScript mapScript = Mockito.mock(SearchScript.class);
		Mockito.doNothing().when(mapScript).setNextDocId(Mockito.anyInt());
		Mockito.when(mapScript.run()).thenReturn(mapHappiness);

		List<ComputedHappiness> searchResults = new ArrayList<ComputedHappiness>();
		HappinessFacetCollector happinessFacetCollector = new HappinessFacetCollector(mapScript, searchResults);

		// when
		happinessFacetCollector.collect(0);

		// then
		assertThat(happinessFacetCollector.getSearchResults()).hasSize(1);
	}
}
