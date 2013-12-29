package it.elasticsearch.script.utilities;

import java.io.BufferedReader;
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

	private static final ESLogger logger = Loggers.getLogger("happiness.script");
	private static HashMap<String, Double> wordHappiness = null;

	public synchronized static HashMap<String, Double> getWordHappiness(Map<String, Object> params)
			throws IOException {

		if (wordHappiness == null) {
			wordHappiness = initializeWordHappiness(params);
		}

		return wordHappiness;
	}

	private static HashMap<String, Double> initializeWordHappiness(Map<String, Object> params) throws IOException {
		String dictionaryFileName = null;
		String dictionarySeparator = "\t";
		int happinessColumn = 1;
		int headerRows = 0;

		if (params == null)
			throw new IOException();
		String paramFilename = (String) params.get(PARAM_FILENAME);
		logger.debug("Read param filename = {}.", paramFilename);

		if (paramFilename != null) {
			dictionaryFileName = paramFilename;

			String paramFileSeparator = (String) params.get(PARAM_SEPARATOR);
			logger.debug("Read param fileseparator = {}.", paramFileSeparator);

			if (paramFileSeparator != null) {
				dictionarySeparator = paramFileSeparator;
			}

			Integer paramColumn = (Integer) params.get(PARAM_COLUMN);
			logger.debug("Read param column = {}.", paramColumn);

			if (paramColumn != null) {
				if (paramColumn.intValue() >= 1) {
					happinessColumn = paramColumn.intValue();
				}
			}

			Integer paramHeadersRow = (Integer) params.get(PARAM_HEADERS);
			logger.debug("Read param header rows = {}.", paramHeadersRow);

			if (paramHeadersRow != null) {
				if (paramHeadersRow.intValue() >= 1) {
					headerRows = paramHeadersRow.intValue();
				}
			}
		}

		HashMap<String, Double> newWordHappiness = new HashMap<String, Double>();
		logger.debug("Initializing word happiness from file: {}", dictionaryFileName);
		BufferedReader in = new BufferedReader(new FileReader(dictionaryFileName));

		int count = 0;
		while (in.ready()) {
			String row = in.readLine();
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

		in.close();

		return newWordHappiness;
	}
}
