package com.eluke;

public class BoundingBox {
	public float xmin = Float.MAX_VALUE;
	public float ymin = Float.MAX_VALUE;
	public float xmax = Float.MIN_VALUE;
	public float ymax = Float.MIN_VALUE;
	
	public BoundingBox() {
		
	}
	public BoundingBox(EndPoint e) {
		extend(e);
	}

	public void extend(BoundingBox bb) {
		xmin = Math.min(bb.xmin,xmin);
		ymin = Math.min(bb.ymin,ymin);
		xmax = Math.max(bb.xmax,xmax);
		ymax = Math.max(bb.ymax,ymax);
		
	}
	public void extend(EndPoint e) {
		xmin = Math.min(e.x,xmin);
		ymin = Math.min(e.y,ymin);
		xmax = Math.max(e.x,xmax);
		ymax = Math.max(e.y,ymax);
	}
	
	@Override
	public String toString() {
		return "[" + (int)Math.abs(xmax-xmin) + " x " + (int)Math.abs(ymax-ymin) + "]";
		
	}
	
	
}
