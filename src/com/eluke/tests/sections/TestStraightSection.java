/**
 * 
 */
package com.eluke.tests.sections;

import java.util.List;

import com.eluke.EndPoint;
import com.eluke.SectionConfiguration;
import com.eluke.sections.StraightSection;
import com.eluke.tests.TestBase;

/**
 * @author luke
 *
 */
public class TestStraightSection extends TestBase {

	public void testExtent() {

		StraightSection ss = new StraightSection(383.0f);
		assertEquals(383.0f, ss.getExtent());
	}
	public void testStraightSection() {
		StraightSection ss = new StraightSection(10);
		
		EndPoint e = new EndPoint(3,6,0.25*Math.PI,true);
		
		List<SectionConfiguration> configs = 
			ss.configurationsForEndpoint(e);
		
		assertEquals(1,configs.size());
		
		SectionConfiguration c = configs.get(0);
		assertEquals(2,c.getEndPoints().length);
		
		boolean foundBegin = false;
		boolean foundEnd = false;
		for (EndPoint configEnd : c.getEndPoints() ) {
			if ( isClose(configEnd.x,3.0) ) {
				// Begin
				assertEquals(false,configEnd.male);
				assertClose(6.0,configEnd.y);
				assertClose(0.25*Math.PI - Math.PI, configEnd.theta);
				foundBegin = true;
			}
			else {
				// End
				assertEquals(true,configEnd.male);
				assertClose(3.0+Math.sqrt(50.0),configEnd.x);
				assertClose(6.0+Math.sqrt(50.0),configEnd.y);
				assertClose(0.25*Math.PI, configEnd.theta);
				foundEnd = true;
			}
		}
		
		assertTrue(foundBegin);
		assertTrue(foundEnd);
	}
}
