package com.eluke.tests.io;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;

import com.eluke.JoinedSection;
import com.eluke.Progressable;
import com.eluke.Railroad;
import com.eluke.io.Resumable;
import com.eluke.sections.SectionFactory;
import com.eluke.tests.TestBase;

public class TestResumable extends TestBase {

	Railroad railroad;
	TestRecorder recorder;
	Progressable progress;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		railroad = new Railroad();
		recorder = new TestRecorder();
		railroad.setRecorder(recorder);
		railroad.setDirectory(TMP);
		new File(TMP).delete();
	}

	public void testSimpleSave() throws IOException {
		progress = new TestSaveProgressable(railroad);
		railroad.setUseIsomorph(false);
		railroad.setOptimizerBounds(9999, 999);
		railroad.setOptimizeComplete(true);
		railroad.getSections().add(SectionFactory.makeSection("sc=" + 8));

		railroad.process(new HashSet<JoinedSection>(JoinedSection.initialize(railroad.getSections())), progress);
		assertTrue( new File(TMP + "/" + Resumable.SAVE_FILE).exists() );
		assertEquals(0,recorder.count);

		railroad.process(new HashSet<JoinedSection>(JoinedSection.initialize(railroad.getSections())), new Progressable());
		assertEquals(2,recorder.count);
	}

	public void testPause() throws IOException {
		// Ensures that a pause/resume work correctly and give us the same results as 
		// not pausing.
		progress = new TestSaveEveryProgressable(railroad,1273);
		railroad.setUseIsomorph(false);
		railroad.setOptimizerBounds(9999, 999);
		railroad.setOptimizeComplete(true);

		railroad.getSections().add(SectionFactory.makeSection("sc=" + 8));
		railroad.getSections().add(SectionFactory.makeSection("ss=" + 2));

		while (progress.getCurrentIteration() == 0 || progress.getIterationItemsWritten() > 0) {
			assertFalse( railroad.isUseIsomorph() );
			railroad.process(new HashSet<JoinedSection>(JoinedSection.initialize(railroad.getSections())), progress);
			assertFalse( railroad.isUseIsomorph() );
			assertTrue( progress.getCurrentIteration() == 10 || new File(TMP + "/" + Resumable.SAVE_FILE).exists() );
		}
		
		assertEquals(31,recorder.count); 

	}
}
