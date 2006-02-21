/*
 * Copyright (C) 2005 Tom Carden (tom@somethingmodern.com)
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place - Suite 330, Boston, MA 02111-1307, USA.
 *  
 */

package org.openstreetmap.util;

import java.util.Collection;
import java.util.Vector;

import org.openstreetmap.client.Tile;

/**
 * Minimal representation of OpenStreetMap node (lat/lon pair, with uid)
 */
public class Node extends Point {

	/**
	 * The id of this node. Unique among other nodes. id=0 means an unknown id.
	 * TODO: check that there is no node 0 in the db
	 */
	public long id = 0;

	/**
	 * The tag string for this node.
	 */
	public String tags = "";

	/**
	 * All lines in this node.
	 */
	public Collection lines = new Vector();

	/**
	 * Create the node from projected values.
	 */
	public Node(float x, float y, Tile projection) {
		super(x, y, projection);
	}

	/**
	 * Create the node with all information given.
	 */
	public Node(double lat, double lon, long uid, String tags) {
		super(lat, lon);
		this.id = uid;
		this.tags = tags;
	}

	public String toString() {
		return "[Node " + id + " lat:" + lat + " lon:" + lon + "]";

	}

	public String key() {
		return "node_" + id;
	}
}
