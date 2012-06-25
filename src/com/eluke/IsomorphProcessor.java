package com.eluke;

import java.util.HashSet;
import java.util.Set;

import com.eluke.sections.SectionCount;

/*
 * Isomorph idea.
 * Two sections of track are isomorphs if:
 *  - They have the same number of pieces remaining.
 *  - They have the same exposed endpoints (absolute values)?
 *  
 *  - NEW: One more idea - isomorphs have the same number of pieces remaining,
 *    and the set of current sections is in the same order. Need to do a graph 
 *    traversal, choose forks left to right?
 */
public class IsomorphProcessor {
	private static final int MAX_ISOMORPHS = 20000000;
	private Set<Integer> isomorphs = new HashSet<Integer>(MAX_ISOMORPHS);
	private long rejections = 0;

	public long getRejections() {
		return rejections;
	}

	public Set<Integer> getIsomorphs() {
		return isomorphs;
	}

	public void setIsomorphs(Set<Integer> isomorphs) {
		this.isomorphs = isomorphs;
	}

	// Clears out the current list of isomorphs
	public void clear() {
		isomorphs.clear();
		rejections = 0;
	}

	static boolean oldHashing = false;
	public static Integer getHashInternal(JoinedSection section) {
		// Get the hash representing the different sections remaining.
		// This can be optimized
		Integer hash;
		if ( oldHashing ) {
			StringBuffer sb = new StringBuffer();
			for (SectionCount count : section.unusedSections) {
				sb.append(count.sectionType.getClass()).append(count.count);
			}
			hash = sb.toString().hashCode();
		}
		else {
			hash = new Integer(17);
			for (SectionCount count : section.unusedSections) {
				hash = 31* hash + count.hashCode();
			}
		}

		// Get the hash for the available endpoints
		for (EndPoint ep : section.endpoints) {
			hash *= ep.absHash();
		}
	
		return hash;
	}


	public Integer getHash(JoinedSection section) {
		Integer hash = getHashInternal(section);

		// Finally, if we have something that already looks like
		// this, return false (not unique).
		if ( isomorphs.contains(hash)) {
			rejections++;
			return null;
		}

		// If we need to clear the isomorph set, then do it
		if ( isomorphs.size() > MAX_ISOMORPHS ) {
			System.out.println("Clearing isomorphs at size " + isomorphs.size());
			isomorphs.clear();
		}

		// Else, add it to our set of unique things and return
		// true (it is unique).
		isomorphs.add(hash);
		return hash;
	}

	public boolean isUnique(JoinedSection section) {
		return getHash(section) != null;
	}
}
