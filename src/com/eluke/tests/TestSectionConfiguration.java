package com.eluke.tests;

import com.eluke.EndPoint;
import com.eluke.SectionConfiguration;

public class TestSectionConfiguration extends TestBase {

	public void testCrossesSection() {
		EndPoint[] endPoints = 
			new EndPoint[]{new EndPoint(0,0,-Math.PI,true),new EndPoint(10,0,0,false)};
		                                    
		SectionConfiguration config = new SectionConfiguration(null,endPoints);
		
		// Not even close (out of bbox)
		{
			SectionConfiguration incomingConfig = 
				new SectionConfiguration(null,
						new EndPoint[]{new EndPoint(100,100,-Math.PI,true),new EndPoint(110,100,0,false)});
			assertFalse(config.crossesSection(incomingConfig));
		}
		// Easy cross
		{
			SectionConfiguration incomingConfig = 
				new SectionConfiguration(null,
						new EndPoint[]{new EndPoint(5,-5,-Math.PI,true),new EndPoint(5,5,0,false)});
			assertTrue(config.crossesSection(incomingConfig));
		}
		
		// Would cross if lines (not segments)
		{
			SectionConfiguration incomingConfig = 
				new SectionConfiguration(null,
						new EndPoint[]{new EndPoint(5,-5,-Math.PI,true),new EndPoint(5,-1,0,false)});
			assertFalse(config.crossesSection(incomingConfig));
		}
		
		// Connect (share an endpoint) - doesn't cross!
		{
			SectionConfiguration incomingConfig = 
				new SectionConfiguration(null,
						new EndPoint[]{new EndPoint(10,-5,-Math.PI,true),new EndPoint(10,0,0,false)});
			assertFalse(config.crossesSection(incomingConfig));
		}
		
		// Simple no cross test for multiple segments
		{
			SectionConfiguration incomingConfig = 
				new SectionConfiguration(null,
						new EndPoint[]{
						new EndPoint(100,100,-Math.PI,true),
						new EndPoint(110,100,0,false),
						new EndPoint(110,150,-Math.PI,true)});
			assertFalse(config.crossesSection(incomingConfig));
		}
		
		// Simple cross test for multiple segments
		{
			SectionConfiguration incomingConfig = 
				new SectionConfiguration(null,
						new EndPoint[]{
						new EndPoint(0,-5,-Math.PI,true),
						new EndPoint(5,-5,-Math.PI,true),
						new EndPoint(5,5,0,false)});
			assertTrue(config.crossesSection(incomingConfig));
		}
	}

	public void testOtherEndpoints() {
		EndPoint[] endPoints = 
			new EndPoint[]{new EndPoint(0,0,-Math.PI,true),new EndPoint(10,0,0,false)};
		                                    
		SectionConfiguration config = new SectionConfiguration(null,endPoints);
		assertEquals(1,config.otherEndpoints(endPoints[0]).size());
		assertTrue(endPoints[1].colocated(config.otherEndpoints(endPoints[0]).get(0)));
		assertTrue(endPoints[0].colocated(config.otherEndpoints(endPoints[1]).get(0)));
	}

	public void testConnectSection() {

		EndPoint[] endPoints = 
			new EndPoint[]{new EndPoint(0,0,-Math.PI,true),new EndPoint(10,0,0,false)};
		                                    
		SectionConfiguration config = new SectionConfiguration(null,endPoints);
		
		// No connect
		{
			SectionConfiguration incomingConfig = 
				new SectionConfiguration(null,
						new EndPoint[]{new EndPoint(100,100,-Math.PI,true),new EndPoint(110,100,0,false)});
			assertNull(config.connectSection(incomingConfig));
		}
		
		// Easy connect
		{
			SectionConfiguration incomingConfig = 
				new SectionConfiguration(null,
						new EndPoint[]{new EndPoint(10,0,-Math.PI,true),new EndPoint(110,0,0,false)});
			EndPoint result = config.connectSection(incomingConfig);
			assertNotNull(result);
			assertTrue(result.colocated(new EndPoint(10,0,-Math.PI,true)));
		}

		// No connect, since points are same but angle is not right
		{
			SectionConfiguration incomingConfig = 
				new SectionConfiguration(null,
						new EndPoint[]{new EndPoint(10,0,-Math.PI/2,true),new EndPoint(110,0,0,false)});
			assertNull(config.connectSection(incomingConfig));
		}
		
		
		// Loose connect
		{
			SectionConfiguration incomingConfig = 
				new SectionConfiguration(null,
						new EndPoint[]{new EndPoint(10.2f,0,-Math.PI-0.1,true),new EndPoint(110,0,0,false)});
			EndPoint result = config.connectSection(incomingConfig);
			assertNotNull(result);
			assertTrue(result.colocated(new EndPoint(10,0,-Math.PI,true)));
		}
	}

}
