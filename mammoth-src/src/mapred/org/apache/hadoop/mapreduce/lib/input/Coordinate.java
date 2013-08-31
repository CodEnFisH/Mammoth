package org.apache.hadoop.mapreduce.lib.input;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.apache.hadoop.io.*;
////////////////////////////////////////////////////////////////////////////////

public abstract class Coordinate implements WritableComparable<Coordinate> {

    private static final int[] EMPTY_COORD = {};	
    private int[] coordValues;
    private int size;

    // Constructor
    public Coordinate() {
        this(EMPTY_COORD);
    }
    // Constructor
    public Coordinate(int[] rhs) {
        coordValues = rhs;
        size = rhs.length;
    }

    // Constructor
    public Coordinate(int dimensionNum) {
        coordValues = new int[dimensionNum];
        size = dimensionNum;
    }

    // Set coordinate at the dimension i
    // 0 <= i < size
    public void setCoordinateAt(int i, int value) {
        if (i < size && i >= 0) {
            coordValues[i] = value;
        } 
        else{
            throw new IllegalArgumentException("dimension out of range");
        }
    }

    // Set all coordinate
    public void setAllCoordinates(int[] rhs) {
        if (rhs.length == size) {
            for (int i = 0; i < rhs.length; i++) {
                coordValues[i] = rhs[i];
            }
        }
        else {
            throw new IllegalArgumentException("dimension not the same");
        }
    }

    // Get coordinates at dimension i
    public int getCoordinateAt(int i) {
        if (i < size) {
            return coordValues[i];
        }
        else {
            throw new IllegalArgumentException("dimension out of range");
        }
    }

    // Implementation of Writable interface
    public void write(DataOutput out) throws IOException {
        out.writeInt(size);                     // write the size
        for (int i = 0; i < size; i++) {
            out.writeInt(coordValues[i]);    // write coordinates
        }
    }
    
    public void readFields(DataInput in) throws IOException {
        coordValues = new int[size = in.readInt()];      // read the size
        for (int i = 0; i < size; i++) {
            coordValues[i] = in.readInt();
        } 
    }
   
    public int compareTo(Coordinate other){
    	if(this.getDimensionNum() < other.getDimensionNum()) {
    		return -1;
    	} else if (this.getDimensionNum() > other.getDimensionNum()) {
    		return 1;
    	} else {
    		for(int i = 0; i < getDimensionNum(); i++){
    			if (this.getCoordinateAt(i) < other.getCoordinateAt(i)){
    				return -1;
    			} else if (this.getCoordinateAt(i) > other.getCoordinateAt(i)){
    				return 1;
    			}
    		}
    		return 0;
    	}
    }
    
    public boolean equals(Object o) {
    	Coordinate otherCoord = (Coordinate) o;
    	if (this.hashCode() == otherCoord.hashCode())
    		return true;
    	else return false;
    }
    
    public int hashCode() {
    	if (size == 0){
    		return -1;
    	}
    	else {
    		return coordValues[0]*10000+coordValues[1];
    	}
    }
    
    public int getDimensionNum(){return coordValues.length;}
    public String toString(){
    	StringBuffer ret = new StringBuffer();
    	for(int i = 0; i < getDimensionNum(); i++){
    		ret.append(coordValues[i]);
    		if(i < getDimensionNum() - 1)
    				ret.append(" ");
    	}
    	return ret.toString();
    }
    
    public abstract double getNorm();
    public abstract double getDistance(Coordinate other);
    public abstract Coordinate getDifference(Coordinate other);
    //hashCode()
}

