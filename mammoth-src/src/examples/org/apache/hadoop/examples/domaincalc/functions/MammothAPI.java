package org.apache.hadoop.examples.domaincalc.functions;

import java.util.Vector;
import org.apache.hadoop.mapreduce.lib.input.*;

public class MammothAPI {	
	/*
	 * The trigger method
	 * 
	 * REPLACE THE FOLLOWING EXAMPLE CODE WITH YOUR OWN LOGICS.
	 * 
	 * The parameters include
	 * "coord" - The coordinate of the point to be computed
	 * "bg" - The data of the state at "coord"
	 * "ob" - The data of the observation at "coord"
	 */
	public CoordinateValuePair[] Trigger(
			TwoDimCoordinate coord, 
			BackgroundValueSet bg,
			double ob) {
		/*
		 * The following example code use the observation to update
		 * the states of a certain point and its neighbors.
		 * The neighbors include the points on the top, bottom, left and right.
		 */
		int x = (int)coord.getX();
		int y = (int)coord.getY();
		TwoDimCoordinate topCoord, bottomCoord, leftCoord, rightCoord;
		BackgroundValueSet topVS, bottomVS, leftVS, rightVS;
		Vector<CoordinateValuePair> neighbors = new Vector<CoordinateValuePair>();
		
		topCoord = new TwoDimCoordinate(x, y+1);
		topVS = new BackgroundValueSet(bg);
		neighbors.add(new CoordinateValuePair(topCoord, topVS));
		
		bottomCoord = new TwoDimCoordinate(x, y-1);
		bottomVS = new BackgroundValueSet(bg);
		neighbors.add(new CoordinateValuePair(bottomCoord, bottomVS));
		
		rightCoord = new TwoDimCoordinate(x+1, y);
		rightVS = new BackgroundValueSet(bg);
		neighbors.add(new CoordinateValuePair(rightCoord, rightVS));
		
		leftCoord = new TwoDimCoordinate(x-1, y);
		leftVS = new BackgroundValueSet(bg);
		neighbors.add(new CoordinateValuePair(leftCoord, leftVS));
		
		CoordinateValuePair[] results = new CoordinateValuePair[neighbors.size()];
		results = neighbors.toArray(results);
		return results;
	}
	
	/*
	 * The aggregate method
	 * 
	 * REPLACE THE FOLLOWING CODE WITH YOUR OWN LOGICS
	 */
	public CoordinateValuePair Aggregate(
			CoordinateValuePair[] valuesAtPoint) {
		int size = valuesAtPoint[0].getValues().getCount();
		double[] values = new double[size];
		for (int i=0; i<size; ++i) {
			values[i] = 0.0;
		}
		for (int i=0; i<valuesAtPoint.length; ++i) {
			BackgroundValueSet.accumulate(values, valuesAtPoint[i].getValues());
		}
		for(int i=0; i<size; ++i) {
			values[i] = values[i]/valuesAtPoint.length;
		}
		
		CoordinateValuePair result = new CoordinateValuePair(
				valuesAtPoint[0].getCoordinate(), values);
		return result;
	}
}
