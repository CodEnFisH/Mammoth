package org.apache.hadoop.examples.domaincalc.functions;

import org.apache.hadoop.mapreduce.lib.input.*;

public class TwoDimensionUniformedKernel extends KernelFunction{

	private final double unitDensity;
	public TwoDimensionUniformedKernel(double scopeSize){
		unitDensity = 1/scopeSize;
	}
	@Override
	public double Kh(Coordinate distance) {
		return unitDensity;
	}
}
