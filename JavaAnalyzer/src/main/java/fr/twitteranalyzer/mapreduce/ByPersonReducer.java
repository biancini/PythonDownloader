package fr.twitteranalyzer.mapreduce;

import java.io.IOException;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

class ByPersonReducer extends
		Reducer<Text, LongWritable, Text, LongWritable> {

	@Override
	protected void reduce(Text word, Iterable<LongWritable> values,
			Context context) throws IOException, InterruptedException {
		long accumulator = 0;
		for (LongWritable value : values) {
			accumulator += value.get();
		}
		context.write(word, new LongWritable(accumulator));
	}
}