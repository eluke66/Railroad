package com.eluke.io;

import java.io.DataInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import com.eluke.JoinedSection;
import com.eluke.io.SectionBlock.BlockState;

public class InputReader {

	String directory;
	DataInputStream input;
	private static final int BLOCK_SIZE = 50;
	private SectionBlock[] sections;
	private long sectionsRead = 0;
	private boolean doSave = false;

	public InputReader(String directory, int numThreads) {
		this.directory = directory;
		this.sections = new SectionBlock[numThreads * 2];
		for ( int i = 0; i < sections.length; i++ ) {
			sections[i] = new SectionBlock(BLOCK_SIZE, i);
		}
	}
	
	public void start(DataInputStream in) throws FileNotFoundException, IOException {
		input = in;
		sectionsRead = 0;
	}
	
	public void start(int iteration, int numItems) throws FileNotFoundException, IOException {
		// - Open the file, determining if gzip was used, unblock run thread
		input = IOUtils.makeInputStream(iteration, directory);
		sectionsRead = 0;
		System.out.println("Starting iteration " + iteration);
	}
	
	public void skip(long numSectionsToSkip) throws IOException {
		JoinedSection js = new JoinedSection();
		System.out.println("Need to skip " + numSectionsToSkip);
		for ( long i = 0; i < numSectionsToSkip; i++ ) {
			js.read(input);
		} 
		sectionsRead = numSectionsToSkip;
	}

	public void run() {
		//- Until we hit EOF:
		//- Read N items, adding them to a list.
		//- If EOF, set 'no more added' flag
		//- Add that list to the list of those available - use a BlockingQueue so we block until we have space
	}

	public void finish() throws IOException {
		input.close();
		for (SectionBlock section : sections) {
			doneWithSection(section);
		}
	}
	
	public void doneWithSection(SectionBlock block) {
		block.setState(BlockState.EMPTY);
		block.setCount(0);
	}
	
	public SectionBlock getSections() {
		// Stop filling blocks if we're saving.
		if ( doSave ) {
			// XXX DBG System.out.println("Saving, byebye from input reader");
			return null;
		}
		int numRead = 0;
		SectionBlock toUse = null;
		for (int i = 0; i < sections.length; i++) {
			// XXX DBG System.out.println("State: " + sections[i].getState());
			if ( sections[i].getState() == BlockState.EMPTY ) {
				toUse = sections[i];
				toUse.setState(BlockState.FILLING);
				break;
			}
		}
		try {
			// XXX DBG System.out.println("Reading...");
			for ( numRead = 0; numRead < BLOCK_SIZE; numRead++ ) {
				toUse.sections.get(numRead).read(input);
			} 
		}
		
		catch (IOException ioe) {
			// We're done

		}
		finally {
			if ( toUse != null ) {
				// XXX DBG System.out.println("Num read: " + numRead);
				toUse.setCount(numRead);
				toUse.setState(BlockState.READY);
				sectionsRead += numRead;
			}
		}
		return toUse;
		
		//- While !noMoreAdded
		//-  Poll an item off the queue, waiting up to a second.
		//-  If got something, return it.
		//- Return null.
	}

	public long getSectionsRead() {
		return sectionsRead;
	}

	public void setSectionsRead(long sectionsRead) {
		this.sectionsRead = sectionsRead;
	}

	public void save() {
		doSave = true;
	}
}
