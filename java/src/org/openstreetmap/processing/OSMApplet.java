/*
 * Copyright (C) 2005 Tom Carden (tom@somethingmodern.com), Steve Coast (steve@asklater.com)
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

/**
 * 
 * <p>TODO BEFORE RELEASE
 * 
 * <ul>
 * <li>Save line segment names back to OSM
 * <li>Stop hard-coding my username and password (just use params)
 * <li>Report useful errors, somehow.
 * <li>Prompt for delete yes/no
 * 
 * <p>GENERAL TODO
 * 
 * <ul>
 * <li>Add an undo queue and a save button - ie don't commit changes live?
 * <li>Implement panning and zooming without reloading the applet (and recenter the projection and retransform the nodes) 
 * <li>Test inverse Mercator accuracy
 * <li>Allow show/hide for GPX tracks (right-click popup menu?)
 * <li>Colour lines according to age/recent edits 
 * <li>Draw street names, not line names, and respect curvy streets
 * <li>Draw continuous street chains as curves?
 * <li>Allow selection of multiple adjacent lines and apply same name to all of them, and then call mergeSegments (and mergeSegments should deal with this)
 * <li>Allow varying applet size 
 * <li>Allow cut and paste for street names 
 * <li>Test non-ascii characters in street names (may need a font change)
 * <li>Package standalone full screen app for demos
 * <li>Draw streets instead of lines, once streets are computed (still edit lines though)
 * <li>Copious refactoring opportunities in the point/line/latlon classes once it all works
 * <li>use off screen images for picking nodes/lines (use uid as colour?)
 * </ul>
 * 
 * <p>APPLET BUGS:
 * 
 * <ul>
 * <li>Text on vertical lines is strange?
 * </ul>
 * 
 * <p>DONE:
 * 
 * <ul>
 * <li>Allow deletion of nodes/lines 
 * <li>Load and display nodes and lines using Mercator transform
 * <li>Eliminate depency on OpenMap
 * <li>Load NASA images
 * <li>Click a line and type directly into it
 * <li>Click to add nodes
 * <li>Click and drag to add lines
 * <li>Fetch GPX track and render them to images for display 
 * <li>Fetch existing street (segment) names from OpenStreetMap (e.g. look at Manhattan Data) 
 * <li>Implement reverse Mercator transform (x/y pixels to lat/lon) 
 * <li>Fix line/point intersection algorithm so lines aren't infinitely long 
 * <li>Allow moving of nodes 
 * <li>Save modifications back to OpenStreetMap
 * <li>Improve the buttons / optimise the different "modes" 
 * <li>Change print(ln)s to print(ln)s and tell print(ln) to use status() if online
 * <li>Fix status/println to use browser status bar
 * <li>GPL everything
 * <li>Choose line width and node size based on scale 
 * </ul>
 * 
 **/

package org.openstreetmap.processing;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

import netscape.javascript.JSObject;

import org.openstreetmap.client.Adapter;
import org.openstreetmap.client.Tile;
import org.openstreetmap.util.Line;
import org.openstreetmap.util.Node;
import org.openstreetmap.util.Point;

import processing.core.PApplet;
import processing.core.PFont;

public class OsmApplet extends PApplet {

	/**
	 * Window standard width in pixel
	 */
	private static final int WINDOW_WIDTH = 700;
	/**
	 * Window standard height in pixel
	 */
	private static final int WINDOW_HEIGHT = 500;

	Tile tiles;

	private JSObject js;

	/**
	 * Current zoom level
	 */
	private int zoom = 15;

	/**
	 * Whether the left mouse button is pressed down.
	 */
	private boolean mouseDown = false;

	/**
	 * The username given as an applet parameter. To set this in test environments,
	 * pass user=(here your email) as parameter to the applet runner.
	 */
	private String userName = null;
	/**
	 * The password in cleartext given as an applet parameter. 
	 * To set this in test environments, set pass=(here your password) as parameter 
	 * to the applet runner. Do not encode the password (cleartext only).
	 */
	private String password = null;

	/**
	 * Handles most communication with the server.
	 */
	public Adapter osm;

	/**
	 * Map of OSMNodes (may or may not be projected into screen space).
	 * Type: String -> Node
	 */
	public Map nodes = new Hashtable();

	/**
	 * Collection of OSMLines
	 * Type: String -> Line
	 */
	public Map lines = new Hashtable();

	/* image showing GPX tracks - TODO: vector of PImages? one per GPX file? */
	// private PImage gpxImage;

	/**
	 * Width of line segments
	 * TODO: modulate based on scale, and road type
	 */
	float strokeWeight = 11.0f;

	/**
	 * For displaying new lines whilst drawing (between start and mouseX/Y)
	 */
	Line tempLine = new Line(null, null);

	/*
	 * current line, for editing street names - TODO: change to array of lines
	 * and apply text to all (save as a new street?) track this in editmode, and
	 * make line.selected flag
	 */
	Line selectedLine;

	/*
	 * current node, for moving nodes - TODO: track this in editmode, and make
	 * node.selected flag
	 */
	Node selectedNode = null;

	/* selected start point when drawing lines */
	Node start = null;

	/*
	 * font for street names - TODO: create on the fly? (investigate standard
	 * available fonts) modulate based on scale, and road type?
	 */
	PFont font;

	/* background image - TODO: layers of images from different mapservers? */
	// PImage img = null;
	/* URL for mapserver... will have bbx,width,height appended */
	String wmsURL = "http://www.openstreetmap.org/tile/0.2/gpx?;http://www.openstreetmap.org/api/wms/0.2/landsat/?request=GetMap&layers=modis,global_mosaic&styles=&srs=EPSG:4326&FORMAT=image/jpeg";

	// "http://onearth.jpl.nasa.gov/wms.cgi?request=GetMap&layers=modis,global_mosaic&styles=&srs=EPSG:4326&format=image/jpeg";

	String apiURL = "http://www.openstreetmap.org/api/0.2/";

	/* modes - input is passed to the current mode, assigned by node manager */
	ModeManager modeManager;

	EditMode nodeMode = new NodeMode(this);
	EditMode lineMode = new LineMode(this);
	EditMode nameMode = new NameMode(this);
	EditMode nodeMoveMode = new NodeMoveMode(this);
	EditMode deleteMode = new DeleteMode(this);
	EditMode moveMode = new MoveMode(this);

	/*
	 * if !ready, a wait cursor is shown and input doesn't do anything TODO:
	 * disable button mouseover highlighting when !ready
	 */
	boolean ready = false;

	long lastmove;

	boolean moved = true;

	boolean gotGPX = false;

	public void setup() {

		size(WINDOW_WIDTH, WINDOW_HEIGHT);
		smooth();

		// this font should have all special characters - open to suggestions
		// for changes though
		font = loadFont("/data/LucidaSansUnicode-11.vlw");

		// initialise node manager and add buttons in desired order
		modeManager = new ModeManager(this);
		modeManager.addMode(moveMode);
		modeManager.addMode(nodeMode);
		modeManager.addMode(lineMode);
		modeManager.addMode(nameMode);
		modeManager.addMode(nodeMoveMode);
		modeManager.addMode(deleteMode);

		modeManager.draw(); // make modeManager set up things

		// for centre lat/lon and scale (degrees per pixel)
		float clat = 51.526447f, clon = -0.14746371f;

		if (online) {
			if (param_float_exists("clat"))
				clat = parse_param_float("clat");
			if (param_float_exists("clon"))
				clon = parse_param_float("clon");
			if (param_float_exists("zoom"))
				zoom = parse_param_int("zoom");

			try {
				String wmsURLfromParam = param("wmsurl");
				if (wmsURLfromParam != null && !wmsURLfromParam.equals("")) {
					wmsURL = wmsURLfromParam;
					if (wmsURL.indexOf("http://") < 0)
						wmsURL = "http://" + wmsURL;
				}
			} catch (Exception e) {
				println(e.toString());
				e.printStackTrace();
			}

			try {
				String apiURLfromParam = param("apiurl");
				if (apiURLfromParam != null) {
					if (!apiURLfromParam.equals("")) {
						apiURL = apiURLfromParam;
						if (apiURL.indexOf("http://") < 0) {
							apiURL = "http://" + apiURL;
						}
					}
				}
			} catch (Exception e) {
				println(e.toString());
				e.printStackTrace();
			}

			js = JSObject.getWindow(this);
		}

		tiles = new Tile(this, wmsURL, clat, clon, WINDOW_WIDTH, WINDOW_HEIGHT, zoom);
		tiles.start();

		System.out.println(tiles);

		recalcStrokeWeight();

		System.out.println("Selected strokeWeight of " + strokeWeight);

		println("check webpage applet parameters for a user/pass");
		try {
			userName = param("user");
			password = param("pass");

			System.out.println("Got user/pass: " + userName + "/" + password);
		} catch (Exception e) {
			e.printStackTrace();
		}

		if (userName == null && password == null) {
			println("check command line arguments for a user/pass");
			for (int i = 0; i < args.length; ++i) {
				if (args[i].startsWith("--user") && args[i].indexOf('=') != -1) {
					userName = args[i].substring(args[i].indexOf('=')+1);
				}
				if (args[i].startsWith("--pass") && args[i].indexOf('=') != -1) {
					password = args[i].substring(args[i].indexOf('=')+1);
				}
				if (userName != null && password != null)
					System.out.println("Got user/pass: " + userName + "/" + password);
			}
		}

		// try to connect to OSM
		osm = new Adapter(userName, password, lines, nodes, apiURL);

		Thread dataFetcher = new Thread(new Runnable() {

			public void run() {

				osm.getNodesAndLines(tiles.getTopLeft(), tiles.getBotRight(),
						tiles);

				System.out.println("Got " + nodes.size() + " nodes and "
						+ lines.size() + " lines.");

				ready = true;

				redraw();
			}
		});

		if (osm != null) {
			dataFetcher.start();
		}

		noLoop();
		redraw();
	} // setup

	
	private boolean param_float_exists(String paramName) {
		try {
			Float.parseFloat(param(paramName));
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	private float parse_param_float(String paramName) {
		return Float.parseFloat(param(paramName));
	}

	private int parse_param_int(String paramName) {
		return Integer.parseInt(param(paramName));
	}

	public void draw() {
		tiles.draw();
		try {
			if (modeManager.currentMode == moveMode) {
				if (!mouseDown && mouseY < buttonHeight + 5 && mouseY > 5
						&& mouseX > 5
						&& mouseX < 5 + buttonWidth * modeManager.getNumModes()) {
					if (ready && !tiles.viewChanged) {
						cursor(ARROW);
					} else {
						cursor(WAIT);
					}
				} else {
					cursor(MOVE);
				}
			} else {
				if (!ready) {
					cursor(WAIT);
				} else {
					cursor(ARROW);
				}
			}
			noFill();
			strokeWeight(strokeWeight + 2.0f);
			stroke(0);
			for (Iterator it = lines.values().iterator(); it.hasNext();) {
				Line line = (Line)it.next();
				// System.out.println("Doing line " + line.a.x + "," + line.a.y
				// + " - " + line.b.x + "," + line.a.y);
				if (line.id == 0) {
					stroke(0, 80);
				} else {
					stroke(0);
				}
				line(line.from.x, line.from.y, line.to.x, line.to.y);
			}
			strokeWeight(strokeWeight);
			stroke(255);
			for (Iterator it = lines.values().iterator(); it.hasNext();) {
				Line line = (Line)it.next();
				if (line.id == 0) {
					stroke(255, 80);
				} else {
					stroke(255);
				}

				line(line.from.x, line.from.y, line.to.x, line.to.y);
			}
			boolean gotOne = false;

			for (Iterator it = lines.values().iterator(); it.hasNext();) {
				Line line = (Line)it.next();
				if (modeManager.currentMode == nameMode && !gotOne) {
					// highlight first line under mouse
					if (line.mouseOver(mouseX, mouseY, strokeWeight)
							&& line.id != 0) {
						strokeWeight(strokeWeight);
						stroke(0xffffff80);
						line(line.from.x, line.from.y, line.to.x, line.to.y);
						gotOne = true;
					}
				}
			}

			// draw temp line

			if (start != null) {
				tempLine.from = start;
				tempLine.to = new Node(mouseX, mouseY, tiles);
				stroke(0, 80);
				strokeWeight(strokeWeight + 2);
				line(tempLine.from.x, tempLine.from.y, tempLine.to.x,
						tempLine.to.y);
				stroke(255, 80);
				strokeWeight(strokeWeight);
				line(tempLine.from.x, tempLine.from.y, tempLine.to.x,
						tempLine.to.y);
			}
			// draw selected line
			stroke(255, 0, 0, 80);
			strokeWeight(strokeWeight);
			if (selectedLine != null) {
				line(selectedLine.from.x, selectedLine.from.y,
						selectedLine.to.x, selectedLine.to.y);
			}

			// draw nodes
			noStroke();
			ellipseMode(CENTER);

			for (Iterator it = nodes.values().iterator(); it.hasNext();) {
				Node node = (Node)it.next();
				if (modeManager.currentMode == lineMode && mouseOverPoint(node)) {
					fill(0xffff0000);
				} else if (modeManager.currentMode == nodeMoveMode) {
					if (node == selectedNode) {
						fill(0xff00ff00);
					} else if (mouseOverPoint(node)) {
						fill(0xffff0000);
					} else {
						fill(0xff000000);
					}
				} else if (modeManager.currentMode == deleteMode) {
					if (mouseOverPoint(node)) {
						fill(0xffff0000);
					} else {
						fill(0xff000000);
					}
				} else if (node == tempLine.from || node == tempLine.to) {
					fill(0xff000000);
				} else if (node.lines.size() > 0) {
					fill(0xffffffff);
				} else {
					fill(0xff000000);
				}
				drawPoint(node);
			}

			// draw street segment names
			fill(0);
			textFont(font);
			textSize(strokeWeight + 4);
			textAlign(CENTER);

			for (Iterator e = lines.values().iterator(); e.hasNext();) {
				Line l = (Line)e.next();
				if (l.getName() != null) {
					pushMatrix();
					if (l.from.x <= l.to.x) {
						translate(l.from.x, l.from.y);
						rotate(l.angle());
					} else {
						translate(l.to.x, l.to.y);
						rotate(PI + l.angle());
					}
					text(l.getName(), l.length() / 2.0f, 4);
					popMatrix();
				}
			}

			// draw all buttons
			modeManager.draw();
			if (online) {
				status("lat: " + tiles.lat(mouseY) + ", lon: "
						+ tiles.lon(mouseX));
			}

		} catch (NullPointerException npe) {
			println("caught null exception...");
		}

	}

	public void recalcStrokeWeight() {
		// 10m roads, but min 2px width
		strokeWeight = max(0.010f / tiles.kilometersPerPixel(), 2.0f); 
	}

	public void mouseMoved() {
		if (ready)
			modeManager.mouseMoved();
	}

	public void mouseDragged() {
		if (ready) {
			modeManager.mouseDragged();
		}
	}

	public void mousePressed() {
		mouseDown = true;
		if (ready)
			modeManager.mousePressed();
	}

	public void mouseReleased() {
		mouseDown = false;
		if (ready) {
			if (!tiles.viewChanged)
				modeManager.mouseReleased();
		}
	}

	public void keyPressed() {
		// print("keyPressed!");
		if (ready) {
			switch (key) {
			case '[':
				lastmove = System.currentTimeMillis();
				tiles.zoomin();
				updatelinks();
				break;
			case ']':
				tiles.zoomout();
				updatelinks();
				break;

			case '+':
			case '=':
				strokeWeight += 1.0f;
				redraw();
				break;
			case '-':
			case '_':
				if (strokeWeight >= 2.0f)
					strokeWeight -= 1.0f;
				redraw();
				break;
			}
			if (modeManager.currentMode == nameMode) {
				// println(key == CODED);
				// println(java.lang.Character.getNumericValue(key));
				// println("key= \"" + key + "\"");
				// println("keyCode= \"" + keyCode + "\"");
				// println("BACKSPACE= \"" + BACKSPACE + "\"");
				// println("CODED= \"" + CODED + "\"");
				modeManager.keyPressed();
			}
		}
		key = 0; // catch when key = escape otherwise processing dies
	}

	public void keyReleased() {
	} // keyReleased

	// bit crufty - TODO tidy up and move into Point
	public boolean mouseOverPoint(Point p) {
		if (p.projected) {
			// /2.0f; so you don't have to be directly on a node for it to light up
			return sqrt(sq(p.x - mouseX) + sq(p.y - mouseY)) < strokeWeight; 
		}
		return false;
	}

	public synchronized void reProject() {
		for (Iterator it = nodes.values().iterator(); it.hasNext();)
			((Node)it.next()).project(tiles);
	}

	// bit crufty - TODO tidy up and move into draw()?
	public void drawPoint(Point p) {
		if (p.projected) {
			ellipseMode(CENTER);
			ellipse(p.x, p.y, strokeWeight - 1, strokeWeight - 1);
		}
	}

	public void updatelinks() {
		js.eval("updatelinks(" + tiles.lon(WINDOW_WIDTH / 2) + "," + tiles.lat(WINDOW_HEIGHT / 2) + "," + tiles.getZoom() + ")");
	}


	///////////////////////////// BUTTON STUFF //////////////////////////////////

	float buttonWidth = 15.0f;
	float buttonHeight = 15.0f;

	// TODO PFont buttonFont; // for tool-tips

	static public void main(String args[]) {
		String[] params = new String[args.length+3];
		params[0] = "--present";
		params[1] = "--display=1";
		params[2] = "org.openstreetmap.processing.OsmApplet";
		System.arraycopy(args, 0, params, 3, args.length);
		PApplet.main(params);
	}
}
