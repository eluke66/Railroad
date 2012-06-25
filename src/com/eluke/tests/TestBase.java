package com.eluke.tests;

import junit.framework.TestCase;

import com.eluke.CompleteSectionRecorder;
import com.eluke.JoinedSection;
import com.eluke.Progressable;
import com.eluke.Railroad;

public class TestBase extends TestCase {
	protected static final double EPSILON = 1e-5;
	protected static final String TMP = "/tmp/railtest";
	
	public static class TestRecorder implements CompleteSectionRecorder {
		public int count = 0;
		
		@Override
		public void emit(JoinedSection section) {
			System.out.println(section);
			count++;
		}
		
	}
	public static class TestSaveEveryProgressable extends Progressable {

		Railroad railroad;
		long every;
		
		
		public TestSaveEveryProgressable(Railroad railroad, long every) {
			this.railroad = railroad;
			this.every = every;
		}

		@Override
		public void sectionProcessed() {
			super.sectionProcessed();
			if ( iterationSectionsProcessed > 5 && (iterationSectionsProcessed % every) == 0 ) {
				railroad.save();
				System.out.println("SAVING since sections processed for iteration " + this.currentIteration + " is " + iterationSectionsProcessed);
			}
			//else { System.out.println("Total items: " + totalItems); }
		}
		
		
		
	}
	public static class TestSaveProgressable extends Progressable {

		Railroad railroad;
		public TestSaveProgressable(Railroad railroad) {
			this.railroad = railroad;
		}
		
		// This is ugly. It exposes an edge case of saving just as we're switching to a new iteration.
		@Override
		public void finishIteration() {
			railroad.save();
			System.out.println("SAVING!");
			super.finishIteration();
		}
		
	}
	public static boolean isClose(double d1, double d2) {
		return Math.abs(d1-d2) < EPSILON;
	}
	public static void assertClose(double d1, double d2) {
		assertTrue("[expected=" + d1 + ", actual=" + d2 + "]",Math.abs(d1-d2) < EPSILON);
	}
	public final void testNothing() {}
}
