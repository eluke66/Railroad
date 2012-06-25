package com.eluke;

public class RailroadPrinter implements CompleteSectionRecorder {

	@Override
	public void emit(JoinedSection section) {
		System.out.println(section);

	}

}
