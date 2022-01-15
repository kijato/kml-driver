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

import com.vividsolutions.jump.feature.*;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import com.vividsolutions.jump.io.*;
import com.vividsolutions.jump.io.datasource.DelegatingCompressedFileHandler;
import com.vividsolutions.jump.io.datasource.StandardReaderWriterFileDataSource;
import org.locationtech.jts.util.Assert;

public class KMLWriter implements JUMPWriter {
  // Standard tags for the auto-generated outputTemplate.
  public static String standard_doc = "Document";
  public static String standard_schema = "Schema";
  public static String standard_simplefield = "SimpleField";
  public static String standard_geom = "geometry";
  public static String standard_folder = "Folder";
  public static String standard_name = "name";
  public static String placemarkName = "Placemark";
  private KMLOutputTemplate outputTemplate = null;
  private final KMLGeometryWriter geometryWriter = new KMLGeometryWriter();

  /** constructor **/
  public KMLWriter() {
    geometryWriter.setLinePrefix("                ");
  }

  private static class ClassicReaderWriterFileDataSource extends
      StandardReaderWriterFileDataSource {
    public ClassicReaderWriterFileDataSource(JUMPReader reader,
        JUMPWriter writer, String[] extensions) {
      super(new DelegatingCompressedFileHandler(reader, toEndings(extensions)),
          writer, extensions);
      this.extensions = extensions;
    }
  }

  public static class KML extends ClassicReaderWriterFileDataSource {
    public KML() {
      super(new KMLReader(false), new KMLWriter(), new String[] { "kml" });
    }
  }

  /**
   * Main entry function - write the KML file.
   * 
   * @param featureCollection
   *          features to write
   * @param dp
   *          specify the 'OuputFile' and 'OuputTemplateFile'
   */
  public void write(FeatureCollection featureCollection, DriverProperties dp)
      throws Exception {
    String outputFname;
    double centralMeridian;

    outputFname = dp.getProperty("File");
    String UTMZone = dp.getProperty("UTM_Zone");
    String centralMeridianStr = dp.getProperty("Central_Meridian");

    if (outputFname == null) {
      outputFname = dp.getProperty("DefaultValue");
    }

    if (outputFname == null) {
      throw new IllegalParametersException(
          "call to KMLWRite.write() has DriverProperties w/o a OutputFile specified");
    }

    if ((UTMZone != null) && (centralMeridianStr != null)) {
      if ((UTMZone.length() > 0) && (centralMeridianStr.length() > 0)) {
        centralMeridian = Double
            .parseDouble(dp.getProperty("Central_Meridian"));
        geometryWriter.setParameters(UTMZone, centralMeridian);
        // if either of these is empty it means that the KMLGeometryWriter
        // will not be projecting the coords, ie, coord in == coord out
        // only way this happens is that the user stated map coords were
        // lat/long
      }
      outputTemplate = KMLWriter.makeOutputTemplate(featureCollection
          .getFeatureSchema());
      // java.io.Writer w = new java.io.BufferedWriter(new
      // java.io.FileWriter(outputFname));
      java.io.Writer w = new java.io.BufferedWriter(new OutputStreamWriter(
          new FileOutputStream(outputFname), StandardCharsets.UTF_8));
      this.write(featureCollection, w);
      w.close();
    }
  }

  private void write(FeatureCollection featureCollection, java.io.Writer writer)
      throws Exception {
    BufferedWriter buffWriter;
    String pre;
    String token;

    if (outputTemplate == null) {
      throw new Exception(
          "attempt to write KML w/o specifying the output template");
    }

    buffWriter = new BufferedWriter(writer);

    buffWriter.write(outputTemplate.headerText);

    for (Feature f : featureCollection.getFeatures()) {

      for (int u = 0; u < outputTemplate.featureText.size(); u++) {
        String evaled;
        pre = outputTemplate.featureText.get(u);
        token = outputTemplate.codingText.get(u);
        buffWriter.write(pre);
        evaled = evaluateToken(f, token);

        if (evaled == null) {
          evaled = "";
        }

        buffWriter.write(evaled);
      }

      buffWriter.write(outputTemplate.featureTextfooter);
      buffWriter.write("\n");
    }

    buffWriter.write(outputTemplate.footerText);
    buffWriter.flush();
  }

  /**
   * Convert an arbitary string into something that will not cause XML to gack.
   * Ie. convert "<" to "&lt;"
   *
   * @param s
   *          string to safe-ify
   */
  private static String safeXML(String s) {
    StringBuilder sb = new StringBuilder(s);
    char c;

    for (int t = 0; t < sb.length(); t++) {
      c = sb.charAt(t);

      if (c == '<') {
        sb.replace(t, t + 1, "&lt;");
      }

      if (c == '>') {
        sb.replace(t, t + 1, "&gt;");
      }

      if (c == '&') {
        sb.replace(t, t + 1, "&amp;");
      }

      if (c == '\'') {
        sb.replace(t, t + 1, "&apos;");
      }

      if (c == '"') {
        sb.replace(t, t + 1, "&quot;");
      }
    }

    return sb.toString();
  }

  /**
   * takes a token and replaces it with its value (ie. geometry or column)
   * 
   * @param f
   *          feature to take geometry or column value from
   * @param token to evaluate - "column","geometry" or "geometrytype"
   */
  private String evaluateToken(Feature f, String token) throws Exception {
    String column;
    String cmd;
    String result;
    int index;

    // token = token.toLowerCase();
    token = token.trim();

    if (!(token.startsWith("=")) || (token.length() < 7)) {
      throw new ParseException("couldn't understand token '" + token
          + "' in the output template");
    }

    token = token.substring(1);
    token = token.trim();
    index = token.indexOf(" ");

    if (index == -1) {
      cmd = token;
    } else {
      cmd = token.substring(0, token.indexOf(" "));
    }

    if (cmd.equalsIgnoreCase("column")) {
      column = token.substring(6);
      column = column.trim();

      result = toString(f, column);

      // need to ensure that the output is XML okay
      result = safeXML(result);

      return result;
    } else if (cmd.equalsIgnoreCase("geometry")) {
      geometryWriter.setMaximumCoordinatesPerLine(1);

      return geometryWriter.write(f.getGeometry());

    } else if (cmd.equalsIgnoreCase("geometrytype")) {
      return f.getGeometry().getGeometryType();
    } else {
      throw new ParseException("couldn't understand token '" + token
          + "' in the output template");
    }
  }

  protected String toString(Feature f, String column) {
    if (column.equalsIgnoreCase("FID"))
      return "" + f.getID();

    Assert.isTrue(
        f.getSchema().getAttributeType(column) != AttributeType.GEOMETRY);
    Object attribute = f.getAttribute(column);

    if (attribute == null) {
      return "";
    }
    if (attribute instanceof Date) {
      return format((Date) attribute);
    }
    return attribute.toString();
  }

  protected String format(Date date) {
    return dateFormatter.format(date);
  }

  private final SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");

  /**
   * given a FEatureSchema, make an output template in the JCS format
   * 
   * @param fcmd
   *          input featureSchema
   */
  private static KMLOutputTemplate makeOutputTemplate(FeatureSchema fcmd) {
    KMLOutputTemplate result;
    int t;
    String colName;
    String colText;
    String colCode;
    String colHeader;

    result = new KMLOutputTemplate();

    // inputTemplate = makeInputTemplate(fcmd);

    result
        .setHeaderText("<?xml version='1.0' encoding='UTF-8'?>\n<kml xmlns=\"http://earth.google.com/kml/2.0\" >\n"
            + "<"
            + standard_doc
            + "> \n"
            + "  <"
            + standard_name
            + ">"
            + "Doc1"
            + "</"
            + standard_name
            + ">\n"
            + getSchemaHeader(fcmd)
            + "    <" + standard_folder + ">\n");// +
    // "      <% FEATURE %>\n");

    colHeader = "        <" + placemarkName + "> \n";

    colText = colHeader + "          <" + standard_name + ">\n";
    colCode = "=COLUMN FID";
    colHeader = "\n          </" + standard_name + ">\n";
    result.addItem(colText, colCode);

    for (t = 0; t < fcmd.getAttributeCount(); t++) {
      colName = fcmd.getAttributeName(t);

      if (t != fcmd.getGeometryIndex()) {
        // not geometry
        colText = colHeader + "          <" + colName + ">\n";
        colCode = "=COLUMN " + colName;
        colHeader = "\n          </" + colName + ">\n";
      } else {
        // geometry
        colText = colHeader;
        colCode = "=GEOMETRY";
        colHeader = "\n";
      }

      result.addItem(colText, colCode);
    }

    result.setFeatureFooter(colHeader + "     </" + placemarkName + ">\n");
    result.setFooterText(
    // "  <% ENDFEATURE %>\n" +
        "    </" + standard_folder + ">\n" + "</" + standard_doc + ">\n"
            + "</kml>\n");

    return result;
  }

  private static String getSchemaHeader(FeatureSchema fcmd) {
    String schemaHeader;
    String fieldLine = "    <" + standard_simplefield
        + " type=\"wstring\" name=\"";

    schemaHeader = "  <" + standard_schema + " parent=\"Placemark\" name=\""
        + placemarkName + "\">\n";

    for (int t = 0; t < fcmd.getAttributeCount(); t++) {
      String colName = fcmd.getAttributeName(t);
      if (!colName.equalsIgnoreCase("Geometry")
          && (!colName.equalsIgnoreCase("FID"))) {
        schemaHeader = schemaHeader + fieldLine + colName + "\"/>\n";// +
      }
    }

    schemaHeader = schemaHeader + "  </" + standard_schema + ">\n";
    return schemaHeader;
  }
}
