package it.elasticsearch.scripts.utilities;

import static org.fest.assertions.Assertions.assertThat;
import static org.fest.assertions.MapAssert.entry;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.mockito.Mockito;

public class HappinessWordsTests {

	@Test(expected = IOException.class)
	public void shouldGetWordHappinessThrowExceptionIfParamsNull() throws IOException {
		// given
		Map<String, Object> params = null;

		// when
		HappinessWords.getWordHappiness(params);

		// then
	}

	@Test(expected = IOException.class)
	public void shouldGetWordHappinessThrowExceptionIfNoFileNamePassedAsParam() throws IOException {
		// given
		Map<String, Object> params = new HashMap<String, Object>();

		// when
		HappinessWords.getDictionaryFileName(params);

		// then
	}

	@Test(expected = IOException.class)
	public void shouldGetDictionaryFileNameThrowExceptionIfNoFileNamePassedAsParam() throws IOException {
		// given
		Map<String, Object> params = new HashMap<String, Object>();

		// when
		HappinessWords.getDictionaryFileName(params);

		// then
	}

	@Test(expected = IOException.class)
	public void shouldGetDictionaryFileNameThrowExceptionIfFileNameParamIsNull() throws IOException {
		// given
		Map<String, Object> params = new HashMap<String, Object>();
		params.put(HappinessWords.PARAM_FILENAME, null);

		// when
		HappinessWords.getDictionaryFileName(params);

		// then
	}

	@Test
	public void shouldGetDictionaryFileNameWorkIfValidFileNameParam() throws IOException {
		// given
		String inputFileName = "/etc/elasticsearch/wordsfile.txt";
		Map<String, Object> params = new HashMap<String, Object>();
		params.put(HappinessWords.PARAM_FILENAME, inputFileName);

		// when
		String dictionaryFileName = HappinessWords.getDictionaryFileName(params);

		// then
		assertThat(dictionaryFileName).isEqualTo(inputFileName);
	}

	@Test
	public void shouldGetDictionaryColumnsSeparatorReturnDefaultValueIfNoSeparatorPassedAsParam()
			throws IOException {
		// given
		Map<String, Object> params = new HashMap<String, Object>();

		// when
		String columnsSeparator = HappinessWords.getDictionaryColumnsSeparator(params);

		// then
		assertThat(columnsSeparator).isEqualTo(HappinessWords.DEFAULT_SEPARATOR);
	}

	@Test
	public void shouldGetDictionaryColumnsSeparatorReturnDefaultValueIfSeparatorParamIsNull() throws IOException {
		// given
		Map<String, Object> params = new HashMap<String, Object>();
		params.put(HappinessWords.PARAM_SEPARATOR, null);

		// when
		String columnsSeparator = HappinessWords.getDictionaryColumnsSeparator(params);

		// then
		assertThat(columnsSeparator).isEqualTo(HappinessWords.DEFAULT_SEPARATOR);
	}

	@Test
	public void shouldGetDictionaryColumnsSeparatorReturnWorkIfValidSeparatorParam() throws IOException {
		// given
		String inputSeparator = " ";
		Map<String, Object> params = new HashMap<String, Object>();
		params.put(HappinessWords.PARAM_SEPARATOR, inputSeparator);

		// when
		String columnsSeparator = HappinessWords.getDictionaryColumnsSeparator(params);

		// then
		assertThat(columnsSeparator).isEqualTo(inputSeparator);
	}

	@Test
	public void shouldGetHappinessColumnReturnDefaultValueIfNoColumnPassedAsParam() throws IOException {
		// given
		Map<String, Object> params = new HashMap<String, Object>();

		// when
		int happinessColumn = HappinessWords.getHappinessColumn(params);

		// then
		assertThat(happinessColumn).isEqualTo(HappinessWords.DEFAULT_COLUMN);
	}

	@Test
	public void shouldGetHappinessColumnReturnDefaultValueIfColumnParamIsNull() throws IOException {
		// given
		Map<String, Object> params = new HashMap<String, Object>();
		params.put(HappinessWords.PARAM_COLUMN, null);

		// when
		int happinessColumn = HappinessWords.getHappinessColumn(params);

		// then
		assertThat(happinessColumn).isEqualTo(HappinessWords.DEFAULT_COLUMN);
	}

	@Test
	public void shouldGetHappinessColumnReturnWorkIfValidColumnParam() throws IOException {
		// given
		int inputColumn = 7;
		Map<String, Object> params = new HashMap<String, Object>();
		params.put(HappinessWords.PARAM_COLUMN, inputColumn);

		// when
		int happinessColumn = HappinessWords.getHappinessColumn(params);

		// then
		assertThat(happinessColumn).isEqualTo(inputColumn);
	}

	@Test
	public void shouldGetHeaderRowsReturnDefaultValueIfNoHeaderPassedAsParam() throws IOException {
		// given
		Map<String, Object> params = new HashMap<String, Object>();

		// when
		int headerRows = HappinessWords.getHeaderRows(params);

		// then
		assertThat(headerRows).isEqualTo(HappinessWords.DEFAULT_HEAERS);
	}

	@Test
	public void shouldGetHeaderRowsReturnDefaultValueIfHeaderParamIsNull() throws IOException {
		// given
		Map<String, Object> params = new HashMap<String, Object>();
		params.put(HappinessWords.PARAM_HEADERS, null);

		// when
		int headerRows = HappinessWords.getHeaderRows(params);

		// then
		assertThat(headerRows).isEqualTo(HappinessWords.DEFAULT_HEAERS);
	}

	@Test
	public void shouldGetHeaderRowsReturnWorkIfValidHeaderParam() throws IOException {
		// given
		int inputHeaders = 7;
		Map<String, Object> params = new HashMap<String, Object>();
		params.put(HappinessWords.PARAM_HEADERS, inputHeaders);

		// when
		int headerRows = HappinessWords.getHeaderRows(params);

		// then
		assertThat(headerRows).isEqualTo(inputHeaders);
	}

	@Test
	public void shouldReadWordsFileWork() throws IOException {
		// given
		String dictionarySeparator = " ";
		int happinessColumn = 1;
		int headerRows = 2;
		String inputWord = "hi";
		double inputHappiness = 5.3;

		BufferedReader bufferedReader = Mockito.mock(BufferedReader.class);
		Mockito.when(bufferedReader.ready()).thenReturn(true, true, true, false);
		Mockito.when(bufferedReader.readLine()).thenReturn("word happiness", "", inputWord + " " + inputHappiness);

		// when
		HashMap<String, Double> happiness = HappinessWords.readWordsFile(bufferedReader, dictionarySeparator,
				happinessColumn, headerRows);

		// then
		assertThat(happiness).hasSize(1);
		assertThat(happiness).includes(entry(inputWord, inputHappiness));
	}
}
