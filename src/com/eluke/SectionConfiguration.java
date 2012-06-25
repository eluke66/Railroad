package com.eluke;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.eluke.sections.Section;
import com.eluke.sections.SectionFactory;

// TODO Eric: New thought
// A section configuration just is a list of endpoints and a pointer to a section
// A section configuration is created by each section. Since a section gives the
// configuration its endpoints when the config is created, we don't need to do transforms
//
// For example, straight section can keep 2 interior endpoints for itself (or just a length)
// But when it creates a configuration at endpoint E1, it places the appropriate gendered
// endpoint also at E1 and the correctly rotated and xlated other gendered endpoint at E2.
// Thought - do we have to put the connected endpoint into the configuration? I think not,
// as it makes otherEndPoints() easier!
//
// To generate the other endpoints that aren't being hooked up, we:
// 1. Start with the relative translation and rotation of each endpoint compared to the one in question.
//    Probably easiest to just keep a big list in each section type. There will be 2 for straight, 4 for 
//     curved, 4+ for the branching pieces.
// 2. For each of the other endpoints:
//    - Multiply the endpoint's relative translated coordinates by the rotation matrix of the incoming
//      endpoint (since this endpoint is at the origin).
//    - Translate the endpoint by the coordinates of the incoming endpoint.
//
// Note that we assume that the beginning endpoint is pointing at rotation -PI, so it hooks on to an 
// incoming piece at rotation 0.
/*  Relative for straight would be:
 * - Male at (0,0). Female at (length,0), rotation 0.
 * - Female at (0,0). Male at (length,0), rotation 0.
 * 
 * Relative for curved would be:
 * - Male at (0,0). Female at (5.25,3) [biggie curve or little?], rotation = 0.724 rad --/
 * - Male at (0,0). Female at (5.25,-3) [biggie curve or little?], rotation = -0.724 rad --\
 * - Female at (0,0). Male at (5.25,3) [biggie curve or little?], rotation = 0.724 rad --/
 * - Female at (0,0). Male at (5.25,-3) [biggie curve or little?], rotation = -0.724 rad --\
 * 
 * So, to attach a curved female to a male straight of length 3: the other male curve point is at:
 *  Straight female = [0,0]
 *  Straight male = [3,0], rotation = 0
 *  Curved female = [3,0]
 *  Curved male1 = [8.25,3], rotation = 0.724    <-------
 *  Curved male2 = [8.25,-3], rotation = -0.724  <-------
 *  
 * To attach a male straight of length 3 to a curved female, the other female points are at:
 *  Curved male = [0,0]
 *  Curved female1 = [5.25,3], rotation = 0.724
 *  Curved female2 = [5.25,-3], rotation = -0.724
 *  Straight male1 = [5.25,3], rotation = 0.724
 *  Straight male2 = [5.25,-3], rotation = -0.724
 *  Straight female1 = [5.25+3*cos(0.724),3+3*sin(0.724)]=[7.5,5], rotation = 0.724     <--------
 *  Straight female2 = [5.25+3*cos(-0.724),-3+3*sin(-0.724)], rotation = -0.724 <--------
 */

/*
 * Represents a particular configuration of a given section. 
 */
public class SectionConfiguration extends Writable {
	public static final float CONNECT_DISTANCE_TOLERANCE = 0.25f; // quarter of an inch
	private static final float CONNECT_ANGLE_TOLERANCE = 0.174f; // 10 degrees

	Section section; // Original section type
	EndPoint[] endPoints;
	private BoundingBox bounds = null;

	@Override
	public void read(DataInputStream in) throws IOException {
		int num = in.readInt();
		endPoints = new EndPoint[num];
		for ( int i = 0; i < num; i++) {
			endPoints[i] = new EndPoint();
			endPoints[i].read(in);
		}
		section = SectionFactory.fromType(in.readInt());
		bounds = null;
	}

	@Override
	public void write(DataOutputStream out) throws IOException {
		out.writeInt(endPoints.length);
		for (EndPoint e : endPoints) {
			e.write(out);
		}
		//System.out.println("SCSCSCSection " + section + " hash is " + section.toString().hashCode() );
		// ERIC WAS out.writeInt(section.toString().hashCode());
		out.writeInt(section.getType());
	}

	public BoundingBox getBounds() {
		if ( bounds == null ) {
			bounds = new BoundingBox();
			for (EndPoint e : getEndPoints()) {
				bounds.extend(e);
			}
		}
		return bounds;
	}

	public SectionConfiguration() {}


	@Override
	public String toString() {
		String tmp = "";
		for (int i = 0; i < endPoints.length - 1; i++) {
			tmp += endPoints[i] + ", ";
		}
		tmp += endPoints[endPoints.length-1];
		return section.toString() + " " + tmp;
	}

	public Section getSection() {
		return section;
	}
	public void setSection(Section section) {
		this.section = section;
	}
	public EndPoint[] getEndPoints() {
		return endPoints;
	}
	public void setEndPoints(EndPoint[] endPoints) {
		this.endPoints = endPoints;
		bounds = null;
	}
	public SectionConfiguration(Section section) {
		this.section = section;
	}
	public SectionConfiguration(Section section, EndPoint[] endPoints) {
		this.section = section;
		this.endPoints = endPoints;
	}

	/*
	 * SectionConfiguration
	 *   - crosses_section(section_configuration):
	 *     - If the current configuration crosses the existing one - unless it can connect - return true
	 *         (Optimization - compare bounding boxes. If they don't overlap, then no need to test!
	 *     - Return false (okay to add!)
	 */
	// TODO - this isn't boolean. It's NO_CROSS, CROSS, POTENTIAL_CONNECT!
	public boolean crossesSection(SectionConfiguration other) {
		Segment us = new Segment();
		Segment otherSegment = new Segment();

		for ( int i = 0; i < endPoints.length - 1; i++) {
			for ( int j = 0; j < other.endPoints.length - 1; j++) {
				us.set(endPoints[i], endPoints[i+1]);
				otherSegment.set(other.endPoints[j], other.endPoints[j+1]);
				if ( us.intersectsNoEndpoints(otherSegment)) {
					return true;
				}
			}
		}

		return false;
	}

	// Returns the list of endpoints that do *not* include the given endpoint
	public List<EndPoint> otherEndpoints(EndPoint newSectionStart) {
		List<EndPoint> others = new ArrayList<EndPoint>();
		for (EndPoint point : endPoints) {
			if ( point.x == newSectionStart.x && point.y == newSectionStart.y ) {
				// Don't add it!
			}
			else {
				others.add(point);
			}
		}
		return others;
	}

	public EndPoint connectSection(SectionConfiguration existingSection) {
		for (EndPoint point : endPoints) {
			for (EndPoint other : existingSection.endPoints) {
				//System.out.println("Trying to connect " + point + " and " + other);
				if ( canConnect(point, other)) {
					//System.out.println("\tSuccess! Returning " + point);
					return point;
				}
			}
		}
		return null;
	}

	// Determines if two points can be connected. Here we can add parameters
	// for "looseness" of connection
	private static boolean canConnect(EndPoint point, EndPoint other) {

		if ( 
				point.colocated(other, CONNECT_DISTANCE_TOLERANCE) && 
				(Math.abs(point.theta-other.theta - Math.PI) % Math.PI) <= CONNECT_ANGLE_TOLERANCE 
		) {
			return true;
		}
		return false;
	}
}
