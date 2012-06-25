package com.eluke.graphics;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class ImageWriter implements RenderOutput {
	private static final String OUTPUT_FORMAT = "png";
	private int width;
	private int height;
	private String filename;
	
	
	public ImageWriter(int width, int height, String filename) {
		this.width = width;
		this.height = height;
		this.filename = filename;
	}

	@Override
	public int getResolutionHeight() {
		return height;
	}

	@Override
	public int getResolutionWidth() {
		return width;
	}

	@Override
	public void write(BufferedImage buffer) throws IOException {
		ImageIO.write(buffer, OUTPUT_FORMAT, new File(filename + "." + OUTPUT_FORMAT));
	}
	
	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}
}
