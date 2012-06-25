package com.eluke;

public class Segment {
	EndPoint begin;
	EndPoint end;

	public Segment(EndPoint begin, EndPoint end) {
		this.begin = begin;
		this.end = end;
	}

	public Segment() {
	}

	public void set(EndPoint begin, EndPoint end) {
		this.begin = begin;
		this.end = end;
	}

	
	public boolean intersects(Segment other) {
		// See:
		// http://local.wasp.uwa.edu.au/~pbourke/geometry/lineline2d/
		
		double denom = ((other.end.y - other.begin.y)*(end.x - begin.x)) -
		((other.end.x - other.begin.x)*(end.y - begin.y));

		if ( denom == 0.0 ) { 
			return false;
			
		}
		
		double numea = ((other.end.x - other.begin.x)*(begin.y - other.begin.y)) -
		((other.end.y - other.begin.y)*(begin.x - other.begin.x));

		double u = numea / denom;
		if ( u < 0.0 || u > 1.0 ) {
			return false;
		}
		
		double numeb = ((end.x - begin.x)*(begin.y - other.begin.y)) -
		((end.y - begin.y)*(begin.x - other.begin.x));
		
		u = numeb / denom;
		return ( u >= 0.0 && u <= 1.0 );
	}
	
	// Same as intersects, but counts a hit at an endpoint as not an intersection
	public boolean intersectsNoEndpoints(Segment other) {
		// See:
		// http://local.wasp.uwa.edu.au/~pbourke/geometry/lineline2d/
		
		double denom = ((other.end.y - other.begin.y)*(end.x - begin.x)) -
		((other.end.x - other.begin.x)*(end.y - begin.y));

		if ( denom == 0.0 ) { 
			return false;
			
		}
		
		double numea = ((other.end.x - other.begin.x)*(begin.y - other.begin.y)) -
		((other.end.y - other.begin.y)*(begin.x - other.begin.x));

		double u = numea / denom;
		if ( u <= 0.0 || u >= 1.0 ) {
			return false;
		}
		
		double numeb = ((end.x - begin.x)*(begin.y - other.begin.y)) -
		((end.y - begin.y)*(begin.x - other.begin.x));
		
		u = numeb / denom;
		return ( u > 0.0 && u < 1.0 );
	}
}
