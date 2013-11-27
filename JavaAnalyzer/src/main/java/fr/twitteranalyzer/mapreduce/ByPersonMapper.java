package fr.twitteranalyzer.mapreduce;

import java.io.IOException;
import java.util.StringTokenizer;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.MapWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

class ByPersonMapper extends
		Mapper<LongWritable, Text, MapWritable, LongWritable> {

	private Text word = new Text();
	private final static LongWritable ONE = new LongWritable(1);

	@Override
	protected void map(LongWritable offset, Text text, Context context)
			throws IOException, InterruptedException {

		StringTokenizer iter = new StringTokenizer(text.toString());
		MapWritable writeable = new MapWritable();
		while (iter.hasMoreTokens()) {
			writeable.put(this.word, ONE);
			this.word.set(iter.nextToken());
			context.write(writeable, ONE);
		}
	}

}