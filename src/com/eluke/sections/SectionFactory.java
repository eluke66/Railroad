package com.eluke.sections;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.eluke.EndPoint;
import com.eluke.SectionConfiguration;
import com.eluke.Transform;

public class SectionFactory {

	// double sided male adapter is 2"
	private static final StraightSection shortSection = new StraightSection(2);
	private static final StraightSection shortMediumSection = new StraightSection(3);
	private static final StraightSection mediumSection = new StraightSection(4);
	private static final StraightSection mediumLongSection = new StraightSection(6);
	private static final StraightSection longSection = new StraightSection(6);

	private static final CurvedSection shortCurve = new CurvedSection(3.0f,1.1875f,CurvedSection.DEFAULT_CURVE); // was 2.0 // really 1.1875?
	private static final CurvedSection longCurve = new CurvedSection(5.5f,2.25f,CurvedSection.DEFAULT_CURVE);
	private static final LeftCurvedSection shortLeftCurve = new LeftCurvedSection(3.0f,2.0f,CurvedSection.DEFAULT_CURVE);


	static Map<Integer,Section> sectionMap = null;

	private static void init() {
		sectionMap = new HashMap<Integer,Section>();
		/*sectionMap.put(shortSection.toString().hashCode(), shortSection);
		sectionMap.put(shortMediumSection.toString().hashCode(), shortMediumSection);
		sectionMap.put(mediumSection.toString().hashCode(), mediumSection);
		sectionMap.put(mediumLongSection.toString().hashCode(), mediumLongSection);
		sectionMap.put(longSection.toString().hashCode(), longSection);
		sectionMap.put(shortCurve.toString().hashCode(), shortCurve);
		sectionMap.put(longCurve.toString().hashCode(), longCurve);
		sectionMap.put(shortLeftCurve.toString().hashCode(), shortLeftCurve);*/
		int type = 0;
		sectionMap.put(type, shortSection); shortSection.setType(type++);
		sectionMap.put(type, shortMediumSection);shortMediumSection.setType(type++);
		sectionMap.put(type, mediumSection);mediumSection.setType(type++);
		sectionMap.put(type, mediumLongSection);mediumLongSection.setType(type++);
		sectionMap.put(type, longSection);longSection.setType(type++);
		sectionMap.put(type, shortCurve);shortCurve.setType(type++);
		sectionMap.put(type, longCurve);longCurve.setType(type++);
		sectionMap.put(type, shortLeftCurve);shortLeftCurve.setType(type++);
	}

	public static Section fromType(Integer hash) { 
		if ( sectionMap == null ) { 
			init();
		}
		Section s = sectionMap.get(hash);
		if ( s == null ) {
			System.out.println("Hashes:\n" + sectionMap);
			throw new IllegalArgumentException("Unknown section hash code: " + hash);
		}
		return s;
	}

	public static SectionCount makeSection(String desc) {
		if ( sectionMap == null ) { 
			init();
		}
		String[] types = desc.split("=");
		assert(types.length == 2);
		int count = Integer.parseInt(types[1]);

		if ( types[0].equals("ss") ) {
			return new SectionCount(shortSection,count);
		}
		else if ( types[0].equals("sms") ) {
			return new SectionCount(shortMediumSection,count);
		}
		else if ( types[0].equals("ms") ) {
			return new SectionCount(mediumSection,count);
		}
		else if ( types[0].equals("mls") ) {
			return new SectionCount(mediumLongSection,count);
		}
		else if ( types[0].equals("ls") ) {
			return new SectionCount(longSection,count);
		}
		else if ( types[0].equals("sc") ) {
			return new SectionCount(shortCurve,count);
		}
		else if ( types[0].equals("lc") ) {
			return new SectionCount(longCurve,count);
		}
		else if ( types[0].equals("Tlsc") ) {
			return new SectionCount(shortLeftCurve,count);
		}

		throw new IllegalArgumentException("Unknown section description " + desc);
	}

	private static class LeftCurvedSection extends CurvedSection {

		public LeftCurvedSection(float width, float height, float theta) {
			super(width,height,theta);
		}

		@Override
		public String toString() {
			return "[LeftCurved width=" + getWidth() + " height=" + height + " theta=" + theta + "]";
		}

		@Override
		public List<SectionConfiguration> configurationsForEndpoint(
				EndPoint endPoint) {
			EndPoint begin = new EndPoint(0, 0, (float)-Math.PI + endPoint.theta, !endPoint.male);
			EndPoint end1 = new EndPoint(getWidth(), height, theta, endPoint.male);

			// Step 1 - rotate our other endpoint by the incoming endPoint
			Transform.rotatePoint(end1, endPoint.theta);

			// Step 2 - translate it to the correct location
			end1.x += endPoint.x;
			end1.y += endPoint.y;
			begin.x = endPoint.x;
			begin.y = endPoint.y;


			// Return the configurations 
			List<SectionConfiguration> configs = new ArrayList<SectionConfiguration>();
			configs.add(new SectionConfiguration(this,new EndPoint[]{begin,end1}));
			return configs;
		}
	}
}
