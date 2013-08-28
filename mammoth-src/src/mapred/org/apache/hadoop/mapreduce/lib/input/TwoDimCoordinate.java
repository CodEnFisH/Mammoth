package org.apache.hadoop.mapreduce.lib.input;

public class TwoDimCoordinate extends Coordinate {
	private static final int DIMENSION = 2;
    
    // Constructor
    public TwoDimCoordinate() {
        super(DIMENSION);
    }

    // Constructor
    public TwoDimCoordinate(int x, int y) {
        super(new int[DIMENSION]);
        setCoordinateAt(0, x);
        setCoordinateAt(1, y);
    }

    public int getX() {return getCoordinateAt(0);}
    public int getY() {return getCoordinateAt(1);}
    public void setX(int x) {setCoordinateAt(0, x);}
    public void setY(int y) {setCoordinateAt(1, y);}
    
    /*
    public boolean equals(Object o) {
    	if (this == o)
    		return true;
    	if (o instanceof TwoDimCoordinate) {
    		TwoDimCoordinate tdc = (TwoDimCoordinate) o;
    		return ( tdc.getX()==this.getX() && tdc.getY()==this.getY() );
    	}
    	else
    		return false;
    }
    */

	@Override
	public double getNorm() {
		return Math.hypot(getX(), getY());
	}
	@Override
	public double getDistance(Coordinate other){
		return Math.hypot(getX()-((TwoDimCoordinate)other).getX(), 
							getY()-((TwoDimCoordinate)other).getY());
	}
	@Override
	public Coordinate getDifference(Coordinate other){
		return new TwoDimCoordinate(
				getX()-((TwoDimCoordinate)other).getX(), 
				getY()-((TwoDimCoordinate)other).getY());
	}
}
