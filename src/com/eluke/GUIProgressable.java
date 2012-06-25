package com.eluke;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import com.eluke.graphics.Renderer;
import com.eluke.graphics.UserInterface;

public class GUIProgressable extends Progressable {

	private UserInterface ui;
	private Timer t;
	
	private long currentRejections;
	private long currentCompletions;
	private long currentProcessed;
	private JoinedSection latestSection = null;

	private class GUIUpdate extends TimerTask {
		private Renderer renderer = new Renderer();
		private JoinedSection latestRendered = null;
		
		@Override
		public void run() {
			ui.updateTotals(startTimeMs, currentRejections+iterationRejections, currentCompletions+iterationCompletions,currentProcessed+iterationSectionsProcessed);
			ui.updateIteration(
					currentIteration,
					totalIterations,
					iterationStartMs,currentIterationSize,iterationSectionsProcessed,iterationItemsWritten,iterationCompletions,iterationRejections);
			ui.updateRejections(rejectionCounts);
			
			if ( latestSection == null ) { 
				try {
					renderer.clear(ui);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			else if ( latestSection != latestRendered ) {
				try {
					String description = latestSection.sections.size() + " sections, " + latestSection.numUnusedPieces() + " unused, " + latestSection.getBounds();
					renderer.draw(latestSection, ui, description);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
			latestRendered = latestSection; 
		}
		
	}
	
	public GUIProgressable(UserInterface ui, int secondsBetweenRefresh) {
		this.ui = ui;
		t = new Timer();
		GUIUpdate updater = new GUIUpdate();
		t.scheduleAtFixedRate(updater, 1000, secondsBetweenRefresh * 1000);
	}
	
	@Override
	public void finish() {
		super.finish();
		t.cancel();
		ui.updateIteration(
				currentIteration,
				totalIterations,
				iterationStartMs,currentIterationSize,iterationSectionsProcessed,iterationItemsWritten,iterationCompletions,iterationRejections);
		ui.updateRejections(rejectionCounts);
	}

	@Override
	public void finishIteration() {
		this.currentCompletions += super.iterationCompletions;
		this.currentRejections += super.iterationRejections;
		this.currentProcessed += super.iterationSectionsProcessed;
		super.finishIteration();
	}

	@Override
	public int getIterationItemsWritten() {
		// TODO Auto-generated method stub
		return super.getIterationItemsWritten();
	}

	@Override
	public void sectionCompleted(JoinedSection newJoinedSection) {
		super.sectionCompleted(newJoinedSection);
		this.latestSection = newJoinedSection;
	}

	@Override
	public void sectionWritten() {
		// TODO Auto-generated method stub
		super.sectionWritten();
	}

	@Override
	public void start(int totalIterations, long initialIterationSize) {
		// TODO Auto-generated method stub
		super.start(totalIterations, initialIterationSize);
	}

	@Override
	public void startIteration() {
		// TODO Auto-generated method stub
		super.startIteration();
	}

}
