package org.apache.hadoop.mapreduce.lib.input;

import org.apache.hadoop.io.IntWritable;

public class DomainHash {
	DomainHash () {}
	
	public static int generateHashCode(
			TwoDimCoordinate coord,
			PartitionInfo info) {
		int x = coord.getX();
		int y = coord.getY();
		int xInterval = info.numOfPointX/info.numOfRegionX;
		int yInterval = info.numOfPointY/info.numOfRegionY;
		int idx = x/xInterval;
		int idy = y/yInterval;
		
		return (idx+idy*info.numOfRegionY);
	}
}
