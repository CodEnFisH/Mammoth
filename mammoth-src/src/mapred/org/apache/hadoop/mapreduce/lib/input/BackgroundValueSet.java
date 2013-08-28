package org.apache.hadoop.mapreduce.lib.input;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import org.apache.hadoop.io.WritableComparable;

public class BackgroundValueSet implements WritableComparable<BackgroundValueSet> {
	public double [] values;
	private int count;
	public final static int ValueSetNum = 15;
	
	public BackgroundValueSet() {
		count = 0;
		values = new double[count];
	}
	
	public BackgroundValueSet(int c) {
		count = c;
		values = new double[count];
		for (int i=0; i<count; ++i) {
			values[i] = 0.0;
		}
	}
	
	public BackgroundValueSet(double ps[]) {
		count = ps.length;
		values = new double[count];
		for (int i=0; i<count; ++i) {
			values[i] = ps[i];
		}
	}
	
	public BackgroundValueSet(double ps[], int c) {
		count = c;
		values = new double[count];
		for (int i=0; i<count; ++i) {
			values[i] = ps[i];
		}
	}
	
	public BackgroundValueSet(BackgroundValueSet vs) {
		count = vs.getCount();
		values = new double[count];
		double [] otherValue = vs.getValues();
		for (int i=0; i<count; ++i) {
			values[i] = otherValue[i];
		}
	}
	
	public static void accumulate(
			double[] prev, BackgroundValueSet otherValueSet) {
		if (prev.length != otherValueSet.getCount())
			return;
		for (int i=0; i<otherValueSet.getCount(); ++i) {
			prev[i] += otherValueSet.getValue(i);
		}
	}
	
	public void aggregate(BackgroundValueSet otherValueSet) {
		for (int i=0; i<otherValueSet.getCount(); ++i) {
			values[i] += otherValueSet.getValue(i);
		}
	}
	
	public int getCount () {
		return count;
	}
	
	public double[] getValues() {
		return values;
	}
	
	public double getValue(int idx) {
		if (idx < count || idx > 0) {
			return values[idx];
		}
		else return 0.0;
	}
	
	public void setCount(int c) {
		count = c;
	}
	
	public void write(DataOutput out) throws IOException {
    	out.writeInt(count);
    	for (int i=0; i<count; ++i) {
    		out.writeDouble(values[i]);
    	}
    }
    
    public void readFields(DataInput in) throws IOException {
    	count = in.readInt();
    	values = new double[count];
    	for (int i=0; i<count; ++i) {
    		values[i] = in.readDouble();
    	}
    }

	@Override
	public int compareTo(BackgroundValueSet o) {
		if (count > o.getCount())
			return 1;
		else return 0;
	}
	
	public String toString() {
		StringBuffer buf = new StringBuffer();
		for (int i=0; i<count; ++i) {
			buf.append((int)values[i]);
			buf.append(" ");
		}
		return buf.toString();
	}
}
