package org.apache.hadoop.mapreduce.lib.input;

import org.apache.hadoop.io.IntWritable;

public class DomainHash {
	
	private int lowerboundX;
	private int lowerboundY;
	private int upperboundX;
	private int upperboundY;
	
	DomainHash () {}
	
	DomainHash (int lx, int ly, int ux, int uy) {
		lowerboundX = lx;
		lowerboundY = ly;
		upperboundX = ux;
		upperboundY = uy;
	}
	
	public static int generateHashCode(
			TwoDimCoordinate coord,
			PartitionInfo info) {
		int x = coord.getX();
		int y = coord.getY();
		
		// check the boundary
		if (x < 0 || x > info.numOfRegionX) {
			return -1;
		}
		
		if (y < 0 || y > info.numOfRegionY) {
			return -1;
		}
		
		
		int xInterval = info.numOfPointX/info.numOfRegionX;
		int yInterval = info.numOfPointY/info.numOfRegionY;
		int idx = x/xInterval;
		int idy = y/yInterval;
		
		return (idx+idy*info.numOfRegionY);
	}
}
