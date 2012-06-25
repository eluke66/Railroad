package com.eluke.graphics;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.border.BevelBorder;

import com.eluke.Progressable.RejectionType;

public class UserInterface implements RenderOutput {

	private JLabel currentIteration = new JLabel("Current Iteration");
	private JTextField iterationProcessed = makeText();
	private JTextField iterationCompleted = makeText();
	private JTextField iterationWritten = makeText();
	private JTextField iterationRejected = makeText();
	private JTextField iterationStartTime = makeText();
	private JTextField iterationElapsedTime = makeText();
	private JTextField iterationSpeed = makeText();
	private JTextField iterationTimeToCompletion = makeText();
	private JProgressBar iterationProgress = new JProgressBar(0,100);


	private JTextField totalProcessed = makeText();
	private JTextField totalCompleted = makeText();
	private JTextField totalRejected = makeText();
	private JTextField totalStartTime = makeText();
	private JTextField totalElapsedTime = makeText();
	private JTextField totalSpeed = makeText();
	private JProgressBar totalProgress = new JProgressBar(0,100);
	private JPanel graphicPanel;
	private JLabel image = new JLabel();

	private JTextField makeText() {
		JTextField text = new JTextField("0",15);
		text.setEditable(false);
		text.setHorizontalAlignment(JTextField.RIGHT);
		text.setBackground(Color.WHITE);

		return text;
	}

	public void updateTotals(long startTimeMs, long rejections, long completions, long processed) {

		totalStartTime.setText(new Date(startTimeMs).toString());
		totalRejected.setText(Long.toString(rejections));
		totalCompleted.setText(Long.toString(completions));
		totalProcessed.setText(Long.toString(processed));
		long elapsedMs = System.currentTimeMillis() - startTimeMs;
		totalElapsedTime.setText(Long.toString(elapsedMs/1000) + " seconds");

		long sectionsPerSecond = (1000*processed/elapsedMs);
		totalSpeed.setText(sectionsPerSecond + " sections/second");
	}

	public void updateIteration(
			int iteration,
			int maxIterations,
			long iterationStartMs, 
			long iterationTodoTotal,
			long iterationProcessedItems,
			int iterationItemsWritten,
			int iterationCompletions, 
			long iterationRejections) {

		totalProgress.setMaximum(maxIterations);
		totalProgress.setValue(iteration);


		currentIteration.setText("Current Iteration: " + iteration + "/" + maxIterations);
		iterationStartTime.setText(new Date(iterationStartMs).toString());
		iterationCompleted.setText(Integer.toString(iterationCompletions));
		iterationWritten.setText(Integer.toString(iterationItemsWritten));
		iterationRejected.setText(Long.toString(iterationRejections));
		long pct = 100;
		if ( iterationTodoTotal != 0 ) {
			pct = 100*iterationProcessedItems/iterationTodoTotal;
			iterationProgress.setMaximum((int)iterationTodoTotal);
			iterationProgress.setValue((int)iterationProcessedItems);
		}
		iterationProcessed.setText(String.format("%d/%d, %d%%",iterationProcessedItems,iterationTodoTotal,pct));
		long elapsedMs = System.currentTimeMillis() - iterationStartMs;
		iterationElapsedTime.setText(Long.toString(elapsedMs/1000) + " seconds");

		long sectionsPerSecond = (1000*iterationProcessedItems/elapsedMs);
		iterationSpeed.setText(sectionsPerSecond + " sections/second");

		long itemsToDo = iterationTodoTotal - iterationProcessedItems;
		if ( sectionsPerSecond > 0 ) {
			long etaInSeconds = itemsToDo / sectionsPerSecond;
			iterationTimeToCompletion.setText(etaInSeconds + " seconds");
		}

	}


	public void build() {
		JFrame frm = new JFrame ("Railroad Builder");

		frm.getContentPane ().setLayout(new BoxLayout(frm.getContentPane(), BoxLayout.X_AXIS));

		// Text panels
		JPanel infoPanels = new JPanel();
		infoPanels.setLayout(new BoxLayout(infoPanels, BoxLayout.Y_AXIS));
		infoPanels.add(buildTotalsPanel());
		infoPanels.add(buildIterationPanel());
		frm.getContentPane ().add(infoPanels);

		// Graphic panel
		graphicPanel = new JPanel();
		graphicPanel.setMinimumSize(new Dimension(500,500));
		graphicPanel.add(image);
		frm.getContentPane ().add(graphicPanel);

		frm.setDefaultCloseOperation (JFrame.EXIT_ON_CLOSE);
		frm.pack ();
		frm.setVisible(true);
	}


	private JPanel buildIterationPanel() {
		JPanel panel = new JPanel ();
		panel.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

		panel.add(currentIteration);

		JPanel infoPanel = new JPanel();
		FlowLayout layout = new FlowLayout();
		layout.setAlignment(FlowLayout.CENTER);
		infoPanel.setLayout(layout);
		// For processing
		{
			JPanel subpanel = new JPanel();
			subpanel.setBorder(BorderFactory.createEtchedBorder());
			subpanel.setLayout(new GridLayout(0,2));
			subpanel.add(new JLabel("Total Processed"));
			subpanel.add(iterationProcessed);
			subpanel.add(new JLabel());
			subpanel.add(iterationProgress);
			subpanel.add(new JLabel("Total Written"));
			subpanel.add(iterationWritten);
			subpanel.add(new JLabel("Total Completed"));
			subpanel.add(iterationCompleted);
			subpanel.add(new JLabel("Total Rejected"));
			subpanel.add(iterationRejected);
			infoPanel.add(subpanel);
		}
		// Time
		{
			JPanel subpanel = new JPanel();
			subpanel.setBorder(BorderFactory.createEtchedBorder());
			subpanel.setLayout(new GridLayout(0,2));
			subpanel.add(new JLabel("Start Time"));
			subpanel.add(iterationStartTime);
			subpanel.add(new JLabel("Elapsed Time"));
			subpanel.add(iterationElapsedTime);
			subpanel.add(new JLabel("Sections/Second"));
			subpanel.add(iterationSpeed);
			subpanel.add(new JLabel("Time to Completion"));
			subpanel.add(iterationTimeToCompletion);
			JLabel j = new JLabel();
			subpanel.add(j);
			subpanel.add(j);
			infoPanel.add(subpanel);
		}

		panel.add(infoPanel);
		return panel;
	}

	private JPanel buildTotalsPanel() {
		JPanel panel = new JPanel ();
		panel.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

		panel.add(new JLabel("TOTALS"));
		// Hide for now panel.add(totalProgress);

		JPanel infoPanel = new JPanel();
		FlowLayout layout = new FlowLayout();
		layout.setAlignment(FlowLayout.CENTER);
		infoPanel.setLayout(layout);
		// For processing
		{
			JPanel subpanel = new JPanel();
			subpanel.setBorder(BorderFactory.createEtchedBorder());
			subpanel.setLayout(new GridLayout(0,2));
			subpanel.add(new JLabel("Total Processed"));
			subpanel.add(totalProcessed);
			subpanel.add(new JLabel("Total Completed"));
			subpanel.add(totalCompleted);
			subpanel.add(new JLabel("Total Rejected"));
			subpanel.add(totalRejected);
			infoPanel.add(subpanel);
		}
		// Time
		{
			JPanel subpanel = new JPanel();
			subpanel.setAlignmentX(JPanel.TOP_ALIGNMENT);
			subpanel.setBorder(BorderFactory.createEtchedBorder());
			subpanel.setLayout(new GridLayout(0,2));
			subpanel.add(new JLabel("Start Time"));
			subpanel.add(totalStartTime);
			subpanel.add(new JLabel("Elapsed Time"));
			subpanel.add(totalElapsedTime);
			subpanel.add(new JLabel("Sections/Second"));
			subpanel.add(totalSpeed);
			infoPanel.add(subpanel);
		}

		panel.add(infoPanel);
		return panel;
	}
	/*
	 * TOTALS
	 *   Total Processed: N             Start Time:   XXXX
	 *   Total Completed: N             Current Time: N seconds
	 *   Total Rejected:  N             Sections/Sec: N
	 *     #1             N (M%)
	 *     #2             N (M%)
	 *     #3             N (M%)
	 * ---------------------------------------------------
	 * Current Iteration: X/Y
	 *   Total Processed: X/Y (N%)       Start Time:   XXXX
	 *   Total Completed: N              Current Time: N seconds
	 *   Total Written:   N              Sections/Sec: N
	 *   Total Rejected:  N              Est time left: N seconds
	 *     #1             N (M%)
	 *     #2             N (M%)
	 *     #3             N (M%)
	 *   
	 * ----------------------------------------------------
	 * Section List
	 *  ....
	 *  ....
	 */


	public void updateRejections(HashMap<RejectionType, Long> rejectionCounts) {
		// TODO Auto-generated method stub

	}

	@Override
	public int getResolutionHeight() {
		if ( graphicPanel == null ) {
			return 1;
		}
		return graphicPanel.getHeight();
	}

	@Override
	public int getResolutionWidth() {
		if ( graphicPanel == null ) {
			return 1;
		}
		return graphicPanel.getWidth();
	}

	@Override
	public void write(BufferedImage buffer) throws IOException {
		image.setIcon(new ImageIcon(buffer));

	}





}
