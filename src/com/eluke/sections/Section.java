package com.eluke.sections;

import java.util.List;

import com.eluke.EndPoint;
import com.eluke.SectionConfiguration;

public abstract class Section {

	private int type;
	
	/**
	 * Returns a list of configurations for the given endpoint. The configurations
	 * already have their transforms set.
	 * @param endPoint Endpoint we're going to be connected to.
	 * @return
	 */
	public abstract List<SectionConfiguration> configurationsForEndpoint(EndPoint endPoint);
	
	
	/**
	 * @return Maximum extent between all endpoints.
	 */
	public abstract float getExtent();
	
	public int getType() { 
		return type;
	}
	
	void setType(int type) {
		this.type = type;
	}
	
}
