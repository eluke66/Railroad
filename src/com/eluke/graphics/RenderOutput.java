package com.eluke.graphics;

import java.awt.image.BufferedImage;
import java.io.IOException;

public interface RenderOutput {
	public int getResolutionWidth();
	public int getResolutionHeight();
	public void write(BufferedImage buffer) throws IOException;
}
