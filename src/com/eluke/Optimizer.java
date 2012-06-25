package com.eluke;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import com.eluke.sections.SectionCount;

public class Optimizer extends Writable {

	private float maxSmallSide = Float.MAX_VALUE;
	private float maxLargeSide = Float.MAX_VALUE;
	private boolean optimizeBounds = false;
	private boolean optimizeCanComplete = false;
	

	@Override
	public void read(DataInputStream in) throws IOException {
		maxSmallSide = in.readFloat();
		maxLargeSide = in.readFloat();
		optimizeBounds = in.readBoolean();
		optimizeCanComplete = in.readBoolean();
	}

	@Override
	public void write(DataOutputStream out) throws IOException {
		out.writeFloat(maxSmallSide);
		out.writeFloat(maxLargeSide);
		out.writeBoolean(optimizeBounds);
		out.writeBoolean(optimizeCanComplete);
	}
	
	public Progressable.RejectionType optimize(JoinedSection section) {
		if ( optimizeBounds ) {
			BoundingBox bbox = section.getBounds();
			float xExtent = bbox.xmax-bbox.xmin;
			float yExtent = bbox.ymax-bbox.ymin;
			
			if (xExtent < yExtent) {
				if ( xExtent > maxSmallSide ) { 
					return Progressable.RejectionType.BOUNDING_BOX;
				}
				if ( yExtent > maxLargeSide ) { 
					return Progressable.RejectionType.BOUNDING_BOX;
				}
			}
			else {
				if ( yExtent > maxSmallSide ) { 
					return Progressable.RejectionType.BOUNDING_BOX;
				}
				if ( xExtent > maxLargeSide ) { 
					return Progressable.RejectionType.BOUNDING_BOX;
				}
			}
		}
		
		// In this case, we have to have enough track to be 
		// able to connect all the endpoints.
		if ( optimizeCanComplete ) {
			float trackAvailable = 0;
			for (SectionCount count : section.unusedSections) {
				trackAvailable += count.count * (count.sectionType.getExtent()+SectionConfiguration.CONNECT_DISTANCE_TOLERANCE);
			}
			
			// For now we do the safest thing, and ensure we have enough for
			// the largest single gap
			float maxEndpointGap = 0;
			for (int i = 0; i < section.getEndpoints().size()-1; i++) {
				float distance = section.getEndpoints().get(i).distanceFrom(section.getEndpoints().get(i+1));
				maxEndpointGap = Math.max(maxEndpointGap, distance);
			}
			
			if (trackAvailable < maxEndpointGap) {
				//System.out.println("Endpoint gap is " + maxEndpointGap + ", available is " + trackAvailable);
				return Progressable.RejectionType.UNCOMPLETABLE;
			}
		}
		
		return Progressable.RejectionType.NO_REJECTION;
	}
	
	public Optimizer() {
		
	}
	public Optimizer(float maxWidth, float maxHeight) {
		this.maxSmallSide = Math.min(maxWidth, maxHeight);
		this.maxLargeSide = Math.max(maxWidth, maxHeight);
		optimizeBounds = true;
	}
	
	public boolean isOptimizeBounds() {
		return optimizeBounds;
	}
	public void setOptimizeBounds(boolean optimizeBounds) {
		this.optimizeBounds = optimizeBounds;
	}

	public void setOptimizeCanComplete(boolean optimizeCanComplete) {
		this.optimizeCanComplete = optimizeCanComplete;
	}

	public boolean isOptimizeCanComplete() {
		return optimizeCanComplete;
	}

}
