package com.eluke;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class EndPoint extends Writable {
	public float x;
	public float y;
	public float theta;
	public boolean male;

	private static boolean maxAccuracy = false;

	public static void setMaxAccuracy(boolean maxAccuracy) {
		EndPoint.maxAccuracy = maxAccuracy;
	}
	public float distanceFrom(EndPoint other) {
		return (float) Math.sqrt((x-other.x)*(x-other.x)+(y-other.y)*(y-other.y));
	}
	
	
	public int absHash() {
/*
		// Old ways, looking for better 
		if ( maxAccuracy ) {
			return 
			Float.floatToIntBits(Math.abs(x)) ^ 
			Float.floatToIntBits(Math.abs(y)) ^ 
			Float.floatToIntBits(Math.abs(theta)) ^
			(male?1231:1237);
		}
		// This makes us accurate to the nearest 100th of an inch or radian
		return 
		(int)(Math.abs(100*x)) ^ 
		(int)(Math.abs(100*y)) ^ 
		Float.floatToIntBits(Math.abs(theta)) ^
		(male?1231:1237);
		// See http://www.java2s.com/Tutorial/Java/0100__Class-Definition/Gethashcodeforprimitivedatatypes.htm

*/
		int result = 17;
		if ( maxAccuracy ) {
			result = 31 * result + Float.floatToIntBits(Math.abs(x));
			result = 31 * result + Float.floatToIntBits(Math.abs(y));
			result = 31 * result + Float.floatToIntBits(Math.abs(theta));
			result = 31 * result + (male?1:0);
			return result;
		}
		else {
			result = 31 * result + (int)Math.abs(100*x);
			result = 31 * result + (int)Math.abs(100*y);
			result = 31 * result + (int)Math.abs(100*theta);
			result = 31 * result + (male?1:0);
			return result;
		}
		
	}

	@Override
	public void read(DataInputStream in) throws IOException {
		x = in.readFloat();
		y = in.readFloat();
		theta = in.readFloat();
		male = in.readBoolean();
	}

	@Override
	public void write(DataOutputStream out) throws IOException {
		out.writeFloat(x);
		out.writeFloat(y);
		out.writeFloat(theta);
		out.writeBoolean(male);

	}

	public EndPoint() {}

	public EndPoint(float x, float y, float theta, boolean male) {
		this.x = x;
		this.y = y;
		this.theta = theta;
		this.male = male;
	}

	public EndPoint(float x, float y, double theta, boolean male) {
		this.x = x;
		this.y = y;
		this.theta = (float)theta;
		this.male = male;
	}

	public boolean colocated(EndPoint other) {
		return colocated(other,1e-5f);
	}

	public boolean colocated(EndPoint other, float tolerance) {
		return (Math.abs(x-other.x) <= tolerance && Math.abs(y-other.y) <= tolerance);
	}

	@Override
	public boolean equals(Object obj) {
		EndPoint other = (EndPoint)(obj);
		return colocated(other, 1e-5f) && (Math.abs(theta-other.theta) <= 1e-5f);
	}

	@Override
	public String toString() {
		return "[x=" + x + ", y=" + y + ", theta=" + theta + ", " + (male?"M":"F") + "]";
	}



}
