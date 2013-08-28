package org.apache.hadoop.examples.domaincalc.functions;

import java.util.Vector;
import org.apache.hadoop.mapreduce.lib.input.*;

public class FourNeighborFunction {

	private int lowerboundX;
	private int lowerboundY;
	private int upperboundX;
	private int upperboundY;
	
	public FourNeighborFunction() {
		lowerboundX = -1;
		lowerboundY = -1;
		upperboundX = -1;
		upperboundY = -1;
	}
	
	public FourNeighborFunction(int lx, int ly, int ux, int uy) {
		lowerboundX = lx;
		lowerboundY = ly;
		upperboundX = ux;
		upperboundY = uy;
	}
	
	public CoordinateValuePair [] calc(TwoDimCoordinate coord, 
			BackgroundValueSet bg, double ob) {
		int x = (int)coord.getX();
		int y = (int)coord.getY();
		TwoDimCoordinate topCoord, bottomCoord, leftCoord, rightCoord;
		BackgroundValueSet topVS, bottomVS, leftVS, rightVS;
		Vector<CoordinateValuePair> neighbors = new Vector<CoordinateValuePair>();
		if (y+1 <= upperboundY) {
			topCoord = new TwoDimCoordinate(x, y+1);
			topVS = new BackgroundValueSet(bg);
			neighbors.add(new CoordinateValuePair(topCoord, topVS));
		}
		if (y-1 >= lowerboundY) {
			bottomCoord = new TwoDimCoordinate(x, y-1);
			bottomVS = new BackgroundValueSet(bg);
			neighbors.add(new CoordinateValuePair(bottomCoord, bottomVS));
		}
		if (x+1 <= upperboundX) {
			rightCoord = new TwoDimCoordinate(x+1, y);
			rightVS = new BackgroundValueSet(bg);
			neighbors.add(new CoordinateValuePair(rightCoord, rightVS));
		}
		if (x-1 >= lowerboundX) {
			leftCoord = new TwoDimCoordinate(x-1, y);
			leftVS = new BackgroundValueSet(bg);
			neighbors.add(new CoordinateValuePair(leftCoord, leftVS));
		}
		
		CoordinateValuePair[] results = new CoordinateValuePair[neighbors.size()];
		results = neighbors.toArray(results);
		return results;
	}
}