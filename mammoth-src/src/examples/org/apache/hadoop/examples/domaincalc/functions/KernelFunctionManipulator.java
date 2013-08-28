package org.apache.hadoop.examples.domaincalc.functions;
////////////////////////////////////////////////////////////////////////////////
import org.apache.hadoop.examples.domaincalc.domain.*;
import org.apache.hadoop.mapreduce.lib.input.*;

public class  KernelFunctionManipulator {
	private KernelFunction kernel;
	private Scope scope;
	private Coordinate samplePointCoordinate;
	private int scopeSize;
	
	// Default Constructor
    public void KernelFuncitonManipulator() {
        kernel = null;
        scope = null;
        samplePointCoordinate = null;
        scopeSize = 0;
    }
    
    // Set the kernel function
    public void setKernelFunction(KernelFunction rhs) {
        kernel = rhs;
    }

    // Set the scope of the kernel function
    public void setScope(Scope rhs){
        scope = rhs;
        scope.setScopeSize();
        scopeSize = scope.getScopeSize();
    }

    // Set the sample point coordinate
    public void setSamplePointCoordinate(Coordinate rhs) {
    	samplePointCoordinate = rhs;
    }
    
    // The real execution of the kernel function
    public CoordinateValuePair[] runKernel() {
        if (kernel == null || scope == null || samplePointCoordinate == null) {
            throw new NullPointerException("Null Pointer in Kernel Function");
        }

        // For each coordinate in this action scope, run the kernel
        // function to get density contribution
        CoordinateValuePair[] densityList = new CoordinateValuePair[scopeSize];
        Coordinate nextPoint = scope.nextPointInScope();
        int i = 0;
        System.out.println(scopeSize);
        while (nextPoint != null) {
        	//System.out.println(i);
            //densityList[i] = new CoordinateValuePair(nextPoint,
            //        kernel.Kh(nextPoint.getDifference(samplePointCoordinate)));
        	densityList[i] = new CoordinateValuePair();
            i++;
            nextPoint = scope.nextPointInScope();
        }
        return densityList;
    }
    
    public int getScopeSize(){
    	return scopeSize;
    }
}
