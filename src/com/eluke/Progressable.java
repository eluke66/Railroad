package com.eluke;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map.Entry;

import com.eluke.utils.Utils;

public class Progressable extends Writable {
	public enum RejectionType {
		CROSSED_TRACK,
		ISOMORPH,
		BOUNDING_BOX,
		UNCOMPLETABLE,
		NO_REJECTION
	}
	
	protected long startTimeMs;
	protected long iterationStartMs;
	protected int currentIteration;

	protected int totalIterations;
	protected long currentIterationSize = 0;
	protected long iterationRejections = 0;
	protected long totalItems = 0;
	protected HashMap<RejectionType,Long> rejectionCounts = new HashMap<RejectionType,Long>();
	protected int iterationCompletions;
	protected int iterationItemsWritten;
	protected int iterationSectionsProcessed;

	@Override
	public String toString() {
		return "Progressable [currentIteration=" + currentIteration
				+ ", currentIterationSize=" + currentIterationSize
				+ ", iterationCompletions=" + iterationCompletions
				+ ", iterationItemsWritten=" + iterationItemsWritten
				+ ", iterationRejections=" + iterationRejections
				+ ", iterationSectionsProcessed=" + iterationSectionsProcessed
				+ ", iterationStartMs=" + iterationStartMs
				+ ", rejectionCounts=" + rejectionCounts + ", startTimeMs="
				+ startTimeMs + ", totalItems=" + totalItems
				+ ", totalIterations=" + totalIterations + "]";
	}

	@Override
	public void read(DataInputStream in) throws IOException {
		startTimeMs = in.readLong();
		iterationStartMs = in.readLong();
		currentIterationSize = in.readLong();
		iterationRejections = in.readLong();
		totalItems = in.readLong();
		currentIteration = in.readInt();
		totalIterations = in.readInt();
		iterationCompletions = in.readInt();
		iterationItemsWritten = in.readInt();
		iterationSectionsProcessed = in.readInt();
		int mapSize = in.readInt();
		rejectionCounts.clear();
		for ( int i = 0; i < mapSize; i++ ) {
			String name = in.readUTF();
			Long value = in.readLong();
			rejectionCounts.put(RejectionType.valueOf(name), value);
		}
	}
	
	@Override
	public void write(DataOutputStream out) throws IOException {
		out.writeLong(startTimeMs);
		out.writeLong(iterationStartMs);
		out.writeLong(currentIterationSize);
		out.writeLong(iterationRejections);
		out.writeLong(totalItems);
		out.writeInt(currentIteration);
		out.writeInt(totalIterations);
		out.writeInt(iterationCompletions);
		out.writeInt(iterationItemsWritten);
		out.writeInt(iterationSectionsProcessed);
		out.writeInt(rejectionCounts.keySet().size());
		for (Entry<RejectionType,Long> entry : rejectionCounts.entrySet()) {
			out.writeUTF(entry.getKey().toString());
			out.writeLong(entry.getValue());
		}
	}
	
	public int getCurrentIteration() {
		return currentIteration;
	}
	public void addRejection(RejectionType type) {
		iterationRejections++;
		if (!rejectionCounts.containsKey(type)) {
			rejectionCounts.put(type, 1L);
		}
		else {
			rejectionCounts.put(type, rejectionCounts.get(type)+1);
		}
	}
	
	public void start(int totalIterations, long initialIterationSize) { 
		startTimeMs = System.currentTimeMillis();
		currentIteration = 0;
		this.totalIterations = totalIterations;
		this.currentIterationSize = initialIterationSize;
		System.out.println("Processing " + totalIterations + " iterations.");
	}
	
	public void finish() {
		System.out.println("\nTotal Time: " + (System.currentTimeMillis() - startTimeMs) / 1000 + " seconds");
		System.out.println("Total Items Considered: " + this.totalItems);
		Long totalRejections = 0L;
		StringBuffer buffer = new StringBuffer();
		
		for (RejectionType type : Utils.SortMapByValues(rejectionCounts, true)) {
			long count = rejectionCounts.get(type);
			totalRejections += count;
			buffer.append("\t").append(type).append(":\t").append(count).append("\n");
		}
		
		System.out.println("Total Rejections: " + totalRejections + " (" + (100*totalRejections/this.totalItems) + " %)");
		System.out.print(buffer.toString());
	}
	
	public void startIteration() {
		currentIteration++;
		iterationRejections = 0;
		iterationCompletions = 0;
		iterationItemsWritten = 0;
		iterationSectionsProcessed = 0;
		iterationStartMs = System.currentTimeMillis(); 	
		
		System.out.print(
			String.format("Iteration %02d/%02d: %08d sections.", currentIteration, totalIterations, currentIterationSize) );
	}
	
	public int getIterationSectionsProcessed() {
		return iterationSectionsProcessed;
	}
	public void setIterationSectionsProcessed(int iterationSectionsProcessed) {
		this.iterationSectionsProcessed = iterationSectionsProcessed;
	}
	public void sectionProcessed() {
		iterationSectionsProcessed++;
	}
	public void sectionCompleted(JoinedSection newJoinedSection) {
		iterationCompletions++;
	}
	
	public void sectionWritten() {
		iterationItemsWritten++;
	}
	
	public int getIterationItemsWritten() {
		return iterationItemsWritten;
	}
	public void finishIteration() {
		long end = System.currentTimeMillis();
		
		long ms = end-iterationStartMs;
		
		
		System.out.print(String.format(" %03d sec", ms/1000));
		if ( ms != 0 ) { 
			System.out.print(String.format(", %05d sections/second",(1000*currentIterationSize/ms)));
		}
		System.out.println(String.format(" Rejects: %08d Complete: %04d Written: %08d",iterationRejections,iterationCompletions,iterationItemsWritten));
		currentIterationSize = iterationItemsWritten; 
		totalItems += iterationRejections + iterationCompletions + iterationItemsWritten;
	}
	
}
