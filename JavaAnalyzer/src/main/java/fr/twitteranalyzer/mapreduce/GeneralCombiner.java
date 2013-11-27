package fr.twitteranalyzer.mapreduce;

import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.util.Tool;

public class GeneralCombiner extends Configured implements Tool {

	private Job job = null;

	public GeneralCombiner(Job job) {
		this.job = job;
	}

	public int run(String[] arg) throws Exception {
		return job.waitForCompletion(true) ? 0 : 1; // this will execute the job
	}

}
