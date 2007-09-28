package com.bretth.osmosis.core.task.v0_4;



/**
 * Defines the interface for combining change sink and change source
 * functionality. This is typically used by classes performing some form of
 * translation on an input change source before sending along to the output.
 * This includes filtering tasks and modification tasks.
 * 
 * @author Brett Henderson
 */
public interface ChangeSinkChangeSource extends ChangeSink, ChangeSource {
	// Interface only combines functionality of its extended interfaces.
}
