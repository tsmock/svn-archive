package org.openstreetmap.josm.plugins.mapillary.commands;

import java.util.ArrayList;
import java.util.List;

import org.openstreetmap.josm.plugins.mapillary.MapillaryAbstractImage;

/**
 * Abstract class for any Mapillary command.
 *
 * @author nokutu
 *
 */
public abstract class MapillaryCommand {
  protected List<MapillaryAbstractImage> images;

  /**
   * Main constructor.
   *
   * @param images
   *          The images that are affected by the command.
   */
  public MapillaryCommand(List<MapillaryAbstractImage> images) {
    this.images = new ArrayList<MapillaryAbstractImage>(images);
  }

  /**
   * Undoes the action.
   */
  public abstract void undo();

  /**
   * Redoes the action.
   */
  public abstract void redo();

  /**
   * If two equal commands are applied consecutively to the same set of images,
   * they are summed in order to reduce them to just one command.
   *
   * @param command
   *          The command to be summed to last command.
   */
  public abstract void sum(MapillaryCommand command);

  /**
   * Checks if the image has been modified, comparing with its original values.
   */
  public void checkModified() {
    for (MapillaryAbstractImage image : this.images)
      image.isModified = (image.tempLatLon == image.latLon || image.tempCa == image.ca);
  }

  @Override
  public abstract String toString();
}
