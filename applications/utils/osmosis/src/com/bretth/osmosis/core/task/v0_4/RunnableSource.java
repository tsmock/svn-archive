package com.bretth.osmosis.core.task.v0_4;



/**
 * Extends the basic Source interface with the Runnable capability. Runnable
 * is not applied to the Source interface because tasks that act as filters
 * do not require Runnable capability.
 * 
 * @author Brett Henderson
 */
public interface RunnableSource extends Source, Runnable {
	// This interface combines Source and Runnable but doesn't introduce
	// methods of its own.
}
