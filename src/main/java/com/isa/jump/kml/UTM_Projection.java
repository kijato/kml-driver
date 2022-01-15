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

import java.io.File;
import java.util.List;
import com.vividsolutions.jump.util.FileUtil;
import java.io.IOException;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;

public class UTM_Projection extends java.lang.Object {
  private String projectionString = "";

  public UTM_Projection(String projectionString) {
    this.projectionString = projectionString;
  }

  public UTM_Projection(PlugInContext context, String fileName) {
    if (new File(fileName).exists()) {
      try {
        List<String> fileContents = FileUtil.getContents(fileName);
        projectionString = fileContents.get(0);
      } catch (IOException ex) {
        context
            .getWorkbenchFrame()
            .getOutputFrame()
            .addText(
                "Could not read: " + fileName + " due to: " + ex.getMessage());
      }
    }
  }

  public String getProjectionString() {
    return projectionString;
  }

  public boolean isUTM() {
    if (projectionString.indexOf("WGS_1984_UTM_Zone_") > 0)
      return true;
    else
      return false;
  }

  public String getZone() {
    String zone = "";
    int pos = projectionString.indexOf("UTM_Zone_");
    if (pos > 0) {
      zone = projectionString.substring(pos + 9, pos + 13);
      pos = zone.indexOf('"');
      if (pos > 0)
        zone = zone.substring(0, pos);
    }
    return zone;
  }

  public String getCentralMeridian() {
    String centralMeridian = "";
    int pos = projectionString.indexOf("Central_Meridian");
    if (pos > 0) {
      centralMeridian = projectionString.substring(pos + 18, pos + 24);
      pos = centralMeridian.indexOf("]");
      if (pos > 0)
        centralMeridian = centralMeridian.substring(0, pos);
    }
    return centralMeridian;
  }

  public void writeProjectionFile(PlugInContext context, String fileName) {
    try {
      FileUtil.setContents(fileName, projectionString);
    } catch (IOException ex) {
      context
          .getWorkbenchFrame()
          .getOutputFrame()
          .addText(
              "Could not write: " + fileName + " due to: " + ex.getMessage());
    }
  }
}
