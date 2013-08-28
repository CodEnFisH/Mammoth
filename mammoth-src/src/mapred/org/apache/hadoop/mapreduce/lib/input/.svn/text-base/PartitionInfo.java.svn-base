package org.apache.hadoop.mapreduce.lib.input;

public class PartitionInfo {
	public int numOfPointX;
	public int numOfPointY;
	public int numOfRegionX;
	public int numOfRegionY;
	public int regionLenX;
	public int regionLenY;
	
	public PartitionInfo() {
		numOfPointX = 0;
		numOfPointY = 0;
		numOfRegionX = 0;
		numOfRegionY = 0;
		regionLenX = 0;
		regionLenY = 0;
	}
	public PartitionInfo(int px, int py, int rx, int ry) {
		numOfPointX = px;
		numOfPointY = py;
		numOfRegionX = rx;
		numOfRegionY = ry;
		regionLenX = numOfPointX / numOfRegionX;
		regionLenY = numOfPointY / numOfRegionY;
	}
}
