package com.eluke.tests;

import java.io.IOException;

import com.eluke.Railroad;
import com.eluke.graphics.RailroadGrapher;
import com.eluke.sections.SectionFactory;

public class TestRailroad extends TestBase {

	Railroad railroad;
	TestRecorder recorder;
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		railroad = new Railroad();
		recorder = new TestRecorder();
		RailroadGrapher grapher = new RailroadGrapher(500,500,"simpleRenderTest");
		railroad.setRecorder(grapher);
		railroad.setDirectory(TMP);
	}

	public void testSimpleStraights() throws IOException {
		// Add a few simple straight pieces
		
		
		railroad.getSections().add(SectionFactory.makeSection("ss=" + 2));
		railroad.getSections().add(SectionFactory.makeSection("sms=" + 2));
		railroad.getSections().add(SectionFactory.makeSection("mls=" + 2));
		railroad.getSections().add(SectionFactory.makeSection("sc=" + 8));

		railroad.setUseIsomorph(true);
		railroad.setOptimizeComplete(true);
		railroad.process();
		
		//System.out.println("Found " + recorder.count + " valid railroads");
	}
}
