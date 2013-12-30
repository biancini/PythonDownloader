package it.elasticsearch.scripts.utilities;

import static org.fest.assertions.Assertions.assertThat;
import static org.fest.assertions.MapAssert.entry;

import java.io.BufferedReader;
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
import org.mockito.Mockito;

public class HappinessWordsTests {

	@Rule
	public TemporaryFolder folder = new TemporaryFolder();

	private File happinessFile = null;

	@Before
	public void createTestData() throws IOException {
		happinessFile = folder.newFile("happiness.txt");
		BufferedWriter out = new BufferedWriter(new FileWriter(happinessFile));
		out.write("hello	5.1\n");
		out.write("world	2.3\n");
		out.close();
	}

	@Test
	public void shouldGetWordHappinessReturn() throws IOException {
		// given
		assertThat(happinessFile.exists()).isTrue();
		Properties properties = new Properties();
		properties.put(HappinessWords.PARAM_FILENAME, happinessFile.getAbsolutePath());

		// when
		HashMap<String, Double> wordHappiness = HappinessWords.getWordHappiness(properties);

		// then
		assertThat(wordHappiness).isNotNull();
	}

	@Test
	public void shouldGetWordHappinessReturnStatic() throws IOException {
		// given
		assertThat(happinessFile.exists()).isTrue();
		Properties properties = new Properties();
		properties.put(HappinessWords.PARAM_FILENAME, happinessFile.getAbsolutePath());

		// when
		HashMap<String, Double> wordHappiness1 = HappinessWords.getWordHappiness(properties);
		HashMap<String, Double> wordHappiness2 = HappinessWords.getWordHappiness(properties);

		// then
		assertThat(wordHappiness1).isEqualTo(wordHappiness2);
	}

	@Test(expected = IOException.class)
	public void shouldInitializeWordHappinessThrowExceptionIfPropertiesNull() throws IOException {
		// given
		Properties properties = null;

		// when
		HappinessWords.initializeWordHappiness(properties);

		// then
	}

	@Test(expected = IOException.class)
	public void shouldGetWordHappinessThrowExceptionIfNoFileNamePassedAsParam() throws IOException {
		// given
		Properties properties = new Properties();

		// when
		HappinessWords.getDictionaryFileName(properties);

		// then
	}

	@Test(expected = IOException.class)
	public void shouldGetDictionaryFileNameThrowExceptionIfNoFileNamePassedAsParam() throws IOException {
		// given
		Properties properties = new Properties();

		// when
		HappinessWords.getDictionaryFileName(properties);

		// then
	}

	@Test(expected = IOException.class)
	public void shouldGetDictionaryFileNameThrowExceptionIfFileNameParamIsNull() throws IOException {
		// given
		Properties properties = new Properties();

		// when
		HappinessWords.getDictionaryFileName(properties);

		// then
	}

	@Test
	public void shouldGetDictionaryFileNameWorkIfValidFileNameParam() throws IOException {
		// given
		String inputFileName = "/etc/elasticsearch/wordsfile.txt";
		Properties properties = new Properties();
		properties.put(HappinessWords.PARAM_FILENAME, inputFileName);

		// when
		String dictionaryFileName = HappinessWords.getDictionaryFileName(properties);

		// then
		assertThat(dictionaryFileName).isEqualTo(inputFileName);
	}

	@Test
	public void shouldGetDictionaryColumnsSeparatorReturnDefaultValueIfNoSeparatorPassedAsParam()
			throws IOException {
		// given
		Properties properties = new Properties();

		// when
		String columnsSeparator = HappinessWords.getDictionaryColumnsSeparator(properties);

		// then
		assertThat(columnsSeparator).isEqualTo(HappinessWords.DEFAULT_SEPARATOR);
	}

	@Test
	public void shouldGetDictionaryColumnsSeparatorReturnDefaultValueIfSeparatorParamIsNull() throws IOException {
		// given
		Properties properties = new Properties();

		// when
		String columnsSeparator = HappinessWords.getDictionaryColumnsSeparator(properties);

		// then
		assertThat(columnsSeparator).isEqualTo(HappinessWords.DEFAULT_SEPARATOR);
	}

	@Test
	public void shouldGetDictionaryColumnsSeparatorReturnWorkIfValidSeparatorParam() throws IOException {
		// given
		String inputSeparator = " ";
		Properties properties = new Properties();
		properties.put(HappinessWords.PARAM_SEPARATOR, inputSeparator);

		// when
		String columnsSeparator = HappinessWords.getDictionaryColumnsSeparator(properties);

		// then
		assertThat(columnsSeparator).isEqualTo(inputSeparator);
	}

	@Test
	public void shouldGetHappinessColumnReturnDefaultValueIfNoColumnPassedAsParam() throws IOException {
		// given
		Properties properties = new Properties();

		// when
		int happinessColumn = HappinessWords.getHappinessColumn(properties);

		// then
		assertThat(happinessColumn).isEqualTo(HappinessWords.DEFAULT_COLUMN);
	}

	@Test
	public void shouldGetHappinessColumnReturnDefaultValueIfColumnParamLessOrEqualToZero() throws IOException {
		// given
		int inputColumn = 0;
		Properties properties = new Properties();
		properties.put(HappinessWords.PARAM_COLUMN, Integer.toString(inputColumn));

		// when
		int happinessColumn = HappinessWords.getHappinessColumn(properties);

		// then
		assertThat(happinessColumn).isEqualTo(HappinessWords.DEFAULT_COLUMN);
	}

	@Test
	public void shouldGetHappinessColumnReturnDefaultValueIfColumnParamIsNull() throws IOException {
		// given
		Properties properties = new Properties();

		// when
		int happinessColumn = HappinessWords.getHappinessColumn(properties);

		// then
		assertThat(happinessColumn).isEqualTo(HappinessWords.DEFAULT_COLUMN);
	}

	@Test
	public void shouldGetHappinessColumnReturnWorkIfValidColumnParam() throws IOException {
		// given
		int inputColumn = 7;
		Properties properties = new Properties();
		properties.put(HappinessWords.PARAM_COLUMN, Integer.toString(inputColumn));

		// when
		int happinessColumn = HappinessWords.getHappinessColumn(properties);

		// then
		assertThat(happinessColumn).isEqualTo(inputColumn);
	}

	@Test
	public void shouldGetHeaderRowsReturnDefaultValueIfNoHeaderPassedAsParam() throws IOException {
		// given
		Properties properties = new Properties();

		// when
		int headerRows = HappinessWords.getHeaderRows(properties);

		// then
		assertThat(headerRows).isEqualTo(HappinessWords.DEFAULT_HEAERS);
	}

	@Test
	public void shouldGetHeaderRowsReturnDefaultValueIfHeaderParamIsNull() throws IOException {
		// given
		Properties properties = new Properties();

		// when
		int headerRows = HappinessWords.getHeaderRows(properties);

		// then
		assertThat(headerRows).isEqualTo(HappinessWords.DEFAULT_HEAERS);
	}

	@Test
	public void shouldGetHeaderRowsReturnDefaultValueIfHeaderParamIsNegative() throws IOException {
		// given
		int inputHeaders = -1;
		Properties properties = new Properties();
		properties.put(HappinessWords.PARAM_HEADERS, Integer.toString(inputHeaders));

		// when
		int headerRows = HappinessWords.getHeaderRows(properties);

		// then
		assertThat(headerRows).isEqualTo(HappinessWords.DEFAULT_HEAERS);
	}

	@Test
	public void shouldGetHeaderRowsReturnWorkIfValidHeaderParam() throws IOException {
		// given
		int inputHeaders = 7;
		Properties properties = new Properties();
		properties.put(HappinessWords.PARAM_HEADERS, Integer.toString(inputHeaders));

		// when
		int headerRows = HappinessWords.getHeaderRows(properties);

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

	@Test
	public void shouldReadWordsComplainAboutColumnTooHigh() throws IOException {
		// given
		String dictionarySeparator = " ";
		int happinessColumn = 3;
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
		assertThat(happiness).hasSize(0);
	}
}
