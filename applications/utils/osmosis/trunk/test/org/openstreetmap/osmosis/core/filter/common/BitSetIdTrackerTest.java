// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.
package org.openstreetmap.osmosis.core.filter.common;

/**
 * Tests the bitset id tracker implementation.
 */
public class BitSetIdTrackerTest extends IdTrackerBase {

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected IdTracker getImplementation() {
		return new BitSetIdTracker();
	}
}
