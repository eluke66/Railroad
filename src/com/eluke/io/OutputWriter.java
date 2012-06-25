package com.eluke.io;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.eluke.CompleteSectionRecorder;
import com.eluke.JoinedSection;

public class OutputWriter {
	public static final String INTERMEDIATE_PREFIX="intm";
	private static final int ZIP_SIZE = 10000000; // 10 million items and above get zipped
	private static final int KEEP_ITERATIONS = 2;
	private static int DEFAULT_OUTPUTS_PER_FILE = 5000000;
	private int outputsPerFile;
	CompleteSectionRecorder recorder;
	int numIntermediateOutputs;
	String directory;
	Map<Integer,DataOutputStream> outputStreams;
	DataOutputStream singleOutput = null;
	int mask;
	int numBits;

	public OutputWriter(CompleteSectionRecorder recorder, String directory) {
		this.recorder = recorder;
		this.directory = directory;
		outputStreams = new HashMap<Integer,DataOutputStream>();
		outputsPerFile = DEFAULT_OUTPUTS_PER_FILE;
	}

	public void start(int iteration, int expectedSize, boolean append) throws FileNotFoundException, IOException { 
		// - Open up output files (tune number of written files), determine if gzip needed, unblock run thread

		int numOutputFiles = expectedSize / outputsPerFile;
		int currentCount = 1;
		numBits = 0;
		for ( numBits = 0; numBits < 15; numBits++, currentCount*=2 ) {
			if ( numOutputFiles < currentCount ) { 
				break;
			}
		}
		numIntermediateOutputs = currentCount;

		System.out.println("\n\n\n******Making output stream for iteration " + iteration + " append is " + append);
		boolean useGZIP = (expectedSize > ZIP_SIZE);
		if ( iteration > KEEP_ITERATIONS ) {
			IOUtils.deleteIteration(iteration-KEEP_ITERATIONS, directory);
		}
		singleOutput = IOUtils.makeOutputStream(iteration, useGZIP, directory, append);
		if ( numIntermediateOutputs > 1 ) {

			for ( int i = 0; i < numIntermediateOutputs; i++) {
				outputStreams.put(new Integer(i), IOUtils.makeOutputStream(iteration, false, INTERMEDIATE_PREFIX + i, directory, append) );
			}

			for ( int j = 0; j < numBits; j++ ) {
				mask |= 1 << j; 
			}
			System.out.println("We have " + numIntermediateOutputs + " intermediate outputs, so mask is " + mask);
		}

	}

	public void WORKINGstart(int iteration, int expectedSize, boolean append) throws FileNotFoundException, IOException { 
		// - Open up output files (tune number of written files), determine if gzip needed, unblock run thread

		int numOutputFiles = expectedSize / outputsPerFile;
		int currentCount = 1;
		int numBits = 0;
		for ( numBits = 0; numBits < 15; numBits++, currentCount*=2 ) {
			if ( numOutputFiles < currentCount ) { 
				break;
			}
		}
		numIntermediateOutputs = currentCount;

		System.out.println("\n\n\n******Making output stream for iteration " + iteration + " append is " + append);
		singleOutput = IOUtils.makeOutputStream(iteration, false, directory, append);
		if ( numIntermediateOutputs > 1 ) {

			for ( int i = 0; i < numIntermediateOutputs; i++) {
				outputStreams.put(new Integer(i), IOUtils.makeOutputStream(iteration, false, INTERMEDIATE_PREFIX + i, directory, append) );
			}

			for ( int j = 0; j < numBits; j++ ) {
				mask |= 1 << j; 
			}
			System.out.println("We have " + numIntermediateOutputs + " intermediate outputs, so mask is " + mask);
		}

	}



	public void run() { 
		//- While !finished:
		//- Sleep 1 second
		//- Write everything in the queues to disk.
		//- Close outputs.
	}

	public void writeCompleted(JoinedSection section) {
		// - Adds section to CompletedQueue
		recorder.emit(section);
	}

	public void writeNext(Integer hash, JoinedSection section) throws IOException {
		//- Adds [hash,section] to WrittenQueue
		if (numIntermediateOutputs <= 1 ) {
			// XXX DBG System.out.println("Writing next SECTION");
			section.write(singleOutput);
		}
		else {
			Integer whichFile = getHashOutput(hash);
			DataOutputStream out = outputStreams.get(whichFile);
			if ( out == null ) {
				System.err.println("Error distributing files\n" +
						"Hash="+hash+"\tWhichFile="+whichFile+"\nKeySet="+outputStreams.keySet()+"\n"+
						"Num Intm Outputs="+numIntermediateOutputs);
			}
			section.write(out);
			out.writeInt(hash);
		}
	}

	public void finish() throws IOException {
		// - Set finished flag

		if( numIntermediateOutputs > 1 ) {
			for (DataOutputStream out : outputStreams.values()) {
				out.close();
			}
			outputStreams.clear();
			combineIntermediateOutputs();
			deleteIntmFiles();
		}

		singleOutput.close();
		singleOutput = null;

		mask = 0;
	}

	private void deleteIntmFiles() {
		for ( String file : new File(directory).list() ) {
			if ( file.contains(INTERMEDIATE_PREFIX) ) {
				new File(directory + "/" + file).delete();
			}
		}

	}

	private void combineIntermediateOutputs() throws FileNotFoundException, IOException {
		// TODO Auto-generated method stub
		Set<Integer> hashes = new HashSet<Integer>();
		Integer hash;
		JoinedSection section = new JoinedSection();
		File theDir = new File(directory);
		DataInputStream input;
		for (File file : theDir.listFiles(new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return (name.startsWith(INTERMEDIATE_PREFIX));
			}
		})) {
			input = IOUtils.makeInputStream(file);
			while (true) {
				try {
					section.read(input);
					hash = input.readInt();

					// MAX VALUE is the special value of "no isomorph, so no hash detection"
					if ( hash==Integer.MAX_VALUE || !hashes.contains(hash) ) {
						section.write(singleOutput);
						hashes.add(hash);
					}
					if ( hashes.size() > 20000000 ) {
						System.out.println("WARN: Clearing hashes on intermediate output combination. Size is " + hashes.size());
						hashes.clear();
					}
				} catch (IOException ioe) {
					// We're done
					break;
				}
			}
			input.close();
			hashes.clear();
		}
	}

	private int getHashOutput(Integer hash) {
		int iterations = 32 / numBits;
		int out = 0;
		int shift = 0;
		int theHash = hash >> 4;
		for ( int i = 0; i < iterations; i++, shift += numBits ) {
			out ^= (theHash & (mask << shift));
		}
		return (out & mask);
	}

	private Byte OLDgetHashOutput(Integer hash) {
		return new Byte((byte) (hash & mask));
	}

	public void setOutputsPerFile(int outputsPerFile) {
		this.outputsPerFile = outputsPerFile;
	}

	public int getOutputsPerFile() {
		return outputsPerFile;
	}

}
