package com.eluke.tests;

import com.eluke.EndPoint;
import com.eluke.IsomorphProcessor;
import com.eluke.JoinedSection;
import com.eluke.sections.SectionCount;
import com.eluke.sections.SectionFactory;

public class TestIsomorph extends TestBase {

	public void testClear() {
		IsomorphProcessor proc = new IsomorphProcessor();
		
		assertEquals(0,proc.getIsomorphs().size());
		
		assertTrue(proc.isUnique(new JoinedSection()));
		assertEquals(1,proc.getIsomorphs().size());
		proc.clear();
		assertEquals(0,proc.getIsomorphs().size());
		
	}

	public void testIsUnique() {
		IsomorphProcessor proc = new IsomorphProcessor();
		JoinedSection section1 = null;
		JoinedSection section2 = null;
		EndPoint e1 = new EndPoint(0,1,4,false);
		EndPoint e2 = new EndPoint(0,-1,2,false);
		EndPoint e3 = new EndPoint(0,-1,-2,false);
		EndPoint e5 = new EndPoint(10,11,Math.PI,true);
		SectionCount sc1 = SectionFactory.makeSection("sc=8"); 
		SectionCount sc3 = SectionFactory.makeSection("lc=99"); 
		
		// Two completely different sections
		proc.clear();
		section1 = new JoinedSection();
		section1.getEndpoints().add(e1);
		section1.getEndpoints().add(e2);
		section1.getUnusedSections().add(sc1);
		section2 = new JoinedSection();
		section2.getEndpoints().add(e5);
		section2.getUnusedSections().add(sc3);
		
		assertTrue(proc.isUnique(section1));
		assertTrue(proc.isUnique(section2));
		assertEquals(2,proc.getIsomorphs().size());
		proc.clear();
		assertEquals(0,proc.getIsomorphs().size());
		
		// Two sections, different pieces left but same endpoints
		proc.clear();
		section1 = new JoinedSection();
		section1.getEndpoints().add(e1);
		section1.getEndpoints().add(e2);
		section1.getUnusedSections().add(sc1);
		section2 = new JoinedSection();
		section2.getEndpoints().add(e1);
		section2.getEndpoints().add(e2);
		section2.getUnusedSections().add(sc3);
		
		assertTrue(proc.isUnique(section1));
		assertTrue(proc.isUnique(section2));
		assertEquals(2,proc.getIsomorphs().size());
		proc.clear();
		assertEquals(0,proc.getIsomorphs().size());
		
		// Two sections, same pieces left but different endpoints
		proc.clear();
		section1 = new JoinedSection();
		section1.getEndpoints().add(e1);
		section1.getEndpoints().add(e2);
		section1.getUnusedSections().add(sc1);
		section2 = new JoinedSection();
		section2.getEndpoints().add(e5);
		section2.getUnusedSections().add(sc1);
		
		assertTrue(proc.isUnique(section1));
		assertTrue(proc.isUnique(section2));
		assertEquals(2,proc.getIsomorphs().size());
		proc.clear();
		assertEquals(0,proc.getIsomorphs().size());
		
		// Two sections, both the same
		proc.clear();
		section1 = new JoinedSection();
		section1.getEndpoints().add(e1);
		section1.getEndpoints().add(e2);
		section1.getUnusedSections().add(sc1);
		section2 = new JoinedSection();
		section2.getEndpoints().add(e1);
		section2.getEndpoints().add(e2);
		section2.getUnusedSections().add(sc1);
		
		assertTrue(proc.isUnique(section1));
		assertFalse(proc.isUnique(section2));
		assertEquals(1,proc.getIsomorphs().size());
		assertFalse(proc.isUnique(section1));
		assertEquals(1,proc.getIsomorphs().size());
		proc.clear();
		assertEquals(0,proc.getIsomorphs().size());
		
		// Two sections, one a reflection of the other 
		proc.clear();
		section1 = new JoinedSection();
		section1.getEndpoints().add(e1);
		section1.getEndpoints().add(e2);
		section1.getUnusedSections().add(sc1);
		section2 = new JoinedSection();
		section2.getEndpoints().add(e1);
		section2.getEndpoints().add(e3);
		section2.getUnusedSections().add(sc1);

		assertTrue(proc.isUnique(section1));
		assertFalse(proc.isUnique(section2));
		assertEquals(1,proc.getIsomorphs().size());
		assertFalse(proc.isUnique(section1));
		assertEquals(1,proc.getIsomorphs().size());
		proc.clear();
		assertEquals(0,proc.getIsomorphs().size());
	}

}
