package com.eluke.tests;

import java.io.IOException;

import com.eluke.Railroad;
import com.eluke.sections.SectionFactory;

public class TestSimpleComplete extends TestBase {
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
		assertEquals(2,recorder.count);
	}
}
