package org.apache.hadoop.mapreduce.lib.input;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.apache.hadoop.io.*;

public class CoordinateValuePair implements WritableComparable<CoordinateValuePair>  {
	private Coordinate coordinate;
    private BackgroundValueSet values;

    // Constructor
    public CoordinateValuePair(){
        coordinate = new TwoDimCoordinate();
        values = new BackgroundValueSet();
    }

    // Constructor
    public CoordinateValuePair(Coordinate coord, double[] v) {
        coordinate = coord;
        values = new BackgroundValueSet(v, v.length);
    }
    
    public CoordinateValuePair(Coordinate coord,
    						BackgroundValueSet vs) {
    	coordinate = coord;
    	values = vs;
    }
    
    public String toString() {
    	StringBuffer buf = new StringBuffer();
    	buf.append(coordinate.toString());
    	buf.append(" ");
    	buf.append(values.toString());
    	return buf.toString();
    }

    public Coordinate getCoordinate() {
        return coordinate;
    }

    public BackgroundValueSet getValues() {
        return values;
    }
    
    public int getValueSetSize() {
    	return values.getCount();
    }
    
    public void write(DataOutput out) throws IOException {
    	coordinate.write(out);
    	values.write(out);
    }
    
    public void readFields(DataInput in) throws IOException {
    	coordinate.readFields(in);
    	values.readFields(in);
    }
    
    public int compareTo(CoordinateValuePair other) {
    	if (coordinate == null || values == null)
    		return 0;
    	return coordinate.compareTo(other.getCoordinate());
    }
}
