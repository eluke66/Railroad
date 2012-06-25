package com.eluke.graphics;

import java.io.File;
import java.io.IOException;

import com.eluke.CompleteSectionRecorder;
import com.eluke.JoinedSection;

public class RailroadGrapher implements CompleteSectionRecorder {
	private Renderer renderer = new Renderer();
	private ImageWriter writer;
	private String directory;
	int whichImage = 0;

	/**
	 * @param width Width
	 * @param height Height
	 * @param filename Beginning of filename - files will be named filename0001.png, filename0002.png, etc.
	 */
	public RailroadGrapher(int width, int height, String directory) {
		this.directory = directory;
		
		writer = new ImageWriter(width, height,null);
	}


	@Override
	public void emit(JoinedSection section) {
		if ( whichImage == 0 ) {
			File dirFile = new File(directory);
			System.out.println("Creating file for " + dirFile.getAbsolutePath());
			if ( !dirFile.exists() ) {
				if ( !dirFile.mkdirs() ) {
					throw new RuntimeException("Unable to create directory for image writing: " + dirFile.getAbsolutePath() );
				}
			}
		}
		whichImage++;
		writer.setFilename( String.format("%s/Render%04d", directory, whichImage));
		try {
			renderer.draw(section, writer, "Render #" + whichImage);
		} catch (IOException e) {
			System.err.println("Unable to write image " + writer.getFilename() + ": " + e.getMessage());
			e.printStackTrace();
		}
	}

}
