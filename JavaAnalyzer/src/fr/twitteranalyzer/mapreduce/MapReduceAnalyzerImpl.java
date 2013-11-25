package fr.twitteranalyzer.mapreduce;

import java.io.IOException;
import java.util.Date;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.MapWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.elasticsearch.hadoop.mr.ESInputFormat;
import org.elasticsearch.hadoop.mr.ESOutputFormat;

import fr.twitteranalyzer.Analyzer;
import fr.twitteranalyzer.exceptions.AnalyzerException;

public abstract class MapReduceAnalyzerImpl implements Analyzer {

	public static final String elasticSearchHost = "localhost";
	public static final int elasticSearchPort = 9200;

	public abstract void runAnalysis(Date from, Date to)
			throws AnalyzerException;

	public abstract String getJobName();

	public abstract void additionalConfigurations(Configuration conf,
			Object... params);

	public Configuration getMapReduceConfiguration(Object... params) {
		Configuration conf = new Configuration();
		conf.set("es.host", elasticSearchHost);
		conf.setInt("es.port", elasticSearchPort);
		conf.setBoolean("mapred.map.tasks.speculative.execution", false);
		additionalConfigurations(conf, params);
		return conf;
	}

	public Job getMapReduceJob(Object... params) throws IOException,
			ClassNotFoundException, InterruptedException {
		Job mapReduceJob = new Job(getMapReduceConfiguration(params),
				getJobName());

		mapReduceJob.setInputFormatClass(ESInputFormat.class);
		mapReduceJob.setOutputFormatClass(ESOutputFormat.class);

		mapReduceJob.setOutputKeyClass(Text.class);
		mapReduceJob.setOutputValueClass(MapWritable.class);

		mapReduceJob.waitForCompletion(true);

		return mapReduceJob;
	}

}
