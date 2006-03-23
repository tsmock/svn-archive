package org.openstreetmap.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.openstreetmap.processing.OsmApplet;

/**
 * Representation of OSM way.
 * @author Imi
 */
public class Way extends OsmPrimitive {

	/**
	 * The lines this way consist of
	 * Type: either Line or LineOnlyId (incomplete lines - only the id known)
	 */
	public List lines = new ArrayList();

	public String key() {
		return "way_" + id;
	}

	public Way(long id) {
		this.id = id;
	}

	public String getName() {
		return (String)tags.get("name");
	}

	public Line getNameLineSegment() {
		String n = (String)tags.get("name_segment");
		if (n != null) {
			try {
				long id = Long.parseLong(n);
				for (Iterator it = lines.iterator(); it.hasNext();) {
					Line l = (Line)it.next();
					if (l.id == id)
						return l;
				}
			} catch (NumberFormatException nfe) {
			}
		}
		if (lines.isEmpty())
			return null;
		return (Line)lines.get(lines.size()/2);
	}

	public String toString() {
		return "[Way "+id+" with "+lines+"]";
	}

	public Map getMainTable(OsmApplet applet) {
		return applet.ways;
	}

	public String getTypeName() {
		return "way";
	}

	public void register() {
		for (Iterator it = lines.iterator(); it.hasNext();)
			((Line)it.next()).ways.add(this);
	}

	public void unregister() {
		for (Iterator it = lines.iterator(); it.hasNext();)
			((Line)it.next()).ways.remove(this);
	}

	public Object clone() {
		Way way = (Way)super.clone();
		way.lines = new ArrayList(lines); 
		return way;
	}

	public void doCopyFrom(OsmPrimitive other) {
		lines = ((Way)other).lines;
	}
}
