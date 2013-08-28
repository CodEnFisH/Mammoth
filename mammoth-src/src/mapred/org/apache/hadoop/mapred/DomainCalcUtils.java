// package org.apache.hadoop.examples;
package org.apache.hadoop.mapred;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.lib.input.Coordinate;
import org.apache.hadoop.mapreduce.lib.input.PartitionInfo;
import org.apache.hadoop.mapreduce.lib.input.TwoDimCoordinate;
import org.apache.hadoop.util.LineReader;

public class DomainCalcUtils {
	public static class ObservationBuilder {
		private Configuration conf;
		double [][] obs;
		final double threshold = -999.000000;
		
		public ObservationBuilder(Configuration conf) {
			this.conf = conf;
		}
		
		public void build (Path obFilePath, int dimx, int dimy) throws IOException {
			FileSystem fs = obFilePath.getFileSystem(conf);
			FileStatus status = fs.getFileStatus(obFilePath);
			long length = status.getLen();
			FSDataInputStream obFileInStream = fs.open(obFilePath);
			LineReader in = new LineReader(obFileInStream, conf);
			obs = new double[dimy][dimx];
			
			Text line = new Text();
			int readSize = 0;
			int pos = 0;
			int lineCount = 0;
			while (pos < length) {
				readSize = in.readLine(line);
				String[] records = line.toString().split(" ");
				obs[lineCount/dimx][lineCount%dimx] = Double.parseDouble(records[2]);
				lineCount++;
				pos += readSize;
			}
			obFileInStream.close();
		}
		
		public double getObservation(Coordinate c) {
			TwoDimCoordinate tdc = (TwoDimCoordinate) c;
			return obs[tdc.getY()][tdc.getX()];
		}
		
		public int getRegionWorkload(int regionId, PartitionBuilder pb) {
			PartitionInfo pi = pb.getPartInfo();
			int idxPosX = regionId % pi.numOfRegionX;
			int idxPosY = regionId / pi.numOfRegionX;
			
			int startPosX = idxPosX * pi.regionLenX;
			int startPosY = idxPosY * pi.regionLenY;
			int endPosX = (startPosX + pi.regionLenX > pi.numOfPointX) 
				? (pi.numOfPointX) : (startPosX + pi.regionLenX);
			int endPosY = (startPosY + pi.regionLenY > pi.numOfPointY) 
				? (pi.numOfPointY) : (startPosY + pi.regionLenY);
			
			int wlAmount = 0;
			for (int j=startPosY; j<endPosY; ++j) {
				for (int i=startPosX; i<endPosX; ++i) {
					if (obs[j][i] > threshold) {
						++wlAmount;
					}
				}
			}
			return wlAmount;
		}
	}
	
	public static class PartitionBuilder {
		private Configuration conf;
		private PartitionInfo partInfo;
		final int PARTITION_METAINFO_SIZE = 4;
		
		public PartitionBuilder (Configuration conf) {
			this.conf = conf;
		}
		
		public void build (Path partitionFilePath) throws IOException {
			FileSystem fs = partitionFilePath.getFileSystem(conf);
			FSDataInputStream partitionFileInStream = 
				fs.open(partitionFilePath);
			LineReader in = new LineReader(partitionFileInStream, conf);
			
			Text line = new Text();
			in.readLine(line);
			String[] records = line.toString().split(" ");
			if (records.length<PARTITION_METAINFO_SIZE) {
				System.out.println("Parsing partition information file failed.");
			}
			else {
				partInfo = new PartitionInfo(Integer.parseInt(records[0]),
						Integer.parseInt(records[1]),
						Integer.parseInt(records[2]),
						Integer.parseInt(records[3]));
			}
		}
		
		public PartitionInfo getPartInfo() {
			return partInfo;
		}
		
		public int getNumOfPointX() {
			if (partInfo != null) {
				return partInfo.numOfPointX;
			}
			else return 0;
		}
		
		public int getNumOfPointY() {
			if (partInfo != null) {
				return partInfo.numOfPointY;
			}
			else return 0;
		}
	}
}
