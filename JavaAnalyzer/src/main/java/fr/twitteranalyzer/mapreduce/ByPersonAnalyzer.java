package fr.twitteranalyzer.mapreduce;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.util.ToolRunner;

import fr.twitteranalyzer.Analyzer;
import fr.twitteranalyzer.exceptions.UtilsException;

public class ByPersonAnalyzer extends MapReduceAnalyzerImpl implements Analyzer {

	public String getJobName() {
		return "Map-Reduce ByPersonAnalyzer";
	}

	public void additionalConfigurations(Configuration conf, Object... params) {
		String date = extractDate(0, params);

		String indexName = "twitter/tweets";
		String query = "created%5Fat:%5B" + date + "+TO+" + date + "%5D";
		conf.set("es.resource", indexName + "/_search?q=" + query);
	}

	private String extractDate(int index, Object... params) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		String stringDate = "*";
		if (params.length > 0 && params[index] instanceof Date) {
			stringDate = dateFormat.format((Date) params[index]);
		}
		return stringDate;
	}

	public void runAnalysis(Date date) throws UtilsException {
		try {
			GeneralCombiner combiner = new GeneralCombiner(getMapReduceJob(date));
			ToolRunner.run(getMapReduceConfiguration(date), combiner, null);
		} catch (Exception e) {
			throw new UtilsException(e);
		}
	}
}
