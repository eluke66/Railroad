package com.eluke.tools;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;

import com.eluke.IsomorphProcessor;
import com.eluke.JoinedSection;
import com.eluke.io.IOUtils;

public class HashTester {

	/**
	 * @param args
	 * @throws IOException 
	 * @throws FileNotFoundException 
	 */
	public static void main(String[] args) throws FileNotFoundException, IOException {
		JoinedSection section = new JoinedSection();
		for (String arg : args) {
			DataInputStream in = IOUtils.makeInputStream(new File(arg));
			HashMap<Integer,Integer> codeToCountMap = new HashMap<Integer,Integer>();
			long total = 0;
			while (true) {
				try {
					section.read(in);
					Integer hash = IsomorphProcessor.getHashInternal(section);
					total++;
					if ( codeToCountMap.get(hash) == null ) {
						codeToCountMap.put(hash, 1);
					}
					else {
						codeToCountMap.put(hash, codeToCountMap.get(hash)+1);
					}
				}
				catch (IOException ioe) {
					break;
				}
			}
			
			// Print statistics
			System.out.println("File: " + arg);
			System.out.println("Total Entries: " + total);
			System.out.println("Total Unique Hashes: " + codeToCountMap.keySet().size());
			//System.out.println("Worst Hash: " + codeToCountMap.keySet().size());
		}

	}

}
