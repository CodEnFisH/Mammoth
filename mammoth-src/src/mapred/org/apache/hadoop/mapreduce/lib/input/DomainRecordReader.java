package org.apache.hadoop.mapreduce.lib.input;

import java.io.IOException;
import java.util.ArrayList;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.compress.CompressionCodec;
import org.apache.hadoop.io.compress.CompressionCodecFactory;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.util.LineReader;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;

public class DomainRecordReader 
	extends RecordReader<LongWritable, ArrayList<CoordinateValuePair>> {
	
	private static final Log LOG = LogFactory.getLog(DomainInputFormat.class);	
	private CompressionCodecFactory compressionCodecs = null;
	private long start;
	private long pos;
	private long end;
	private int maxLineLength;
	private LineReader in;
	private LongWritable key = null;
	private Text line = null;
	private ArrayList<CoordinateValuePair> value = null;
	
	public void initialize(
			InputSplit genericSplit,
			TaskAttemptContext context) throws IOException {
		FileSplit split = (FileSplit) genericSplit;
		Configuration job = context.getConfiguration();
		maxLineLength = job.getInt(
				"mapred.linerecordreader.maxlength", Integer.MAX_VALUE);
		LOG.info("In initialize, MAXLENGTH: " + maxLineLength);
		final Path file = split.getPath();
		compressionCodecs = new CompressionCodecFactory(job);
	    CompressionCodec codec = compressionCodecs.getCodec(file);
	    
		start = split.getStart();
		end = start + split.getLength();
		pos = start;
		
		// open the file and locate the start of the split
		FileSystem fs = file.getFileSystem(job);
		FSDataInputStream fileIn = fs.open(split.getPath());
		boolean skipFirstLine = false;
		if (codec != null) {
			in = new LineReader(codec.createInputStream(fileIn), job);
		}
		else {
			if (start != 0) {
				skipFirstLine = true;
				--start;
				fileIn.seek(start);
			}
			in = new LineReader(fileIn, job);
		}
		if (skipFirstLine) {
			start += in.readLine(new Text(), 0,
                    (int)Math.min((long)Integer.MAX_VALUE, end - start));
		}
		
		value = new ArrayList<CoordinateValuePair>();
	}
	
	private void parseLine (Text line) {
		String strLine = line.toString();
	}
	
	public boolean nextKeyValue() throws IOException {
		if (key == null) {
			key = new LongWritable();
		}
		key.set(pos);
		if (line == null) {
			line = new Text();
		}
		int newSize = 0;
		while (pos < end) {
			newSize = in.readLine(line, maxLineLength, maxLineLength);
			if (newSize == 0 || newSize == 1) break;
			pos += newSize;
			String[] strValues = line.toString().split(" ");
			TwoDimCoordinate c = new TwoDimCoordinate(
					Integer.parseInt(strValues[0]),
					Integer.parseInt(strValues[1]));
			int paramNum = strValues.length - 2;
			double[] params = new double[paramNum];
			for (int i=0; i<paramNum; ++i) {
				params[i] = Double.parseDouble(strValues[2+i]);
			}
			CoordinateValuePair pair = new CoordinateValuePair(c, params);			
			value.add(pair);
		}
		
		if (newSize == 0) {
			key = null;
			value = null;
			return false;
		}
		else return true;
	}
	
	public LongWritable getCurrentKey() {
		return key;
	}
	
	public ArrayList<CoordinateValuePair> getCurrentValue() {
		return value;
	}
	
	public float getProgress() {
		if (start == end) {
			return 0.0f;
		}
		else {
			return Math.min(1.0f, (pos - start) / (float)(end - start));
		}
	}
	
	public synchronized void close() throws IOException {
		if (in != null) {
			in.close(); 
		}
	}
}
