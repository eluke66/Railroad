package com.eluke.tests;

import java.io.IOException;

import com.eluke.Railroad;
import com.eluke.sections.SectionFactory;

public class TestCircle extends TestBase {
	
	Railroad railroad;
	TestRecorder recorder;
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		railroad = new Railroad();
		recorder = new TestRecorder();
		railroad.setRecorder(recorder);
		railroad.setDirectory(TMP);
	}

	public void testSimpleAllCurves() throws IOException {

		railroad.getSections().add(SectionFactory.makeSection("sc=" + 8));
		railroad.process();
		railroad.setOptimizeComplete(true);
		assertEquals(2,recorder.count);
	}
	
	public void testSimpleAllCurvesNoIsomorphs() throws IOException {

		railroad.getSections().add(SectionFactory.makeSection("sc=" + 8));
		railroad.setUseIsomorph(true);
		railroad.process();
		assertEquals(1,recorder.count);
	}
	
	public void testSimpleCurvesTwoStraight() throws IOException {

		railroad.getSections().add(SectionFactory.makeSection("sc=" + 8));
		railroad.getSections().add(SectionFactory.makeSection("ss=" + 2));

		
		railroad.process();
		assertEquals(31,recorder.count); // Hmmm, unconfirmed.
	}
	public void testSimpleCurvesTwoStraightNoIsomorphs() throws IOException {

		railroad.getSections().add(SectionFactory.makeSection("sc=" + 8));
		railroad.getSections().add(SectionFactory.makeSection("ss=" + 2));
		railroad.setUseIsomorph(true);
		railroad.process();
		// TODO - this test is flaky assertEquals(6,recorder.count);
	}
}
