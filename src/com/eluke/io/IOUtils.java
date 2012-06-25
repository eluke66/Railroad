package com.eluke.io;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class IOUtils {

	public static final String OUTPUT_PREFIX="rails";
	
	public static void ensureDirectory(String directory) throws IOException {
		File dir = new File(directory);
		if ( !dir.exists() ) {
			if ( !dir.mkdirs() ) {
				throw new IOException("Unable to ensure directory " + directory);
			}
		}
		else {
			if ( !dir.isDirectory() ) {
				throw new IllegalArgumentException("Unable to ensure directory " + directory + " as it exists and is not a directory");
			}
			else {
				for (File f : dir.listFiles()) {
					f.delete();
				}
			}
		}
	}
	public static DataInputStream makeInputStream(int iteration, String directory) throws FileNotFoundException, IOException {
		/*File file = new File(directory + "/" + OUTPUT_PREFIX + "." + iteration + ".gz");
		boolean useGZIP = true;
		if ( !file.exists() ) {
			file = new File(directory + "/" + OUTPUT_PREFIX + "." + iteration);
			useGZIP = false;
		}
		if ( useGZIP ) {
			return new DataInputStream(new GZIPInputStream(new BufferedInputStream(new FileInputStream(file))));
		}
		return new DataInputStream(new BufferedInputStream(new FileInputStream(file)));
*/
		return makeInputStream(OUTPUT_PREFIX + "." + iteration, directory);
	}
	public static DataInputStream makeInputStream(File file) throws FileNotFoundException, IOException {
		return new DataInputStream(new BufferedInputStream(new FileInputStream(file)));
	}
	
	public static DataInputStream makeInputStream(String filename, String directory) throws IOException {
		File file = new File(directory + "/" + filename + ".gz");
		boolean useGZIP = true;
		if ( !file.exists() ) {
			file = new File(directory + "/" + filename);
			useGZIP = false;
		}
		if ( useGZIP ) {
			return new DataInputStream(new GZIPInputStream(new BufferedInputStream(new FileInputStream(file))));
		}
		return new DataInputStream(new BufferedInputStream(new FileInputStream(file)));
	}
	public static DataOutputStream makeOutputStream(String filename, String directory, boolean useGZIP) throws IOException {
		if ( useGZIP ) {
			return new DataOutputStream(new BufferedOutputStream(new GZIPOutputStream(new FileOutputStream(new File(directory+"/" + filename + ".gz")))));
		}
		return new DataOutputStream(new BufferedOutputStream(new FileOutputStream(new File(directory+"/" + filename))));
	
	}
	
	public static DataOutputStream makeOutputStream(int iteration,
			boolean useGZIP, String directory, boolean append) throws IOException {
		if ( useGZIP ) {
			return new DataOutputStream(new BufferedOutputStream(new GZIPOutputStream(new FileOutputStream(new File(directory+"/" + OUTPUT_PREFIX + "." + iteration + ".gz"), append))));
		}
		return new DataOutputStream(new BufferedOutputStream(new FileOutputStream(new File(directory+"/" + OUTPUT_PREFIX + "." + iteration), append)));
	}
	
	public static DataOutputStream makeOutputStream(int iteration, boolean useGZIP, String directory) throws FileNotFoundException, IOException {
		return makeOutputStream(iteration, useGZIP, directory);
	}
	
	public static DataOutputStream makeOutputStream(int iteration, boolean useGZIP, String prefix, String directory, boolean append) throws FileNotFoundException, IOException {
		if ( useGZIP ) {
			return new DataOutputStream(new BufferedOutputStream(new GZIPOutputStream(new FileOutputStream(new File(directory+"/" + prefix + "." + iteration + ".gz"), append))));
		}
		return new DataOutputStream(new BufferedOutputStream(new FileOutputStream(new File(directory+"/" + prefix + "." + iteration), append)));
	}
	
	public static DataOutputStream makeOutputStream(int iteration, boolean useGZIP, String prefix, String directory) throws FileNotFoundException, IOException {
		return makeOutputStream(iteration, useGZIP, prefix, directory, false);
	}
	
	public static void deleteIteration(int iteration, String directory) {
		File file = new File(directory+"/" + OUTPUT_PREFIX + "." + iteration + ".gz");
		if ( file.exists() ) {
			System.out.println("Delete of old iteration file " + file.getName() + " is " + file.delete());
		}
		else {
			file = new File(directory+"/" + OUTPUT_PREFIX + "." + iteration);
			if ( file.exists() ) {
				System.out.println("Delete of old iteration file " + file.getName() + " is " + file.delete());
			}
		}
		
	}
	
}
