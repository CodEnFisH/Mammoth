package org.apache.hadoop.examples.domaincalc.domain;

import org.apache.hadoop.mapreduce.lib.input.*;


/* Square (2D) domain*/
public class Square extends Scope{
	TwoDimCoordinate leftUp;
	TwoDimCoordinate rightDown;
	TwoDimCoordinate currentPosition;
    int accuracy;

    /* Constructor
     * */
    public Square (TwoDimCoordinate lu, TwoDimCoordinate rd, int accuracy){
        this.accuracy = accuracy;

        // Aligned
        int minX = (int)(lu.getX()/accuracy) * accuracy;
        int minY = (int)(lu.getY()/accuracy) * accuracy;
        int maxX = (int)(rd.getX()/accuracy) * accuracy;
        int maxY = (int)(rd.getY()/accuracy) * accuracy;

        // initialize
        leftUp = new TwoDimCoordinate(minX, minY);
        rightDown = new TwoDimCoordinate(maxX, maxY);
        currentPosition = new TwoDimCoordinate(leftUp.getX(), leftUp.getY());
        
        setScopeSize();
    }

	
	/* @Override
	 * Implementation of function from father class
     * Return next point in this square
     * if out of scope, just return null
     * */
	public Coordinate nextPointInScope() {
		TwoDimCoordinate nextPt = null;
        // x+=accuracy
        if (currentPosition.getX() + accuracy < rightDown.getX()) {
            currentPosition.setX(currentPosition.getX() + accuracy);
            nextPt = new TwoDimCoordinate(currentPosition.getX(), 
                    currentPosition.getY());

        } 
        else {
            // y+=accuracy
            if (currentPosition.getY() + accuracy < rightDown.getY()) {
                currentPosition.setX(leftUp.getX());
                currentPosition.setY(currentPosition.getY() + accuracy);
                nextPt = new TwoDimCoordinate(currentPosition.getX(), 
                        currentPosition.getY());
            } 
        }
        // if out of scope, return null
        return nextPt;
	}


	@Override
	public void reset() {
		this.currentPosition.setX(leftUp.getX());
		this.currentPosition.setY(leftUp.getY());
	}
}
