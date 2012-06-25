package com.eluke.io;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import com.eluke.Progressable;
import com.eluke.Railroad;


public class Resumable {
	public static final String SAVE_FILE = "SAVE";
	
	
	public Resumable() {
		
	}
	
	public static void save(Railroad railroad, Progressable progress, InputReader input ) throws IOException {
		String directory = railroad.getDirectory();
		DataOutputStream out = IOUtils.makeOutputStream(SAVE_FILE, directory, false);
		
		// Save current railroad configuration
		out.writeBoolean(railroad.isUseIsomorph());
		railroad.getOptimizer().write(out);
		
		// Save progressable
		progress.write(out);
		System.out.println("Progress SAVED: " + progress);
		
		// Save the current location in the input file
		out.writeLong(input.getSectionsRead());
		
		out.close();
	}
	
	public static boolean load(Railroad railroad, Progressable progress, InputReader input ) throws IOException {
		String directory = railroad.getDirectory();
		DataInputStream in = null;
		try {
			in = IOUtils.makeInputStream(SAVE_FILE, directory);
		} catch (FileNotFoundException e) {
			System.out.println("NO SAVE FILE found in " + directory);
			// no save file - just return false;
			return false;
		}
		
		// Load railroad configuration
		railroad.setUseIsomorph(in.readBoolean());
		railroad.getOptimizer().read(in);
		
		// Load progressable
		progress.read(in);
		System.out.println("Progress LOADED: " + progress);
		
		// Load the current location in the input file.
		input.start(progress.getCurrentIteration()-1, progress.getIterationItemsWritten());
		input.skip(in.readLong());
		
		in.close();
		new File(directory + "/" + SAVE_FILE).delete();
		new File(directory + "/" + SAVE_FILE + ".gz").delete();
		return true;
	}
}
