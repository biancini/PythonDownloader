package it.elasticsearch.utilities;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Properties;

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

	public synchronized static HashMap<String, Double> getWordHappiness(Properties properties) {
		return getWordHappiness(properties, false);
	}

	public synchronized static HashMap<String, Double> getWordHappiness(Properties properties, boolean reInitialize) {
		try {
			if (wordHappiness == null || reInitialize) {
				wordHappiness = initializeWordHappiness(properties);
			}
		} catch (IOException e) {
			logger.error("Exception while initializing word happiness: {}.", e);
			wordHappiness = null;
		}

		return wordHappiness;
	}

	protected static HashMap<String, Double> initializeWordHappiness(Properties properties) throws IOException {
		if (properties == null) {
			throw new IOException();
		}

		String dictionaryFileName = getDictionaryFileName(properties);
		String dictionarySeparator = getDictionaryColumnsSeparator(properties);
		int happinessColumn = getHappinessColumn(properties);
		int headerRows = getHeaderRows(properties);

		logger.debug("Initializing word happiness from file: {}", dictionaryFileName);
		BufferedReader bufferedReader = new BufferedReader(new FileReader(dictionaryFileName));
		return readWordsFile(bufferedReader, dictionarySeparator, happinessColumn, headerRows);
	}

	protected static HashMap<String, Double> readWordsFile(BufferedReader bufferedReader,
			String dictionarySeparator, int happinessColumn, int headerRows) throws FileNotFoundException,
			IOException {
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
				} else {
					logger.warn("The happiness column is beyond available values.", Arrays.toString(vals));
				}
			}
			count++;
		}

		bufferedReader.close();
		return newWordHappiness;
	}

	protected static int getHeaderRows(Properties properties) {
		int headerRows = DEFAULT_HEAERS;
		String paramHeadersRow = properties.getProperty(PARAM_HEADERS);
		logger.debug("Read param header rows = {}.", paramHeadersRow);

		if (paramHeadersRow != null) {
			int intHeaders = Integer.parseInt(paramHeadersRow);
			if (intHeaders >= 1) {
				headerRows = intHeaders;
			} else {
				logger.warn("Wrong parameter in configuration for headers: {}.", paramHeadersRow);
			}
		}
		return headerRows;
	}

	protected static int getHappinessColumn(Properties properties) {
		int happinessColumn = DEFAULT_COLUMN;
		String paramColumn = properties.getProperty(PARAM_COLUMN);
		logger.debug("Read param column = {}.", paramColumn);

		if (paramColumn != null) {
			int intColumn = Integer.parseInt(paramColumn);
			if (intColumn >= 1) {
				happinessColumn = intColumn;
			} else {
				logger.warn("Wrong parameter in configuration for column: {}.", paramColumn);
			}
		}
		return happinessColumn;
	}

	protected static String getDictionaryColumnsSeparator(Properties properties) {
		String dictionarySeparator = DEFAULT_SEPARATOR;
		String paramFileSeparator = properties.getProperty(PARAM_SEPARATOR);
		logger.debug("Read param fileseparator = {}.", paramFileSeparator);

		if (paramFileSeparator != null) {
			dictionarySeparator = paramFileSeparator;
		}
		return dictionarySeparator;
	}

	protected static String getDictionaryFileName(Properties properties) throws IOException {
		String dictionaryFileName = properties.getProperty(PARAM_FILENAME);
		logger.debug("Read param filename = {}.", dictionaryFileName);

		if (dictionaryFileName == null) {
			throw new IOException();
		}
		return dictionaryFileName;
	}
}
