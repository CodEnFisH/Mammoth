
// Added by Xin, 01/19/2012

package org.apache.hadoop.mapreduce.lib.input;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.PathFilter;
import org.apache.hadoop.fs.BlockLocation;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.InputFormat;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.JobContext;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;

public class DomainInputFormat 
		extends FileInputFormat<LongWritable, ArrayList<CoordinateValuePair>> {
	private static final Log LOG = LogFactory.getLog(DomainInputFormat.class);
	static final String NUM_INPUT_FILES = "mapreduce.input.num.files";
	
	public RecordReader<LongWritable, ArrayList<CoordinateValuePair>> createRecordReader(
			InputSplit split, TaskAttemptContext context) {
		return new DomainRecordReader();
	}
	
	public List<InputSplit> getSplits(
			JobContext job) throws IOException {
		int numRegionX = 10;
		int numRegionY = 10;
		
		InputSplit [] splitArray = new InputSplit[numRegionX*numRegionY];
		
		// generate splits of domains
		List<InputSplit> splits = new ArrayList<InputSplit>();
		List<FileStatus> files = listStatus(job);
		for (FileStatus file : files) {
			Path path = file.getPath();
			FileSystem fs = path.getFileSystem(job.getConfiguration());
			long length = file.getLen();
			BlockLocation[] blkLocations = 
				fs.getFileBlockLocations(file, 0, length);
			
			// get file name
			String fn = path.getName();
			String fnNoSuffix = fn.substring(0, fn.indexOf("."));
			String [] indexes = fnNoSuffix.split("_");
			int index1 = Integer.parseInt(indexes[1]);
			int index2 = Integer.parseInt(indexes[2]);
			int indexInArray = index1 + numRegionX * index2;
			splitArray[indexInArray] = new FileSplit(
					path, 0, length, blkLocations[0].getHosts());
		}
		
		for (int i=0; i<splitArray.length; ++i) {
			splits.add(splitArray[i]);
		}
		
		// List<InputSplit> splits = new ArrayList<InputSplit>(
		//		Arrays.asList(splitArray));
		/*
		// create one split for each file
		splits.add(new FileSplit(
				path, 0, length, blkLocations[0].getHosts()));
		*/
		job.getConfiguration().setLong(NUM_INPUT_FILES, files.size());
		
		return splits;
	}
}
