package org.apache.hadoop.examples;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.Random;
import java.util.Vector;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.mapred.DomainCalcUtils;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.BackgroundValueSet;
import org.apache.hadoop.mapreduce.lib.input.Coordinate;
import org.apache.hadoop.mapreduce.lib.input.CoordinateValuePair;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TwoDimCoordinate;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.GenericOptionsParser;
import org.apache.hadoop.mapreduce.lib.input.*;
import org.apache.hadoop.examples.domaincalc.functions.*;

public class Mammoth {
	
	static MammothAPI api = new MammothAPI();
	
	// Mapreducing for regions
	public static class MammothTrigger
		extends Mapper<Object, ArrayList<CoordinateValuePair>,
						IntWritable, CoordinateValuePair> {
  		
		final double threshold = -999.000000;
		// final String prefixOfReuseData = "/mnt/s3gator/_mammoth_map_";
		String prefixOfReuseData;
		DomainCalcUtils.ObservationBuilder obBuilder;
		DomainCalcUtils.PartitionBuilder pBuilder;
		
		private IntWritable getHashcodeByCoordinate(
				Coordinate coord,
				PartitionInfo info) {
			return new IntWritable(
					DomainHash.generateHashCode((TwoDimCoordinate)coord, info));
		}
		
		protected void setup(Context context)
			throws IOException, InterruptedException {
			Configuration conf = context.getConfiguration();
			String strPartitionInfoPath = conf.get(
					"app.partition.info.path", "/user/s3gator/conf/partition.info");
			String strObPath = conf.get(
					"app.ob.info.path", "/user/s3gator/conf/observation");
			pBuilder = new DomainCalcUtils.PartitionBuilder(conf);
  			// pBuilder.build(new Path("/user/s3gator/Conf/partition.info"));
			pBuilder.build(new Path(strPartitionInfoPath));
  			obBuilder = new DomainCalcUtils.ObservationBuilder(conf);
  			//obBuilder.build(new Path("/user/s3gator/Conf/observation"), 
  			//		pBuilder.getNumOfPointX(), pBuilder.getNumOfPointY());
  			obBuilder.build(new Path(strObPath), 
  					pBuilder.getNumOfPointX(), pBuilder.getNumOfPointY());
  			
  			prefixOfReuseData = conf.get(
  					"app.reused.intermediate.data.path", "/mnt/s3gator/_mammoth_map_");
		}
		
  		public void map(Object key, ArrayList<CoordinateValuePair> value,
  				Context context) throws IOException, InterruptedException {
  			
  			long timer = System.currentTimeMillis();
  			HashMap<IntWritable,IntWritable> propagationCounter = 
					new HashMap<IntWritable,IntWritable>();
  			Random randomGenerator = new Random();
		
  			ArrayList<CoordinateValuePair> region = value;
  			
  			// map task id is used as its hash code
  			IntWritable mapLocalHashCode = new IntWritable(
  					context.getTaskAttemptID().getTaskIndex());
  			
  			// compute data hash code for reference
  			IntWritable dataLocalHashCode = getHashcodeByCoordinate(
					(TwoDimCoordinate)region.get(0).getCoordinate(),
					pBuilder.getPartInfo());
					
  			// Generate a local file for storing the current map's intermediate output,
  			// which can be re-used by the reduce co-localized scheduled.
  			BufferedOutputStream bos = new BufferedOutputStream(
  					new FileOutputStream(
  							prefixOfReuseData+Integer.toString(mapLocalHashCode.get())));
  			
  			Iterator<CoordinateValuePair> pointItr = region.iterator();
			
  			long stopSign = -1;
  			long counter = 0;
  			while (pointItr.hasNext()) {
  				CoordinateValuePair point = pointItr.next();
  				TwoDimCoordinate tdc = (TwoDimCoordinate)point.getCoordinate();
  				BackgroundValueSet bgValues = point.getValues();
  				Double obValue = new Double(obBuilder.getObservation(point.getCoordinate()));
  				
  				// No need to do computation if the value 
  				// of the observation is less than the threshold
  				if ( obValue == null || obValue.compareTo(threshold) <= 0 ){  					
  					// writeBState(bos, point);
  					
  					context.write(mapLocalHashCode, point);
  				}
  				else {
  					CoordinateValuePair[] results = api.Trigger(
  							(TwoDimCoordinate)point.getCoordinate(), 
  							bgValues, obValue);
  					
  					for (int i=0; i<results.length; ++i) {
  						IntWritable resultHashCode = getHashcodeByCoordinate(
  								results[i].getCoordinate(), pBuilder.getPartInfo());
  						if (resultHashCode.equals(dataLocalHashCode)) {
  							// Computation results in inner part can be saved  
  							// locally as they need not to be propagated to
  							// neighbors. The colocalized-scheduled reduce task, 
  							// which is scheduled to the same physical machine,
  							// can read the file to get results without network propagation
  							
  							// 1. output locally
  							// writeBState(bos, results[i]);
  							
  							// 2. shuffle to reduce
  							context.write(mapLocalHashCode, results[i]);
  						}
  						else {
  							IntWritable neighbor = randNeighborHashCode(
  									mapLocalHashCode, pBuilder.getPartInfo());
  							
  								context.write(neighbor,	results[i]);
  						}
  					}
  					
  					counter++;
					stopSign = context.increProcessedObs();
					if (counter > stopSign){
						System.out.println("Map " + mapLocalHashCode + " stops at " + stopSign);
						break;
					}
  				}
  			}
  			
  			bos.flush();
  			bos.close();
  		}
  		
  		public static void writeBState(
  				BufferedOutputStream bos, 
  				CoordinateValuePair point) throws IOException {
  			
  			Coordinate coord = point.getCoordinate();
  			BackgroundValueSet valueSet = point.getValues();
  			double[] values = valueSet.getValues();
  			
  			// 2 integers, values.length doubles, spaces and 1 return
  			StringBuilder sb = new StringBuilder((4+2)*2+(8+2)*values.length+2);
  			sb.append(coord.getCoordinateAt(0)); sb.append(' ');
  			sb.append(coord.getCoordinateAt(1)); sb.append(' ');
  			for (int i=0; i<values.length; ++i) {
  				sb.append(values[i]); sb.append(' ');
  			}
  			sb.append('\n');
  			bos.write(sb.toString().getBytes(), 0, sb.toString().length());
  		}
  		
  		private IntWritable randNeighborHashCode(
  				IntWritable localHashCode,
  				PartitionInfo info) {
  			
  			ArrayList<Integer> neighbors = new ArrayList<Integer>();
  			// left neighbor exists
  			if ( localHashCode.get() % info.numOfRegionX != 0) {
  				neighbors.add(localHashCode.get() - 1);
  			}
  			// right neighbor exists
  			if ( (localHashCode.get() % info.numOfRegionX) != (info.numOfRegionX-1) ) {
  				neighbors.add(localHashCode.get() + 1);
  			}
  			// top neighbor exists
  			if ( localHashCode.get() - info.numOfRegionX >= 0) {
  				neighbors.add(localHashCode.get() - info.numOfRegionX);
  			}
  			// bottom neighbor exists
  			if ( localHashCode.get() + info.numOfRegionX 
  					< info.numOfRegionX*info.numOfRegionY) {
  				neighbors.add(localHashCode.get() + info.numOfRegionX);
  			}
  			
  			Random rand = new Random();
  			int randInt = rand.nextInt(neighbors.size());
  			
  			return new IntWritable(neighbors.get(randInt));
  		}
	}
	
	public static class MammothAggregator 
		extends Reducer<IntWritable,CoordinateValuePair,
						IntWritable,CoordinateValuePair> {
		
		// final String prefixOfReuseData = "/mnt/s3gator/_mammoth_reduce_";
		String prefixOfReuseData;
		
		public void reduce(IntWritable key, Iterable<CoordinateValuePair> values, 
						Context context) throws IOException, InterruptedException {
			
			Configuration conf = context.getConfiguration();
			prefixOfReuseData = conf.get(
					"app.reduce.out.path", "/mnt/s3gator/_mammoth_reduce_");
			
			IntWritable reduceLocalHashCode = new IntWritable(
  					context.getTaskAttemptID().getTaskIndex());
			BufferedOutputStream bos = new BufferedOutputStream(
  					new FileOutputStream(
  							prefixOfReuseData+Integer.toString(reduceLocalHashCode.get())));
			
			Vector<CoordinateValuePair> valuesInVec = new Vector<CoordinateValuePair>();
			for (CoordinateValuePair value : values) {
				valuesInVec.add(value);
			}
			
			CoordinateValuePair[] valuesInArray = new CoordinateValuePair[valuesInVec.size()];
			valuesInVec.toArray(valuesInArray);
			CoordinateValuePair cvp = api.Aggregate(valuesInArray);
			MammothTrigger.writeBState(bos, cvp);
			/*
			for (CoordinateValuePair value : values) {
				MammothTrigger.writeBState(bos, value);
			}
			*/
			
			bos.flush();
			bos.close();
		}
  	}
	
	public static void main(String[] args) throws Exception {
		Configuration conf = new Configuration();
	    String[] otherArgs = new GenericOptionsParser(conf, args).getRemainingArgs();
	    if (otherArgs.length != 2) {
	      System.err.println("Usage: wordcount <in> <out>");
	      System.exit(2);
	    }
	    
	    Job job = new Job(conf, "Mammoth for state-transition applications.");
	    job.setJarByClass(Mammoth.class);
	    job.setInputFormatClass(DomainInputFormat.class);
	    job.setMapperClass(MammothTrigger.class);
	    job.setReducerClass(MammothAggregator.class);
	    job.setOutputKeyClass(IntWritable.class);
	    job.setOutputValueClass(CoordinateValuePair.class);
	    
	    int chunkNumX = conf.getInt("app.chunknum.x", 10);
	    int chunkNumY = conf.getInt("app.chunknum.y", 10);
	    
	    job.setChunkNumX(chunkNumX);
	    job.setChunkNumY(chunkNumY);
	    job.setNumReduceTasks(chunkNumX*chunkNumY);
	    
	    FileInputFormat.addInputPath(job, new Path(otherArgs[0]));
	    FileOutputFormat.setOutputPath(job, new Path(otherArgs[1]));
	    System.exit(job.waitForCompletion(true) ? 0 : 1);
	}
}
