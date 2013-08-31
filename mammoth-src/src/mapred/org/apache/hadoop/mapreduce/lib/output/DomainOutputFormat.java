package org.apache.hadoop.mapreduce.lib.output;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.NumberFormat;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.FSDataOutputStream;

import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.compress.CompressionCodec;
import org.apache.hadoop.io.compress.GzipCodec;
import org.apache.hadoop.mapreduce.OutputFormat;
import org.apache.hadoop.mapreduce.RecordWriter;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.TaskID;
import org.apache.hadoop.util.*;

public class DomainOutputFormat<K, V> extends TextOutputFormat<K, V> {
	
	private static final NumberFormat NUMBER_FORMAT = NumberFormat.getInstance();
	
	public synchronized static String getUniqueFile(
			TaskAttemptContext context, String name, String extension) {
		TaskID taskId = context.getTaskAttemptID().getTaskID();
		Configuration conf = context.getConf();
		int chunkNumX = conf.getInt("app.chunknum.x", 10);
		int partition = taskId.getId();
		int x = partition%chunkNumX;
		int y = partition/chunkNumX;
		StringBuilder result = new StringBuilder();
		result.append("split");
		result.append('_');
		result.append(NUMBER_FORMAT.format(x));
		result.append('_');
		result.append(NUMBER_FORMAT.format(y));
		result.append(".bsc");
		return result.toString();
	}
}
