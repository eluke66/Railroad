package com.eluke.sections;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import com.eluke.Writable;

public class SectionCount extends Writable {
	public Section sectionType;
	public int count;
	
	@Override
	public void read(DataInputStream in) throws IOException {
		count = in.readInt();
		sectionType = SectionFactory.fromType(in.readInt());
	}

	@Override
	public void write(DataOutputStream out) throws IOException {
		out.writeInt(count);
		out.writeInt(sectionType.getType());
	}
	
	public SectionCount() {}
	public SectionCount(Section sectionType, int count) {
		super();
		this.sectionType = sectionType;
		this.count = count;
	}

	public SectionCount(SectionCount sectionCount) {
		this.sectionType = sectionCount.sectionType;
		this.count = sectionCount.count;
	}

	@Override
	public int hashCode() {
		return sectionType.hashCode() + 31*count;
	}

	@Override
	public boolean equals(Object obj) {
		SectionCount sc = (SectionCount)obj;
		return sc.sectionType == sectionType && count==sc.count;
	}
}
