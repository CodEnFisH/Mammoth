package org.apache.hadoop.examples.domaincalc.domain;

import org.apache.hadoop.mapreduce.lib.input.*;

public abstract class Scope {
	
	private int scopeSize;
	
	public abstract Coordinate nextPointInScope();
	public abstract void reset();
	public void setScopeSize() {
		int cnt = 0;
		Coordinate next = nextPointInScope();
		while(next != null){
			cnt++;
			next = nextPointInScope();
		}
		
		reset();
		scopeSize = cnt;
	}
	
	public int getScopeSize(){
		return scopeSize;
	}
}

