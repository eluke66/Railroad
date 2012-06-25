package com.eluke.sections;

import java.util.Collections;
import java.util.List;

import com.eluke.EndPoint;
import com.eluke.SectionConfiguration;
import com.eluke.Transform;


public class StraightSection extends Section {
	float length;
	
	
	public StraightSection(float length) {
		this.length = length;
	}


	@Override
	public String toString() {
		return "[Straight Len=" + length + "]";
	}


	@Override
	public List<SectionConfiguration> configurationsForEndpoint(EndPoint endPoint) {
		EndPoint begin = new EndPoint(0, 0, (float)-Math.PI + endPoint.theta, !endPoint.male);
		EndPoint end = new EndPoint(length, 0, 0, endPoint.male);
		
		// Step 1 - rotate our other endpoint by the incoming endPoint
		Transform.rotatePoint(end, endPoint.theta);
		
		// Step 2 - translate it to the correct location
		end.x += endPoint.x;
		end.y += endPoint.y;
		begin.x = endPoint.x;
		begin.y = endPoint.y;
		
		
		// Return the configuration
		return Collections.singletonList(new SectionConfiguration(this,new EndPoint[]{begin,end}));
	}


	@Override
	public float getExtent() {
		return length;
	}

	
	
}
