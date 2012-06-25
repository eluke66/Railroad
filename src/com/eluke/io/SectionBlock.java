package com.eluke.io;

import java.util.ArrayList;

import com.eluke.JoinedSection;

public class SectionBlock {
	
	public enum BlockState {
		EMPTY, // Unused, ready for filling.
		FILLING, // Being read into
		READY // Ready for use by a worker
		
	};
	
	ArrayList<JoinedSection> sections;
	int count;
	int id;
	BlockState state;
	
	public SectionBlock(int capacity, int id) {
		this.id = id;
		this.sections = new ArrayList<JoinedSection>(capacity);
		for ( int i = 0; i < capacity; i++ ) {
			sections.add(new JoinedSection());
		}
		state = BlockState.EMPTY;
	}
	
	public ArrayList<JoinedSection> getSections() {
		return sections;
	}
	public void setSections(ArrayList<JoinedSection> sections) {
		this.sections = sections;
	}
	public int getCount() {
		return count;
	}
	public void setCount(int count) {
		this.count = count;
	}
	public int getId() {
		return id;
	}
	public BlockState getState() {
		return state;
	}
	public void setState(BlockState state) {
		this.state = state;
	}
}
