package com.eluke.tests;

import com.eluke.EndPoint;
import com.eluke.Transform;

public class TestTransform extends TestBase {

	private EndPoint mkPoint() {
		return new EndPoint(1, 0, 0, false);
	}
	
	public void testAdvancedRotatePoint() {
		EndPoint point = new EndPoint(10, 0, 0, false);
		Transform.rotatePoint(point, 0.25*Math.PI);
		assertClose(Math.sqrt(50.0), point.x);
		assertClose(Math.sqrt(50.0), point.y);
		assertClose(0.25*Math.PI, point.theta);
	}
	
	public void AAAtestRotatePoint() {
		// Rotate by 0
		EndPoint point = mkPoint();
		Transform.rotatePoint(point, 0);
		assertClose(1.0, point.x);
		assertClose(0.0, point.y);
		assertClose(0.0, point.theta);
		
		
		// Rotate by 90
		point = mkPoint();
		Transform.rotatePoint(point, Math.PI / 2);
		assertClose(0.0, point.x);
		assertClose(1.0, point.y);
		assertClose(Math.PI / 2, point.theta);
		
		// Rotate by 180
		point = mkPoint();
		Transform.rotatePoint(point, Math.PI);
		assertClose(-1.0, point.x);
		assertClose(0.0, point.y);
		assertClose(Math.PI, point.theta);
		
		// Rotate by 270
		point = mkPoint();
		Transform.rotatePoint(point, 1.5*Math.PI);
		assertClose(0.0, point.x);
		assertClose(-1.0, point.y);
		assertClose(1.5*Math.PI, point.theta);
		
	}
	
	

}
