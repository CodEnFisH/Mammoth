package org.apache.hadoop.mapreduce.lib.input;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import org.apache.hadoop.io.WritableComparable;
/*
public class BackgroundValueSet implements WritableComparable<BackgroundValueSet> {
	public double zp;
	public double u;
	public double v;
	public double w;
	public double pt;
	public double p;
	public double qv;
	public double qc;
	public double qr;
	public double qi;
	public double qs;
	public double qh;
	public double tke;
	public double kmh;
	public double kmv;
	
	static int ValueSetNum = 15;

	private int count;
	
	public BackgroundValueSet() {
		count = 0; 
		zp = u = v = w = pt = p = qv
		= qc = qr = qi = qs = qh
		= tke = kmh = kmv = 0.0;
	}
	
	public BackgroundValueSet(double ps[], int c) {
		count = c;
		zp = ps[0];
		u = ps[1];
		v = ps[2];
		w = ps[3];
		pt = ps[4];
		p = ps[5];
		qv = ps[6];
		qc = ps[7];
		qr = ps[8];
		qi = ps[9];
		qs = ps[10];
		qh = ps[11];
		tke = ps[12];
		kmh = ps[13];
		kmv = ps[14];
	}
	
	public BackgroundValueSet(BackgroundValueSet vs) {
		zp = vs.zp;
		u = vs.u;
		v = vs.v;
		w = vs.w;
		pt = vs.pt;
		p = vs.p;
		qv = vs.qv;
		qc = vs.qc;
		qr = vs.qr;
		qi = vs.qi;
		qs = vs.qs;
		qh = vs.qh;
		tke = vs.tke;
		kmh = vs.kmh;
		kmv = vs.kmv;
		count = vs.getCount();
	}
	
	public int getCount () {
		return count;
	}
	
	public void setCount(int c) {
		count = c;
	}
	
	public void write(DataOutput out) throws IOException {
    	out.writeInt(count);
    	out.writeDouble(zp);
    	out.writeDouble(u);
    	out.writeDouble(v);
    	out.writeDouble(w);
    	out.writeDouble(pt);
    	out.writeDouble(p);
    	out.writeDouble(qv);
    	out.writeDouble(qc);
    	out.writeDouble(qr);
    	out.writeDouble(qi);
    	out.writeDouble(qs);
    	out.writeDouble(qh);
    	out.writeDouble(tke);
    	out.writeDouble(kmh);
    	out.writeDouble(kmv);
    }
    
    public void readFields(DataInput in) throws IOException {
    	count = in.readInt();
    	zp = in.readDouble();
    	u = in.readDouble();
    	v = in.readDouble();
    	w = in.readDouble();
    	pt = in.readDouble();
    	p = in.readDouble();
    	qv = in.readDouble();
    	qc = in.readDouble();
    	qr = in.readDouble();
    	qi = in.readDouble();
    	qs = in.readDouble();
    	qh = in.readDouble();
    	tke = in.readDouble();
    	kmh = in.readDouble();
    	kmv = in.readDouble();
    }

	@Override
	public int compareTo(BackgroundValueSet o) {
		if (count > o.getCount())
			return 1;
		else return 0;
	}
	
	public String toString() {
		StringBuffer buf = new StringBuffer();
		buf.append((int)zp); buf.append(" ");
		buf.append((int)u); buf.append(" ");
		buf.append((int)v); buf.append(" ");
		buf.append((int)w); buf.append(" ");
		buf.append((int)pt); buf.append(" ");
		buf.append((int)p); buf.append(" ");
		buf.append((int)qv); buf.append(" ");
		buf.append((int)qc); buf.append(" ");
		buf.append((int)qr); buf.append(" ");
		buf.append((int)qi); buf.append(" ");
		buf.append((int)qs); buf.append(" ");
		buf.append((int)qh); buf.append(" ");
		buf.append((int)tke); buf.append(" ");
		buf.append((int)kmh); buf.append(" ");
		buf.append((int)kmv); buf.append(" ");
		return buf.toString();
	}
}
*/

public class BackgroundValueSet implements WritableComparable<BackgroundValueSet> {
	public double [] values;
	private int count;
	public final static int ValueSetNum = 15;
	
	public BackgroundValueSet() {
		count = 0;
		values = new double[count];
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
