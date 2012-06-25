package com.eluke;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import javax.swing.SwingUtilities;

import com.eluke.JoinedSection.SectionAddition;
import com.eluke.graphics.RailroadGrapher;
import com.eluke.graphics.UserInterface;
import com.eluke.io.IOUtils;
import com.eluke.io.InputReader;
import com.eluke.io.OutputWriter;
import com.eluke.io.Resumable;
import com.eluke.io.SectionBlock;
import com.eluke.sections.SectionCount;
import com.eluke.sections.SectionFactory;


public class Railroad {

	private List<SectionCount> sections = new ArrayList<SectionCount>();
	private CompleteSectionRecorder recorder = null;
	private boolean useIsomorph = false;
	private boolean useGZIP = false;
	private Optimizer optimizer = new Optimizer();
	private boolean useGUI = false;
	private UserInterface ui = null;
	private String directory;
	private int numThreads = 1;
	private volatile boolean doSave = false;
	private PauseListener listener;
	private boolean profileMode = false;

	public void setOptimizerBounds(float x, float y) {
		optimizer = new Optimizer(x,y);
	}
	public void setOptimizeComplete(boolean optimizeComplete) {
		optimizer.setOptimizeCanComplete(optimizeComplete);
	}

	/**
	 * @param args
	 * @throws IOException 
	 * @throws InterruptedException 
	 */
	public static void main(String[] args) throws IOException, InterruptedException {
		Railroad railroad = new Railroad();

		// Get list of sections
		railroad.parseArguments(args);

		// Start the 'pause' listening thread
		railroad.startPauseThread();

		// Process section list
		railroad.process();

		railroad.stopPauseThread();
		System.exit(0);

	}


	private class PauseListener extends Thread {

		Railroad railroad;

		public PauseListener(Railroad railroad) {
			this.railroad = railroad;
		}

		@Override
		public void run() {
			try {
				System.in.read();
				railroad.save();
			} catch (IOException e) {
				// Ignore it - we're done!
			}

		}

	}

	private void stopPauseThread() throws InterruptedException {
		if ( !profileMode ) {
			listener.interrupt();
			listener.join();
		}
	}

	private void startPauseThread() {
		if ( !profileMode ) {
			listener = new PauseListener(this);
			listener.start();
		}
	}

	private void parseArguments(String[] args) {
		boolean optComplete = false;
		for ( int i = 0; i < args.length; i++) {
			if ( args[i].equals("-s")) {
				i++;
				while (i < args.length && !args[i].startsWith("-")) {
					sections.add(SectionFactory.makeSection(args[i]));
					i++;
				}
				if ( i != args.length) {i--;}
			}
			else if ( args[i].equals("-o") ) {
				i++;
				directory = args[i];
			}
			else if ( args[i].equals("-gui") ) {
				useGUI=true;
			}
			else if ( args[i].equals("-i") ) {
				useIsomorph=true;
			}
			else if ( args[i].equals("-profile") ) {
				profileMode=true;
			}
			else if ( args[i].equals("-accurate") ) {
				EndPoint.setMaxAccuracy(true);
			}
			else if ( args[i].equals("-g") ) {
				useGZIP=true;
			}
			else if ( args[i].equals("-threads") ) {
				i++;
				numThreads = Integer.parseInt(args[i]);
			}
			else if ( args[i].equals("-oc") ) {
				optComplete = true;
			}
			else if ( args[i].equals("-bounds") ) {
				i++;
				String[] parts = args[i].split(",");
				if ( parts.length != 2 ) {
					throw new IllegalArgumentException("Bounds should be minsize,maxsize. Our table is 27,48");
				}
				this.setOptimizerBounds(Float.parseFloat(parts[0]),Float.parseFloat(parts[0]));
			}
			else if ( args[i].equals("-draw")) {
				i++;
				String[] parts = args[i].split(",");
				int width = 500;
				int height = 500;
				String directory;
				if ( parts.length == 3) {
					width = Integer.parseInt(parts[0]);
					height = Integer.parseInt(parts[1]);
					directory = parts[2];
				}
				else if ( parts.length == 1 ) {
					directory = args[i];
				}
				else {
					throw new IllegalArgumentException("Bad drawing spec. Should be x,y,directory or just directory");
				}
				setRecorder(new RailroadGrapher(width,height,directory));
			}
			else {
				System.err.println("Unknown argument " + args[i]);
			}
		}


		if ( directory == null) {
			throw new IllegalArgumentException("Output directory not set via -o");
		}
		if ( recorder == null ) {
			setRecorder(new RailroadPrinter());
		}
		if ( optimizer == null ) {
			optimizer = new Optimizer();
		}
		optimizer.setOptimizeCanComplete(optComplete);
	}

	public void process() throws IOException {
		Progressable progress = null;
		if ( useGUI && !profileMode ) { 
			ui = new UserInterface();
			progress = new GUIProgressable(ui,1);
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					ui.build();
				}
			});
		}
		else {
			progress = new Progressable();
		}
		process(new HashSet<JoinedSection>(JoinedSection.initialize(sections)), progress);
	}

	public void save() {
		System.out.println("Saving...");
		this.doSave  = true;
	}

	/*
	For each joined_section in last_build_joined_set:
	 *    For each section_type in joined_section.list_of_unused_pieces:
	 *      For each endpoint in joined_section.endpoints:
	 *        For each section_configuration in section_type.configurations_for_endpoint_type(endpoint):
	 *          connection_type = joined_section.can_add(section_configuration, endpoint)
	 *          If connection_type.value=true
	 *             new_joined_section = joined_section(joined_section,endpoint,section_configuration,connection_type);
	 *             If new_joined_section.isComplete()
	 *               write_complete_section(new_joined_section)
	 *             Else
	 *               new.set_unused_pieces(joined_section.list_of_unused_pieces.minus(section_type))
	 *               current_build_joined_set.add(new) // Or write to disk to save memory?
	 *  last_build_joined_set = current_build_joined_set
	 */
	public void process(Set<JoinedSection> lastJoinedSections, Progressable progress) throws IOException {

		InputReader input = new InputReader(directory, numThreads);
		OutputWriter output = new OutputWriter(recorder, directory);
		boolean isRestart = false;

		// Restore if possible
		if ( Resumable.load(this, progress, input) ) {
			System.out.println("Resuming from previous save");
			isRestart = true;
		}
		else {
			IOUtils.ensureDirectory(directory);
			int expectedTotalIterations = (int) (lastJoinedSections.iterator().next().numUnusedPieces()+1);
			int numItemsWritten = 0;
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			DataOutputStream memOut = new DataOutputStream(bos);
			for ( JoinedSection js : lastJoinedSections ) { 
				numItemsWritten++;
				js.write(memOut);
			}
			memOut.close();


			progress.start(expectedTotalIterations, numItemsWritten);
			input.start(new DataInputStream(new ByteArrayInputStream(bos.toByteArray())));
		}

		// Set up isomorph removal if desired.
		IsomorphProcessor isomorpher = null;
		if ( useIsomorph ) {
			isomorpher = new IsomorphProcessor();
		}


		do {
			int numItemsWritten = 0;
			if (!isRestart) {
				if ( progress.getCurrentIteration() > 0 ) {
					input.start(progress.getCurrentIteration(), -1);
				}
				numItemsWritten = progress.getIterationItemsWritten();
				progress.startIteration();
			}

			// TODO: second param should be calculated, or estimated by numItemsWritten*N?
			output.start(progress.getCurrentIteration(), numItemsWritten, isRestart);

			if ( isRestart ) {
				isRestart = false;
			}

			// For each current mini-railroad we have:
			//  Go through all the currently unused pieces
			//   And try to attach each one to all of the open ends of the mini-railroad
			//    If we can attach it (i.e., it doesn't cross another section), then:
			//     Create a new mini-railroad with that section attached.
			//     If the new mini-railroad is complete (no open sections anymore), then write it out.
			JoinedSection joinedSection = null;

			while (true) {
				if ( doSave ) {
					input.save();
				}
				SectionBlock sections = input.getSections();
				if ( sections == null || sections.getCount() == 0 ) {
					// We're done!
					break;
				}

				for (int i = 0; i < sections.getCount(); i++) {
					joinedSection = sections.getSections().get(i);
					progress.sectionProcessed();

					for (SectionCount sectionCount : joinedSection.unusedSections) {

						for (EndPoint endPoint : joinedSection.endpoints) {
							for (SectionConfiguration sectionConfiguration : sectionCount.sectionType.configurationsForEndpoint(endPoint)) {

								// See if we can add the configuration to that joined section
								SectionAddition connection = joinedSection.canAdd(sectionConfiguration, endPoint);

								// If we can, then create a new joined section by adding the configuration to that endpoint
								if ( connection.valid ) {
									JoinedSection newJoinedSection = new JoinedSection(joinedSection, endPoint, sectionConfiguration, connection, sectionCount);

									// Further, if the new joined section is complete, then write it out
									if ( newJoinedSection.isComplete() ) {
										progress.sectionCompleted(newJoinedSection);
										output.writeCompleted(newJoinedSection);
									}
									// Otherwise, add it to the next set
									else {
										// See if we can discard this piece!
										Progressable.RejectionType rejection = optimizer.optimize(newJoinedSection);
										if ( rejection != Progressable.RejectionType.NO_REJECTION) {
											progress.addRejection(rejection);
											continue;
										}

										// Do isomorph detection if required
										Integer hash = null;
										if ( isomorpher != null ) {
											hash = isomorpher.getHash(newJoinedSection);
											if (hash == null) {
												progress.addRejection(Progressable.RejectionType.ISOMORPH);
												continue;
											}
										}
										else {
											hash = Integer.MAX_VALUE;
										}

										progress.sectionWritten();
										output.writeNext(hash, newJoinedSection);
									}

								}
								else {
									progress.addRejection(Progressable.RejectionType.CROSSED_TRACK);
								}
							}
						}
					}
				} // End iteration over JoinedSections in the SectionBlock
				input.doneWithSection(sections);

			} // End of while(true)

			input.finish();
			output.finish();

			// If we're in the process of saving, then do that and exit
			if ( doSave ) {
				System.out.println("\nWriting state to disk");
				Resumable.save(this, progress, input);
				doSave = false;
				return;
			} 

			progress.finishIteration();

			if ( isomorpher != null ) {
				isomorpher.clear();
			}
		} while ( progress.getIterationItemsWritten() > 0 );

		progress.finish();
	}

	public void processORIG(Set<JoinedSection> lastJoinedSections, Progressable progress) throws IOException {


		int expectedTotalIterations = (int) (lastJoinedSections.iterator().next().numUnusedPieces()+1);
		int numItemsWritten = 0;
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutputStream memOut = new DataOutputStream(bos);
		for ( JoinedSection js : lastJoinedSections ) { 
			numItemsWritten++;
			js.write(memOut);
		}
		memOut.close();


		progress.start(expectedTotalIterations, numItemsWritten);

		// Set up isomorph removal if desired.
		IsomorphProcessor isomorpher = null;
		if ( useIsomorph ) {
			isomorpher = new IsomorphProcessor();
		}
		DataInputStream currentInput = new DataInputStream(new ByteArrayInputStream(bos.toByteArray()));
		DataOutputStream currentOut = null;

		do {

			progress.startIteration();
			currentOut = makeOutputStream(progress.getCurrentIteration());
			// For each current mini-railroad we have:
			//  Go through all the currently unused pieces
			//   And try to attach each one to all of the open ends of the mini-railroad
			//    If we can attach it (i.e., it doesn't cross another section), then:
			//     Create a new mini-railroad with that section attached.
			//     If the new mini-railroad is complete (no open sections anymore), then write it out.
			JoinedSection joinedSection = new JoinedSection();

			while (true) {
				try {
					joinedSection.read(currentInput);
				} catch (IOException ioe) {
					// We're done
					break;
				}

				progress.sectionProcessed();

				for (SectionCount sectionCount : joinedSection.unusedSections) {

					for (EndPoint endPoint : joinedSection.endpoints) {
						for (SectionConfiguration sectionConfiguration : sectionCount.sectionType.configurationsForEndpoint(endPoint)) {

							// See if we can add the configuration to that joined section
							SectionAddition connection = joinedSection.canAdd(sectionConfiguration, endPoint);

							// If we can, then create a new joined section by adding the configuration to that endpoint
							if ( connection.valid ) {
								JoinedSection newJoinedSection = new JoinedSection(joinedSection, endPoint, sectionConfiguration, connection, sectionCount);

								// Further, if the new joined section is complete, then write it out
								if ( newJoinedSection.isComplete() ) {
									//System.out.println("Complete - have " + newJoinedSection.unusedSections.size() + " piece counts left");
									progress.sectionCompleted(newJoinedSection);
									writeCompleteSection(newJoinedSection);
								}
								// Otherwise, add it to the next set
								else {
									// See if we can discard this piece!
									Progressable.RejectionType rejection = optimizer.optimize(newJoinedSection);
									if ( rejection != Progressable.RejectionType.NO_REJECTION) {
										progress.addRejection(rejection);
										continue;
									}

									// Do isomorph detection if required
									if ( isomorpher != null ) {
										if (!isomorpher.isUnique(newJoinedSection)) {
											progress.addRejection(Progressable.RejectionType.ISOMORPH);
											continue;
										}
									}

									progress.sectionWritten();
									newJoinedSection.write(currentOut);
								}

							}
							else {
								progress.addRejection(Progressable.RejectionType.CROSSED_TRACK);
							}
						}
					}
				}
			}

			currentInput.close();
			if ( isomorpher != null ) {
				isomorpher.clear();
			}
			// If we wrote more than N records, then close in a separate thread
			if ( numItemsWritten > 20000 ) {
				new CloserThread(currentOut).start();
			}
			else {
				currentOut.close();
			}
			progress.finishIteration();
			currentInput = makeInputStream(progress.getCurrentIteration());
			
		} while ( progress.getIterationItemsWritten() > 0 );

		progress.finish();
	}

	private static class CloserThread extends Thread { 
		OutputStream o;
		public CloserThread(OutputStream o) {
			this.o = o;
		}
		public void run() {
			try {
				o.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private DataOutputStream makeOutputStream(int iteration) throws FileNotFoundException, IOException {
		if ( useGZIP ) {
			return new DataOutputStream(new BufferedOutputStream(new GZIPOutputStream(new FileOutputStream(new File("rails." + iteration)))));
		}
		return new DataOutputStream(new BufferedOutputStream(new FileOutputStream(new File("rails." + iteration))));
	}

	private DataInputStream makeInputStream(int iteration) throws FileNotFoundException, IOException {
		if ( useGZIP ) {
			return new DataInputStream(new GZIPInputStream(new BufferedInputStream(new FileInputStream(new File("rails." + iteration)))));
		}
		return new DataInputStream(new BufferedInputStream(new FileInputStream(new File("rails." + iteration))));

	}

	private void writeCompleteSection(JoinedSection newJoinedSection) {
		recorder.emit(newJoinedSection);

	}

	public CompleteSectionRecorder getRecorder() {
		return recorder;
	}

	public void setRecorder(CompleteSectionRecorder recorder) {
		this.recorder = recorder;
	}

	public boolean isUseIsomorph() {
		return useIsomorph;
	}

	public void setUseIsomorph(boolean useIsomorph) {
		this.useIsomorph = useIsomorph;
	}

	public List<SectionCount> getSections() {
		return sections;
	}
	public String getDirectory() {
		return directory;
	}
	public void setDirectory(String directory) {
		this.directory = directory;
	}
	public int getNumThreads() {
		return numThreads;
	}
	public void setNumThreads(int numThreads) {
		this.numThreads = numThreads;
	}
	public Optimizer getOptimizer() {
		return optimizer;
	}
}
