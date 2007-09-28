package com.bretth.osmosis.core.task.v0_4;



/**
 * Defines the interface for combining multi sink and source functionality. This
 * is typically used by classes combining the contents of two data sources.
 * 
 * @author Brett Henderson
 */
public interface MultiSinkRunnableSource extends MultiSink, RunnableSource {
	// Interface only combines functionality of its extended interfaces.
}
