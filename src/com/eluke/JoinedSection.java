package com.eluke;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.eluke.sections.SectionCount;

/*
 * JoinedSection
 *   * Graph of section configurations (array, linked list, multi-point-list)
 *   * Map of endpoints to section configurations.
 *   * List of unused pieces
 *   
 *   
 */
public class JoinedSection extends Writable {
	private static final EndPoint ORIGIN = new EndPoint(0,0,0,true);

	protected List<EndPoint> endpoints;
	protected List<SectionConfiguration> sections;
	protected List<SectionCount> unusedSections;
	private BoundingBox bounds = null;

	public BoundingBox getBounds() {
		if ( bounds == null ) {
			bounds = new BoundingBox();
			for (SectionConfiguration section : sections) {
				/*for (EndPoint e : section.getEndPoints()) {
					bounds.extend(e);
				}*/
				bounds.extend(section.getBounds());
			}
		}
		return bounds;
	}

	public long numUnusedPieces() {
		long numUnused = 0;
		for ( SectionCount sc : unusedSections ) {
			numUnused += sc.count;
		}
		return numUnused;
	}

	// Gets all the unique endpoints
	public Set<EndPoint> getUniqueEndpoints() {
		Set<EndPoint> unique = new HashSet<EndPoint>();
		for (SectionConfiguration section : sections) {
			for (EndPoint e : section.getEndPoints()) {
				unique.add(e);
			}
		}
		return unique;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		for (SectionConfiguration config : sections) {
			builder.append(config.toString()).append("\n");
		}
		return builder.toString();
	}

	@Override
	public void read(DataInputStream in) throws IOException {
		this.readList(endpoints, EndPoint.class, in);
		this.readList(sections, SectionConfiguration.class, in);
		this.readList(unusedSections, SectionCount.class, in);
	}

	@Override
	public void write(DataOutputStream out) throws IOException {
		this.writeList(endpoints, out);
		this.writeList(sections, out);
		this.writeList(unusedSections, out);

	}

	// Represents the addition of a section configuration onto a joined section.
	// If valid is false, then the configuration illegally crosses another piece.
	// If valid, then the connected section is the optional *other* configuration
	// that the potential configuration can connect to (i.e., the one that it connects
	// to to close the loop). The connectedEndPoint is the endpoint on the connectedSection
	// that the potential configuration connects to.
	public static class SectionAddition {
		public boolean valid;
		public SectionConfiguration connectedSection;
		public EndPoint connectedEndPoint;

		public SectionAddition(boolean valid) {
			this.valid = valid;
			this.connectedSection = null;
			connectedEndPoint = null;
		}

		public SectionAddition(boolean valid,
				SectionConfiguration connectedSection, EndPoint endPoint) {
			this.valid = valid;
			this.connectedSection = connectedSection;
			this.connectedEndPoint = endPoint;
		}
	}

	public int unusedPiecesHash() {
		return unusedSections.hashCode();
	}

	/**
	 * Creates a new joined section from the given existing section. Adds the section configuration
	 * to the joinedSection at the given endPoint. If the "SectionAddition" specifies another endpoint
	 * (i.e., the configuration attaches to 2 points in the joinedSection), then remove it from the
	 * available set of endpoints. Finally, decrements the number of available pieces by the given section
	 * count (as the newly attached configuration was of that section type).
	 * 
	 * @param existing Existing joined section
	 * @param newSectionStart Point to add the new section configuration to.
	 * @param sectionConfiguration New section config to be added to the section
	 * @param addition Specifies if the configuration attaches to multiple endpoints.
	 * @param sectionCount Section type of new section configuration.
	 */
	public JoinedSection(JoinedSection existing, EndPoint newSectionStart, SectionConfiguration sectionConfiguration, SectionAddition addition, SectionCount sectionCount) {

		// Copy existing information
		sections = new ArrayList<SectionConfiguration>(existing.sections);
		this.endpoints = new ArrayList<EndPoint>(existing.endpoints);
		this.unusedSections = new ArrayList<SectionCount>();//existing.unusedSections);
		for ( SectionCount sc : existing.unusedSections) {
			this.unusedSections.add(new SectionCount(sc.sectionType, sc.count));
		}

		// Add existing configuration to the graph
		sections.add(sectionConfiguration);

		for (EndPoint e : sectionConfiguration.otherEndpoints(newSectionStart)) {
			endpoints.add(e);
		}
		/* EWAS
		for (EndPoint e : sectionConfiguration.otherEndpoints(newSectionStart)) {
			endpointMap.put(e,sectionConfiguration);
		}
		 */

		Iterator<EndPoint> it = endpoints.iterator();
		boolean found = false;
		while (it.hasNext()) {
			EndPoint next = it.next();
			if ( next.colocated(newSectionStart)) {
				it.remove();
			}
			// If the addition specifies an endpoint, remove it from the endpoint map
			else if (addition.connectedEndPoint != null ) {
				if ( next.colocated(addition.connectedEndPoint) ) {
					it.remove();
					found = true;
				}
			}
		}
		if ( addition.connectedEndPoint != null) {
			assert(found);
		}
		/* EWAS
		// Set the newSectionStart's endpoints to point to the new section configuration
		Iterator<EndPoint> it = endpointMap.keySet().iterator();
		while (it.hasNext()) {
			if ( it.next().colocated(newSectionStart) ) {
				it.remove();
			}
		}


		// If the addition specifies an endpoint, remove it from the endpoint map
		if (addition.connectedEndPoint != null ) {
			boolean found = false;
			it = endpointMap.keySet().iterator();
			while (it.hasNext()) {
				if ( it.next().colocated(addition.connectedEndPoint) ) {
					it.remove();
					found = true;
				}
			}
			assert(found);
		}
		 */

		// Remove sectionConfiguration's section type from the unused pieces catalog (since we're adding it here)
		decrement(sectionCount);


	}

	private void decrement(SectionCount sectionCount) {
		int idx = unusedSections.indexOf(sectionCount);
		if ( unusedSections.get(idx).count == 1 ) {
			unusedSections.remove(idx);
		}
		else {
			unusedSections.get(idx).count--;
		}
	}

	public JoinedSection() {
		endpoints = new ArrayList<EndPoint>();
		sections = new ArrayList<SectionConfiguration>();
		unusedSections = new ArrayList<SectionCount>();
	}

	// Creates a new list of joined sections from the list of input sections
	public static List<JoinedSection> initialize(List<SectionCount> sections) {
		List<JoinedSection> joinedSections = new ArrayList<JoinedSection>();

		for (SectionCount sectionCount : sections) { 
			List<SectionConfiguration> configs = sectionCount.sectionType.configurationsForEndpoint(ORIGIN);
			for (SectionConfiguration config : configs) {
				JoinedSection newJoin = new JoinedSection(sections, config, sectionCount);
				joinedSections.add(newJoin);
			}
		}

		return joinedSections;
	}

	public JoinedSection(List<SectionCount> sections, SectionConfiguration config, SectionCount sectionCount) {
		unusedSections = new ArrayList<SectionCount>();
		for (SectionCount sc : sections ) {
			unusedSections.add(new SectionCount(sc));
		}
		// Remove sectionConfiguration's section type from the unused pieces catalog (since we're adding it here)
		decrement(sectionCount);
		this.sections = new ArrayList<SectionConfiguration>();
		this.sections.add(config);
		endpoints = new ArrayList<EndPoint>();
		for (EndPoint endPoint : config.getEndPoints()) {
			endpoints.add(endPoint);
		}
	}

	public JoinedSection(List<SectionCount> sections) {
		endpoints = new ArrayList<EndPoint>();
		this.sections = new ArrayList<SectionConfiguration>();
		unusedSections = sections;
	}
	/*
	 * - can_add(SectionConfiguration, EndPoint):
	 *     - Set connecting_section to null (by default, we don't connect another endpoint)
	 *     - Transform SectionConfiguration to fit on the endPoint.
	 *     - For each of the existing section configurations:
	 *       - if SectionConfiguration.crosses_section(existing_config) return [false,null]
	 *       - if SectionConfiguration.connects_section(existing_config) connecting_section=existing_config
	 *     - Return [true,connecting_section] (okay to add!)
	 */
	public SectionAddition canAdd(SectionConfiguration sectionConfiguration, EndPoint endpoint) {
		SectionConfiguration connectingSection = null;
		EndPoint connectingEndpoint = null;


		for (SectionConfiguration existingSection : this.sections ) {
			connectingEndpoint = existingSection.connectSection(sectionConfiguration);
			if ( connectingEndpoint != null && !connectingEndpoint.colocated(endpoint) ) {
				connectingSection = existingSection;
				break;
			}
			if ( sectionConfiguration.crossesSection(existingSection) ) {
				/*System.out.println("Potential config " + sectionConfiguration + " crosses us:");
				for (SectionConfiguration e : this.sections ) {
					System.out.println("\t" + e);
				}*/

				return new SectionAddition(false);
			}

			/*
			 * WAS - try to connect first
			  if ( sectionConfiguration.crossesSection(existingSection) ) {

				return new SectionAddition(false);
			}
			connectingEndpoint = sectionConfiguration.connectSection(existingSection);
			if ( connectingEndpoint != null ) {
				connectingSection = existingSection;
				break;
			}
			 */
		}

		return new SectionAddition(true, connectingSection, connectingEndpoint);
	}

	public boolean isComplete() {
		return endpoints.isEmpty();
	}

	public List<SectionConfiguration> getSections() {
		bounds = null;
		return sections;
	}

	public List<SectionCount> getUnusedSections() {
		return unusedSections;
	}

	public void setSections(List<SectionConfiguration> sections) {
		this.sections = sections;
	}

	public void setUnusedSections(List<SectionCount> unusedSections) {
		this.unusedSections = unusedSections;
	}

	public List<EndPoint> getEndpoints() {
		return endpoints;
	}

	public void setEndpoints(List<EndPoint> endpoints) {
		this.endpoints = endpoints;
	}



}
