package org.apache.hadoop.examples.domaincalc.functions;

import java.util.Vector;
import org.apache.hadoop.mapreduce.lib.input.*;

/*
 * PLEASE REPLACE THE FOLLOWING EXAMPLE CODE WITH YOUR OWN LOGICS.
 */

/*
 * The following example code implements Double Exponential Smoothing,
 * which applies to time series data of predicting the current state
 * using historical states and current observation.
 * 
 * Please refer http://en.wikipedia.org/wiki/Exponential_smoothing for details
 */
public class MammothAPI {
	/*
	 * The trigger method
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
		 * The "coord" pinpoints the position of the point;
		 * the "bg" is the SMOOTHED VALUE of the last iteration;
		 * the "ob" is the RAW DATA of the current iteration.
		 * 
		 * The estimate value at every iteration is saved
		 * in association with the smoothed value. Both of them serve
		 * the next iteration.
		 * 
		 * For the first iteration, the SMOOTHED value equal the RAW data,
		 * and the ESTIMATE is 0.
		 */
		
		double alpha = 0.5, beta = 0.5;
		double prevSmoothed, prevEstimate;
		double cntSmoothed, cntEstimate;
		
		if (bg.getCount() == 1) {
			// The first iteration
			prevSmoothed = bg.getValues()[0];
			prevEstimate = 0;
		}
		else {
			/*
			 * As the Double Exponential Smoothing method only leverages
			 * the data of states from the last iteration and the current
			 * iteration, there will be sufficient information since the 
			 * second the iteration.
			 */
			prevSmoothed = bg.getValues()[0];
			prevEstimate = bg.getValues()[1];
		}
		
		cntSmoothed = alpha*ob + (1-alpha)*(prevSmoothed+prevEstimate);
		cntEstimate = beta*(cntSmoothed-prevSmoothed) + (1-beta)*prevEstimate;
		
		double [] resultsInArray = new double[2];
		resultsInArray[0] = cntSmoothed;
		resultsInArray[1] = cntEstimate;		
		CoordinateValuePair [] results = new CoordinateValuePair[1];
		results[0] = new CoordinateValuePair(coord, 
				new BackgroundValueSet(resultsInArray));
		
		return results;
	}
	
	/*
	 * The aggregate method
	 */
	public CoordinateValuePair Aggregate(
			CoordinateValuePair[] valuesAtPoint) {
		
		return valuesAtPoint[0];
	}
}
