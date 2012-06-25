package com.eluke.graphics;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Collection;

import com.eluke.EndPoint;
import com.eluke.JoinedSection;
import com.eluke.SectionConfiguration;
import com.eluke.sections.CurvedSection;
import com.eluke.sections.Section;

public class Renderer {
	private static final int ENDPOINT_SIZE = 5;
	private static final double RAIL_WIDTH = 5;
	private static final double TIE_WIDTH = 7.5;
	private static final Color BROWN = new Color(165, 42, 42);
	private static final boolean DRAW_THIN = true;
	//private static final int SCALE = 40;

	private static class Context {
		public float translateX;
		public float translateY;
		public float scaleX;
		public float scaleY;

		public Context(float translateX, float translateY, float scaleX,
				float scaleY) {
			this.translateX = translateX;
			this.translateY = translateY;
			this.scaleX = scaleX;
			this.scaleY = scaleY;
		}

		public int endpointX(EndPoint e) {
			return (int)((e.x-translateX+5)*scaleX);
		}
		public int endpointY(EndPoint e) {
			return (int)((e.y-translateY+5)*scaleY);
		}
	}

	public void clear(RenderOutput output) throws IOException {
		BufferedImage buffer = new BufferedImage (output.getResolutionWidth(), output.getResolutionHeight(), BufferedImage.TYPE_INT_RGB);
		Graphics2D g = buffer.createGraphics ();
		g.setColor (Color.WHITE);
		g.fillRect (0, 0, output.getResolutionWidth(), output.getResolutionHeight());
		output.write(buffer);
	}

	public void draw(JoinedSection railroad, RenderOutput output, String description) throws IOException {
		// Set up the drawing surface 
		BufferedImage buffer = new BufferedImage (output.getResolutionWidth(), output.getResolutionHeight(), BufferedImage.TYPE_INT_RGB);
		Graphics2D g = buffer.createGraphics ();
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setColor (Color.WHITE);
		g.fillRect (0, 0, output.getResolutionWidth(), output.getResolutionHeight());

		// Write a note at the top
		g.setColor (Color.BLACK);
		g.drawString(description, 0, 10);

		// For each unique section endpoint, draw a dot
		g.setColor(Color.RED);
		Collection<EndPoint> endpoints = railroad.getUniqueEndpoints();
		float minX=Float.MAX_VALUE;
		float minY=Float.MAX_VALUE;
		float maxX=Float.MIN_VALUE;
		float maxY=Float.MIN_VALUE;
		for (EndPoint endpoint : endpoints) {
			minX = Math.min(minX, endpoint.x);
			minY = Math.min(minY, endpoint.y);
			maxX = Math.max(maxX, endpoint.x);
			maxY = Math.max(maxY, endpoint.y);
		}
		maxX+=10;
		maxY+=10;
		float scaleX = output.getResolutionWidth() / (maxX-minX);
		float scaleY = output.getResolutionHeight() / (maxY-minY);

		Context context = new Context(minX, minY, scaleX, scaleY);
		for (EndPoint endpoint : endpoints) {
			render(endpoint,g, context);
		}

		// Now render each section
		for (SectionConfiguration section : railroad.getSections()) {

			// YUUUCK, fix this!
			if ( section.getSection().getClass().isAssignableFrom(CurvedSection.class)) {
				render((CurvedSection)section.getSection(), section.getEndPoints(), g, context);
			}
			else {
				render(section.getSection(), section.getEndPoints(), g, context);
			}
		}

		// And finally, give it to the output tool
		output.write(buffer);
	}

	private static class Pair {
		double x, y;

		public Pair(double x, double y) {
			this.x = x;
			this.y = y;
		}

	}
	private Pair offset(float x1, float x2, float y1, float y2) {
		if ( x1 == x2 ) {
			return new Pair(1.0, 0);
		}
		if ( y1 == y2 ) {
			return new Pair(0, 1.0);
		}
		double theta = Math.atan(-(y1-y2)/(x1-x2));

		return new Pair( Math.cos(theta), Math.sin(theta) );
	}


	private void render(Section section, EndPoint[] endPoints, Graphics2D g, Context context) {
		// Default implementation draws a simple line between the endpoints in black
		g.setColor(Color.BLACK);
		for (int i = 0; i < endPoints.length-1; i++) {

			if ( DRAW_THIN ) {
				g.drawLine(context.endpointX(endPoints[i]), context.endpointY(endPoints[i]), 
						context.endpointX(endPoints[i+1]), context.endpointY(endPoints[i+1]));
			}
			else {
				float x1 = context.endpointX(endPoints[i]);
				float x2 = context.endpointX(endPoints[i+1]);
				float y1 = context.endpointY(endPoints[i]);
				float y2 = context.endpointY(endPoints[i+1]);

				Pair p1 = offset(x1,x2,y1,y2);


				g.drawLine((int)(x1+RAIL_WIDTH*p1.x), 
						(int)(y1+RAIL_WIDTH*p1.y), 
						(int)(x2+RAIL_WIDTH*p1.x), 
						(int)(y2+RAIL_WIDTH*p1.y));
				g.drawLine((int)(x1-RAIL_WIDTH*p1.x), 
						(int)(y1-RAIL_WIDTH*p1.y), 
						(int)(x2-RAIL_WIDTH*p1.x), 
						(int)(y2-RAIL_WIDTH*p1.y));

				/*
			g.setColor(BROWN);
			float totalDistance = (float) Math.sqrt( (x1-x2)*(x1-x2) + (y1-y2)*(y1-y2));
			int numIncrements = (int)totalDistance / 10;
			float dy = (y2-y1);
			float dx = (x2-x1);
			Pair p2 = offset(x2,x1,y1,y2);
			for ( int j = 0; j < numIncrements; j++ ) {
				float startX = x1+10*j*(dx/numIncrements); 
				float startY = y1+10*j*(dy/numIncrements); 


				g.drawLine((int)startX,
						(int)startY, 
						(int)((startX+TIE_WIDTH*p2.x)), 
						(int)((startY+TIE_WIDTH*p2.y)));
				g.drawLine((int)startX,
						(int)startY, 
						(int)((startX-TIE_WIDTH*p2.x)), 
						(int)((startY-TIE_WIDTH*p2.y)));

			}
				 */
			}
		}

	}

	private void render(CurvedSection section, EndPoint[] endPoints, Graphics2D g, Context context) {
		// Default implementation draws a simple line between the endpoints in black

		g.setColor(Color.BLUE);
		for (int i = 0; i < endPoints.length-1; i++) {

			if ( DRAW_THIN ) {
				g.drawLine(context.endpointX(endPoints[i]), context.endpointY(endPoints[i]), 
						context.endpointX(endPoints[i+1]), context.endpointY(endPoints[i+1]));
			}
			else {
				float x1 = context.endpointX(endPoints[i]);
				float x2 = context.endpointX(endPoints[i+1]);
				float y1 = context.endpointY(endPoints[i]);
				float y2 = context.endpointY(endPoints[i+1]);
				Pair p1 = offset(x1,x2,y1,y2);
				g.drawLine((int)(x1+RAIL_WIDTH*p1.x), 
						(int)(y1+RAIL_WIDTH*p1.y), 
						(int)(x2+RAIL_WIDTH*p1.x), 
						(int)(y2+RAIL_WIDTH*p1.y));
				g.drawLine((int)(x1-RAIL_WIDTH*p1.x), 
						(int)(y1-RAIL_WIDTH*p1.y), 
						(int)(x2-RAIL_WIDTH*p1.x), 
						(int)(y2-RAIL_WIDTH*p1.y));
			}
			/* 
			g.drawLine(context.endpointX(endPoints[i]), context.endpointY(endPoints[i]), 
					context.endpointX(endPoints[i+1]), context.endpointY(endPoints[i+1]));
			 */
		}
		/*
		 * THIS will be hard. Need to center the circle *above* the arc!
		assert(endPoints.length == 2);
		g.setColor(Color.green);
		float xmin = Math.min((float)context.endpointX(endPoints[0]), (float)context.endpointX(endPoints[1]));
		float ymin = Math.min((float)context.endpointY(endPoints[0]), (float)context.endpointY(endPoints[1]));
		float width = Math.max((float)context.endpointX(endPoints[0]), (float)context.endpointX(endPoints[1])) - xmin;
		float height = Math.max((float)context.endpointY(endPoints[0]), (float)context.endpointY(endPoints[1])) - ymin;
		Arc2D arc = new Arc2D.Float(xmin, ymin, width, height, arg4, arg5, arg6);
		g.draw(arc);
		 */
	}

	private void render(EndPoint endpoint, Graphics2D g, Context context) {
		Shape circle = new Ellipse2D.Float(
				(float)context.endpointX(endpoint)-ENDPOINT_SIZE/2, 
				(float)context.endpointY(endpoint)-ENDPOINT_SIZE/2,
				(float)ENDPOINT_SIZE, 
				(float)ENDPOINT_SIZE);
		g.fill(circle);
		//g.drawOval((int)endpoint.x*SCALE, (int)endpoint.y*SCALE, ENDPOINT_SIZE, ENDPOINT_SIZE);
		//g.drawOval(context.endpointX(endpoint), context.endpointY(endpoint), ENDPOINT_SIZE, ENDPOINT_SIZE);
	}

}
