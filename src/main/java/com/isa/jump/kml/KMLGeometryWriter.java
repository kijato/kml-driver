/*
 * The Unified Mapping Platform (JUMP) is an extensible, interactive GUI 
 * for visualizing and manipulating spatial features with geometry and attributes.
 *
 * JUMP is Copyright (C) 2003 Vivid Solutions
 *
 * This program implements extensions to JUMP and is
 * Copyright (C) 2005 Integrated Systems Analysts, Inc.
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 * 
 * For more information, contact:
 *
 * Integrated Systems Analysts, Inc.
 * 630C Anchors St., Suite 101
 * Fort Walton Beach, Florida
 * USA
 *
 * (850)862-7321
 */

package com.isa.jump.kml;

import com.vividsolutions.jump.coordsys.Reprojector;
import com.vividsolutions.jump.coordsys.CoordinateSystem;
import com.vividsolutions.jump.coordsys.impl.PredefinedCoordinateSystems;
import com.vividsolutions.jump.coordsys.Radius;
import com.vividsolutions.jump.coordsys.Spheroid;
import org.locationtech.jts.geom.*;

import java.io.*;


/**
 * Writes or creates a formatted string containing the KML representation of a
 * JTS Geometry. Supports a user-defined line prefix and a user-defined maximum
 * number of coordinates per line. Indents components of Geometries to provide a
 * nicely-formatted representation.
 */
public class KMLGeometryWriter {

  String zoneStr = "";
  int zoneInt = 0;
  boolean zoneSouth = false;
  double centralMeridian = 0.0;

  /**
   * Returns a <code>String</code> of repeated characters.
   *
   * @param ch
   *          the character to repeat
   * @param count
   *          the number of times to repeat the character
   * @return a <code>String</code> of characters
   */
  private static String stringOfChar(char ch, int count) {
    StringBuilder buf = new StringBuilder();
    for (int i = 0; i < count; i++) {
      buf.append(ch);
    }
    return buf.toString();
  }

  private final int INDENT_SIZE = 2;
  // these could be made setable
  private static final String coordinateSeparator = ",";
  private static final String tupleSeparator = " ";

  private String linePrefix = null;
  private int maxCoordinatesPerLine = 10;
  private String srsName = null;
  private String gid = null;

  public KMLGeometryWriter() {
  }

  public void setParameters(String zoneStr, double centralMeridian) {
    if (zoneStr.length() > 0) {
      this.zoneStr = zoneStr;
      zoneSouth = (zoneStr.endsWith("S"));
      this.zoneInt = Integer
          .parseInt(zoneStr.substring(0, zoneStr.length() - 1));
      this.centralMeridian = centralMeridian;
    }
  }

  public void setLinePrefix(String linePrefix) {
    this.linePrefix = linePrefix;
  }

  /**
   * Sets the <code>srsName</code> attribute to be output on the Geometry
   * element. If <code>null</code> no attribute will be output.
   * 
   * @param srsName name of the Spatial Reference system
   */
  public void setSRSName(String srsName) {
    this.srsName = srsName;
  }

  /**
   * Sets the <code>gid</code> attribute to be output on the Geometry element.
   * If <code>null</code> no attribute will be output.
   * 
   * @param gid feature identifier
   */
  public void setGID(String gid) {
    this.gid = gid;
  }

  public void setMaximumCoordinatesPerLine(int maxCoordinatesPerLine) {
    if (maxCoordinatesPerLine <= 0) {
      maxCoordinatesPerLine = 1;
    }
    this.maxCoordinatesPerLine = maxCoordinatesPerLine;
  }

  public String write(Geometry geom) {
    StringBuffer buf = new StringBuffer();
    write(geom, buf);
    return buf.toString();
  }

  public void write(Geometry geometry, Writer writer) throws IOException {
    writer.write(write(geometry));
  }

  /**
   * Generates the KML representation of a JTS Geometry.
   * 
   * @param g
   *          Geometry to output
   */
  public void write(Geometry g, StringBuffer buf) {
    writeGeometry(g, attributeString(), 0, buf);
  }

  /**
   * Generates the KML representation of a JTS Geometry.
   * 
   * @param g
   *          Geometry to output
   */
  private void writeGeometry(Geometry g, String attributes, int level,
      StringBuffer buf) {
    /*
     * order is important in this if-else list. E.g. homogeneous collections
     * need to come before GeometryCollection
     */
    if (g instanceof Point) {
      writePoint((Point) g, attributes, level, buf);
    } else if (g instanceof LinearRing) {
      writeLinearRing((LinearRing) g, attributes, level, buf);
    } else if (g instanceof LineString) {
      writeLineString((LineString) g, attributes, level, buf);
    } else if (g instanceof Polygon) {
      writePolygon((Polygon) g, attributes, level, buf);
    } else if (g instanceof MultiPoint) {
      writeMultiPoint((MultiPoint) g, attributes, level, buf);
    } else if (g instanceof MultiLineString) {
      writeMultiLineString((MultiLineString) g, attributes, level, buf);
    } else if (g instanceof MultiPolygon) {
      writeMultiPolygon((MultiPolygon) g, attributes, level, buf);
    } else if (g instanceof GeometryCollection) {
      writeGeometryCollection((GeometryCollection) g, attributes, level, buf);
    }
    // throw an error for an unknown type?
  }

  private void startLine(StringBuffer buf, int level, String text) {
    if (linePrefix != null)
      buf.append(linePrefix);
    buf.append(stringOfChar(' ', INDENT_SIZE * level));
    buf.append(text);
  }

  private String geometryTag(String geometryName, String attributes) {
    StringBuilder buf = new StringBuilder();
    buf.append("<");
    buf.append(geometryName);
    if (attributes != null && attributes.length() > 0) {
      buf.append(" ");
      buf.append(attributes);
    }
    buf.append(">");
    return buf.toString();
  }

  private String attributeString() {
    StringBuilder buf = new StringBuilder();
    if (gid != null) {
      buf.append(" gid='");
      buf.append(gid);
      buf.append("'");
    }
    if (srsName != null) {
      buf.append(" srsName='");
      buf.append(srsName);
      buf.append("'");
    }
    return buf.toString();
  }

  // <Point><coordinates>1195156.78946687,382069.533723461</coordinates></Point>
  private void writePoint(Point p, String attributes, int level,
      StringBuffer buf) {
    startLine(buf, level, geometryTag("Point", attributes) + "\n");
    write(new Coordinate[] { p.getCoordinate() }, level + 1, buf);
    startLine(buf, level, "</Point>\n");
  }

  // <LineString><coordinates>1195123.37289257,381985.763974674
  // 1195120.22369473,381964.660533343
  // 1195118.14929823,381942.597718511</coordinates></LineString>
  private void writeLineString(LineString ls, String attributes, int level,
      StringBuffer buf) {
    startLine(buf, level, geometryTag("LineString", attributes) + "\n");
    write(ls.getCoordinates(), level + 1, buf);
    startLine(buf, level, "</LineString>\n");
  }

  // <LinearRing><coordinates>1226890.26761027,1466433.47430292
  // 1226880.59239079,1466427.03208053...></coordinates></LinearRing>
  private void writeLinearRing(LinearRing lr, String attributes, int level,
      StringBuffer buf) {
    startLine(buf, level, geometryTag("LinearRing", attributes) + "\n");
    write(lr.getCoordinates(), level + 1, buf);
    startLine(buf, level, "</LinearRing>\n");
  }

  private void writePolygon(Polygon p, String attributes, int level,
      StringBuffer buf) {
    startLine(buf, level, geometryTag("Polygon", attributes) + "\n");

    startLine(buf, level, "  <outerBoundaryIs>\n");
    writeLinearRing(p.getExteriorRing(), null, level + 1, buf);
    startLine(buf, level, "  </outerBoundaryIs>\n");

    for (int t = 0; t < p.getNumInteriorRing(); t++) {
      startLine(buf, level, "  <innerBoundaryIs>\n");
      writeLinearRing(p.getInteriorRingN(t), null, level + 1, buf);
      startLine(buf, level, "  </innerBoundaryIs>\n");
    }

    startLine(buf, level, "</Polygon>\n");
  }

  private void writeMultiPoint(MultiPoint mp, String attributes, int level,
      StringBuffer buf) {
    startLine(buf, level, geometryTag("MultiPoint", attributes) + "\n");
    for (int t = 0; t < mp.getNumGeometries(); t++) {
      startLine(buf, level, "  <pointMember>\n");
      writePoint((Point) mp.getGeometryN(t), null, level + 1, buf);
      startLine(buf, level, "  </pointMember>\n");
    }
    startLine(buf, level, "</MultiPoint>\n");
  }

  private void writeMultiLineString(MultiLineString mls, String attributes,
      int level, StringBuffer buf) {
    startLine(buf, level, geometryTag("MultiLineString", attributes) + "\n");
    for (int t = 0; t < mls.getNumGeometries(); t++) {
      startLine(buf, level, "  <lineStringMember>\n");
      writeLineString((LineString) mls.getGeometryN(t), null, level + 1, buf);
      startLine(buf, level, "  </lineStringMember>\n");
    }
    startLine(buf, level, "</MultiLineString>\n");
  }

  private void writeMultiPolygon(MultiPolygon mp, String attributes, int level,
      StringBuffer buf) {
    startLine(buf, level, geometryTag("MultiPolygon", attributes) + "\n");
    for (int t = 0; t < mp.getNumGeometries(); t++) {
      startLine(buf, level, "  <polygonMember>\n");
      writePolygon((Polygon) mp.getGeometryN(t), null, level + 1, buf);
      startLine(buf, level, "  </polygonMember>\n");
    }
    startLine(buf, level, "</MultiPolygon>\n");
  }

  private void writeGeometryCollection(GeometryCollection gc,
      String attributes, int level, StringBuffer buf) {
    startLine(buf, level, geometryTag("MultiGeometry", attributes) + "\n");
    for (int t = 0; t < gc.getNumGeometries(); t++) {
      startLine(buf, level, "  <geometryMember>\n");
      writeGeometry(gc.getGeometryN(t), null, level + 1, buf);
      startLine(buf, level, "  </geometryMember>\n");
    }
    startLine(buf, level, "</MultiGeometry>\n");
  }

  /**
   * Takes a list of coordinates and converts it to KML.<br>
   * 2d and 3d aware. Terminates the coordinate output with a newline.
   *
   * @param coords
   *          array of coordinates
   */
  private void write(Coordinate[] coords, int level, StringBuffer buf) {
    CoordinateSystem destination = null;
    CoordinateSystem source = null;

    if (zoneInt > 0) {
      destination = PredefinedCoordinateSystems.GEOGRAPHICS_WGS_84;
      source = new CoordinateSystem("UTM " + zoneStr + " / WGS 84",
          32600 + zoneInt, new UniversalTransverseMercator() {
            {
              setSpheroid(new Spheroid(new Radius(Radius.GRS80)));
              setParameters(zoneInt, zoneSouth, centralMeridian);
            }
          });
    }

    startLine(buf, level, "<coordinates>");

    boolean isNewLine = false;
    for (int i = 0; i < coords.length; i++) {
      if (isNewLine) {
        startLine(buf, level, "  ");
        isNewLine = false;
      }

      Coordinate coord = new Coordinate(coords[i]);
      if (source != null)
        Reprojector.instance().reproject(coord, source, destination);
      buf.append(coord.x);
      buf.append(coordinateSeparator);
      buf.append(coord.y);
      buf.append(coordinateSeparator);

      if (Double.isNaN(coords[0].z))
        buf.append(0.0);
      else
        buf.append(coord.z);

      buf.append(tupleSeparator);

      // break output lines to prevent them from getting too long
      if ((i + 1) % maxCoordinatesPerLine == 0 && i < coords.length - 1) {
        buf.append("\n");
        isNewLine = true;
      }
    }

    buf.append("</coordinates>\n");
  }

  // public class UniversalTransverseMercator extends Projection {
  //
  // private final static double SCALE_FACTOR = 0.9996;
  // private final static double FALSE_EASTING = 500000.0;
  // private final static double FALSE_NORTHING = 10000000.0;
  //
  // private TransverseMercator transverseMercator = new TransverseMercator();
  //
  // public UniversalTransverseMercator() { }
  //
  // private int zone = -1;
  //
  // public void setParameters(int zone, double centralMeridian)
  // {
  // Assert.isTrue(zone <= 60, "UTM zone " + zone + " not supported");
  // Assert.isTrue(zone >= 1, "UTM zone " + zone + " not supported");
  // transverseMercator.setParameters(centralMeridian);
  // this.zone = zone;
  // }
  //
  // public void setSpheroid(Spheroid s) {
  // transverseMercator.setSpheroid(s);
  // }
  //
  // public Geographic asGeographic(Planar p, Geographic q) {
  //
  // Assert.isTrue(zone != -1, "Call #setParameters first");
  //
  // p.x = (p.x - FALSE_EASTING) / SCALE_FACTOR;
  // double falseNorthing = (zoneSouth) ? FALSE_NORTHING : 0;
  // p.y = (p.y - falseNorthing) / SCALE_FACTOR;
  // transverseMercator.asGeographic(p, q);
  // return q;
  // }
  //
  // public Planar asPlanar(Geographic q0, Planar p) {
  //
  // Assert.isTrue(zone != -1, "Call #setParameters first");
  //
  // transverseMercator.asPlanar(q0, p);
  // p.x = SCALE_FACTOR * p.x + FALSE_EASTING;
  // double falseNorthing = (q0.lat<0) ? FALSE_NORTHING : 0;
  // p.y = SCALE_FACTOR * p.y + falseNorthing;
  // return p;
  // }
  //
  // }
}
