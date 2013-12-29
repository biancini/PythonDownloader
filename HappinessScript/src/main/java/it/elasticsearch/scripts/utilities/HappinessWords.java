package it.elasticsearch.scripts.utilities;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.elasticsearch.common.logging.ESLogger;
import org.elasticsearch.common.logging.Loggers;

public class HappinessWords {

	public static final String PARAM_FILENAME = "filename";
	public static final String PARAM_SEPARATOR = "separator";
	public static final String PARAM_COLUMN = "column";
	public static final String PARAM_HEADERS = "headers";

	public static final String DEFAULT_SEPARATOR = "\t";
	public static final int DEFAULT_COLUMN = 1;
	public static final int DEFAULT_HEAERS = 0;

	private static final ESLogger logger = Loggers.getLogger("happiness.script");
	private static HashMap<String, Double> wordHappiness = null;

	public synchronized static HashMap<String, Double> getWordHappiness(Map<String, Object> params)
			throws IOException {

		if (wordHappiness == null) {
			wordHappiness = initializeWordHappiness(params);
		}

		return wordHappiness;
	}

	protected static HashMap<String, Double> initializeWordHappiness(Map<String, Object> params)
			throws IOException {

		if (params == null) {
			throw new IOException();
		}

		String dictionaryFileName = getDictionaryFileName(params);
		String dictionarySeparator = getDictionaryColumnsSeparator(params);
		int happinessColumn = getHappinessColumn(params);
		int headerRows = getHeaderRows(params);

		logger.debug("Initializing word happiness from file: {}", dictionaryFileName);
		BufferedReader bufferedReader = new BufferedReader(new FileReader(dictionaryFileName));
		return readWordsFile(bufferedReader, dictionarySeparator, happinessColumn, headerRows);
	}

	protected static HashMap<String, Double> readWordsFile(BufferedReader bufferedReader, String dictionarySeparator,
			int happinessColumn, int headerRows) throws FileNotFoundException, IOException {
		HashMap<String, Double> newWordHappiness = new HashMap<String, Double>();

		int count = 0;
		while (bufferedReader.ready()) {
			String row = bufferedReader.readLine();
			if (count < headerRows) {
				logger.trace("Ignoring header row: {}", row);
			} else {
				logger.trace("Considering the following row from file: {}", row);
				String[] vals = row.split(dictionarySeparator);

				if (happinessColumn < vals.length) {
					String word = vals[0];
					double happiness = Double.parseDouble(vals[happinessColumn]);

					logger.trace("Adding word {} with happiness = {}.", word, happiness);
					newWordHappiness.put(word, happiness);
				}
			}
			count++;
		}

		bufferedReader.close();
		return newWordHappiness;
	}

	protected static int getHeaderRows(Map<String, Object> params) {
		int headerRows = DEFAULT_HEAERS;
		Integer paramHeadersRow = (Integer) params.get(PARAM_HEADERS);
		logger.debug("Read param header rows = {}.", paramHeadersRow);

		if (paramHeadersRow != null) {
			if (paramHeadersRow.intValue() >= 1) {
				headerRows = paramHeadersRow.intValue();
			}
		}
		return headerRows;
	}

	protected static int getHappinessColumn(Map<String, Object> params) {
		int happinessColumn = DEFAULT_COLUMN;
		Integer paramColumn = (Integer) params.get(PARAM_COLUMN);
		logger.debug("Read param column = {}.", paramColumn);

		if (paramColumn != null) {
			if (paramColumn.intValue() >= DEFAULT_COLUMN) {
				happinessColumn = paramColumn.intValue();
			}
		}
		return happinessColumn;
	}

	protected static String getDictionaryColumnsSeparator(Map<String, Object> params) {
		String dictionarySeparator = DEFAULT_SEPARATOR;
		String paramFileSeparator = (String) params.get(PARAM_SEPARATOR);
		logger.debug("Read param fileseparator = {}.", paramFileSeparator);

		if (paramFileSeparator != null) {
			dictionarySeparator = paramFileSeparator;
		}
		return dictionarySeparator;
	}

	protected static String getDictionaryFileName(Map<String, Object> params) throws IOException {
		String dictionaryFileName = (String) params.get(PARAM_FILENAME);
		logger.debug("Read param filename = {}.", dictionaryFileName);

		if (dictionaryFileName == null) {
			throw new IOException();
		}
		return dictionaryFileName;
	}
}
