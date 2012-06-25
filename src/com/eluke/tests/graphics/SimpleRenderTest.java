package com.eluke.tests.graphics;

import java.io.IOException;

import com.eluke.Railroad;
import com.eluke.graphics.RailroadGrapher;
import com.eluke.sections.SectionFactory;
import com.eluke.tests.TestBase;

public class SimpleRenderTest extends TestBase {
	
	public void testSimpleRender() throws IOException {
		RailroadGrapher grapher = new RailroadGrapher(500,500,"simpleRenderTest");
		
		Railroad railroad = new Railroad();
		railroad.setRecorder(grapher);
		railroad.getSections().add(SectionFactory.makeSection("sc=" + 8));
		railroad.getSections().add(SectionFactory.makeSection("ss=" + 2));
		railroad.setUseIsomorph(true);
		railroad.setDirectory(TMP);
		railroad.process();
	}
}
