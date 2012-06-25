package com.eluke.tests;

import java.util.ArrayList;
import java.util.List;

import com.eluke.EndPoint;
import com.eluke.JoinedSection;
import com.eluke.SectionConfiguration;
import com.eluke.JoinedSection.SectionAddition;
import com.eluke.sections.Section;
import com.eluke.sections.SectionCount;
import com.eluke.sections.StraightSection;

public class TestJoinedSection extends TestBase {

	public void testInitialize() {
		Section ss = new StraightSection(3);
		List<SectionCount> sections = new ArrayList<SectionCount>();
		sections.add(new SectionCount(ss,1));
		sections.add(new SectionCount(new StraightSection(5),1));

		List<JoinedSection> joined = JoinedSection.initialize(sections);
		assertEquals(2,joined.size());

		boolean foundFive = false;
		boolean foundThree = false;
		for (JoinedSection js : joined) {
			assertEquals(1,js.getUnusedSections().size());
			if ( js.getUnusedSections().get(0).sectionType == ss ) {
				foundFive = true;
				assertEquals(1,js.getUnusedSections().get(0).count);
			}
			else {
				foundThree = true;
				assertEquals(1,js.getUnusedSections().get(0).count);
			}
		}

		assertTrue(foundFive);
		assertTrue(foundThree);

		// Could add more testing here!
	}

	public void testConstructor() {
		Section ss = new StraightSection(3);
		List<SectionCount> sections = new ArrayList<SectionCount>();
		sections.add(new SectionCount(ss,1));
		sections.add(new SectionCount(new StraightSection(5),1));

		List<JoinedSection> joined = JoinedSection.initialize(sections);
		assertEquals(2,joined.size());
		
		JoinedSection joinLenFive = null;
		if ( joined.get(0).getUnusedSections().get(0).sectionType == ss ) {
			joinLenFive = joined.get(0);
		}
		else {
			joinLenFive = joined.get(1);
		}

		// Attaching ss to the joined section (which contains the 5" section)
		EndPoint attachPoint = joinLenFive.getEndpoints().iterator().next();
		List<SectionConfiguration> configs = ss.configurationsForEndpoint(attachPoint);
		assertEquals(1,configs.size());

		// Create a new joined section - no attaching!
		JoinedSection next = new JoinedSection(joinLenFive, attachPoint, configs.get(0), new SectionAddition(true), sections.get(0));
		
		// We should have 2 configurations in next
		assertEquals(2,next.getSections().size());
		
		// We should have 2 endpoints now - one for each configuration
		assertEquals(2,next.getEndpoints().size());
		boolean found3 = false;
		boolean found5 = false;
		// EWAS for (SectionConfiguration sc : next.getEndpointMap().values()) {
		for (SectionConfiguration sc : next.getSections()) {
			if ( sc == configs.get(0) ) {
				found3 = true;
			}
			else {
				found5 = true;
			}
		}
		assertTrue(found3);
		assertTrue(found5);
		
		// We should have 0 unused pieces
		assertEquals(0,next.getUnusedSections().size());

	}
	
	public void testCanAdd() {
		//fail("Not yet implemented");
	}

	public void testIsComplete() {
		JoinedSection joinedSection = new JoinedSection();
		
		StraightSection s1 = new StraightSection(3);
		StraightSection connector = new StraightSection(5);
		
		// 0->3
		SectionConfiguration config1 = s1.configurationsForEndpoint(new EndPoint(0,0,0,true)).get(0);
		joinedSection.getSections().add(config1);
		
		// 8-11
		SectionConfiguration config2 = s1.configurationsForEndpoint(new EndPoint(8,0,0,true)).get(0);
		joinedSection.getSections().add(config2);
		
		joinedSection.getUnusedSections().add(new SectionCount(connector,1));
		
		// Here we cheat - we just merrily assume that the 2 existing sections already connect!
		EndPoint connectPoint = new EndPoint(3,0,0,true);
		// EWAS joinedSection.getEndpointMap().put(connectPoint, config1);
		joinedSection.getEndpoints().add(connectPoint);
		EndPoint otherConnectPoint = new EndPoint(8,0,(float)-Math.PI,false);
		// EWAS joinedSection.getEndpointMap().put(otherConnectPoint, config2);
		joinedSection.getEndpoints().add(otherConnectPoint);
		
		// Add a configuration that will connect them
		SectionConfiguration connectingConfig = connector.configurationsForEndpoint(connectPoint).get(0);
		SectionAddition addition = joinedSection.canAdd(connectingConfig, connectPoint);
		assertTrue(addition.valid);
		assertNotNull(addition.connectedSection);
		assertEquals(otherConnectPoint,addition.connectedEndPoint);
		JoinedSection completed = new JoinedSection(joinedSection, connectPoint, connectingConfig, addition, joinedSection.getUnusedSections().get(0));
		
		// We should be complete!
		assertTrue(completed.isComplete());
	}

}
