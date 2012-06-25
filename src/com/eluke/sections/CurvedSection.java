package com.eluke.sections;

import java.util.ArrayList;
import java.util.List;

import com.eluke.EndPoint;
import com.eluke.SectionConfiguration;
import com.eluke.Transform;

/*
 * Arrangements:
 * male bottom, turning left
 * male bottom, turning right
 * female bottom, turning left
 * female bottom, turning right
 */
public class CurvedSection extends Section {
	public static float DEFAULT_CURVE = (float)Math.PI / 4.0f;// WAS 0.724f;
	
	protected float width;
	protected float height;
	protected float theta;
	
	public float getHeight() {
		return height;
	}

	public void setHeight(float height) {
		this.height = height;
	}

	public float getTheta() {
		return theta;
	}

	public void setTheta(float theta) {
		this.theta = theta;
	}

	public CurvedSection(float width, float height, float theta) {
		this.setWidth(width);
		this.height = height;
		this.theta = theta;
	}
	
	@Override
	public String toString() {
		return "[Curved width=" + getWidth() + " height=" + height + " theta=" + theta + "]";
	}

	@Override
	public List<SectionConfiguration> configurationsForEndpoint(
			EndPoint endPoint) {
		EndPoint begin = new EndPoint(0, 0, (float)-Math.PI + endPoint.theta, !endPoint.male);
		EndPoint end1 = new EndPoint(getWidth(), height, theta, endPoint.male);
		EndPoint end2 = new EndPoint(getWidth(), -height, -theta, endPoint.male);
		
		// Step 1 - rotate our other endpoint by the incoming endPoint
		Transform.rotatePoint(end1, endPoint.theta);
		Transform.rotatePoint(end2, endPoint.theta);
		
		// Step 2 - translate it to the correct location
		end1.x += endPoint.x;
		end1.y += endPoint.y;
		end2.x += endPoint.x;
		end2.y += endPoint.y;
		begin.x = endPoint.x;
		begin.y = endPoint.y;
		
		
		// Return the configurations 
		List<SectionConfiguration> configs = new ArrayList<SectionConfiguration>();
		configs.add(new SectionConfiguration(this,new EndPoint[]{begin,end1}));
		configs.add(new SectionConfiguration(this,new EndPoint[]{begin,end2}));
		return configs;
	}

	public void setWidth(float width) {
		this.width = width;
	}

	public float getWidth() {
		return width;
	}

	@Override
	public float getExtent() {
		return Math.max(width, height);
	}
}
