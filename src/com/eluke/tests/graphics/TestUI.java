package com.eluke.tests.graphics;

import com.eluke.graphics.UserInterface;
import com.eluke.tests.TestBase;

public class TestUI extends TestBase {

	public void testme() throws InterruptedException {
		UserInterface ui = new UserInterface();
		ui.build();
		Thread.sleep(3000);
	}
}
