package com.eluke.tests.io;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.eluke.EndPoint;
import com.eluke.JoinedSection;
import com.eluke.SectionConfiguration;
import com.eluke.JoinedSection.SectionAddition;
import com.eluke.io.IOUtils;
import com.eluke.io.InputReader;
import com.eluke.io.OutputWriter;
import com.eluke.sections.Section;
import com.eluke.sections.SectionCount;
import com.eluke.sections.StraightSection;
import com.eluke.tests.TestBase;

public class TestOutputWriter extends TestBase {
	
	private JoinedSection makeSection() {
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
		return next;
		
	}
	public void testSimpleMultiFiles() throws FileNotFoundException, IOException {
		TestRecorder recorder = new TestRecorder();
		OutputWriter writer = new OutputWriter(recorder, TMP);
		IOUtils.ensureDirectory(TMP);
		
		writer.setOutputsPerFile(1);
		
		// This should make 9 files - 1 permanent, 8 temp
		writer.start(0, 5, false);
		assertEquals(9, new File(TMP).list().length);
		
		// Now, we write 10 sections, with incrementing hashes.
		JoinedSection section = makeSection();
		for ( int i = 0; i < 10; i++) {
			writer.writeNext(new Integer(i), section);
		}
		
		// Finish it up
		writer.finish();
		
		// Now, the final output file should contain 10 sections
		InputReader reader = new InputReader(TMP, 1);
		reader.start(0, 100);
		assertEquals(10, reader.getSections().getCount());
	}
	
	public void testSimpleMultiFilesCullHashes() throws FileNotFoundException, IOException {
		TestRecorder recorder = new TestRecorder();
		OutputWriter writer = new OutputWriter(recorder, TMP);
		IOUtils.ensureDirectory(TMP);
		
		writer.setOutputsPerFile(1);
		
		// This should make 9 files - 1 permanent, 8 temp
		writer.start(0, 5, false);
		assertEquals(9, new File(TMP).list().length);
		
		// Now, we write 20 sections, with duplicate incrementing hashes.
		JoinedSection section = makeSection();
		for ( int i = 0; i < 20; i++) {
			writer.writeNext(new Integer(i% 10), section);
		}
		
		// Finish it up
		writer.finish();
		
		// Now, the final output file should contain 10 sections
		InputReader reader = new InputReader(TMP, 1);
		reader.start(0, 100);
		assertEquals(10, reader.getSections().getCount());
	}
}
