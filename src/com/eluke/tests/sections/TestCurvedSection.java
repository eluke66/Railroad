package com.eluke.tests.sections;

import java.util.List;

import com.eluke.EndPoint;
import com.eluke.SectionConfiguration;
import com.eluke.sections.CurvedSection;
import com.eluke.sections.SectionFactory;
import com.eluke.tests.TestBase;

public class TestCurvedSection extends TestBase {

	public void testExtent() {
		CurvedSection cs = (CurvedSection)(SectionFactory.makeSection("sc=1").sectionType);
		
		
		assertEquals(3.0f, cs.getExtent());
	}
	
	public void testCurved() {
		CurvedSection cs = (CurvedSection)(SectionFactory.makeSection("sc=1").sectionType);

		// Add another curved section to the end
		EndPoint e = new EndPoint(cs.getWidth(),cs.getHeight(),cs.getTheta(),true);

		List<SectionConfiguration> configs = 
			cs.configurationsForEndpoint(e);

		assertEquals(2,configs.size());

		// Ensure that we have 2 next endpoints
		boolean foundVertical = false;
		boolean foundHorizontal = false;
		for ( SectionConfiguration config : configs ) {
			assertEquals(1,config.otherEndpoints(e).size());
			System.out.println("Theta is " + config.otherEndpoints(e).get(0).theta);
			if ( config.otherEndpoints(e).get(0).theta == 2*CurvedSection.DEFAULT_CURVE ) {
				foundVertical = true;
			}
			else if ( config.otherEndpoints(e).get(0).theta == 0.0f) {
				foundHorizontal = true;
			}
			else {
				assertFalse("Unknown theta of " +  config.otherEndpoints(e).get(0).theta, true);
			}
		}
		assertTrue(foundVertical);
		assertTrue(foundHorizontal);
	}
}
