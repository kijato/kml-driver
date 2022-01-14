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

package com.isa.jump.plugin;

import java.io.File;
import java.util.List;
import java.util.ArrayList;
import com.vividsolutions.jump.util.FileUtil;
import java.io.IOException;

import com.vividsolutions.jump.workbench.Logger;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;

public class UTM_Projection_List {

  private final ArrayList<UTM_Projection> UTM_Projections;

  public UTM_Projection_List(File file) {
    UTM_Projections = new ArrayList<>();

    if (file.exists()) {
      try {
        List<String> projectionStrings = FileUtil.getContents(file.toURI());
        for (String projectionString : projectionStrings) {
          UTM_Projections.add(new UTM_Projection(projectionString));
        }
      } catch (IOException ex) {
        Logger.error(ex);
      }
    }
  }

  public String getZone(double latitude, double longitude) {
    // there are two exceptions to the equations below: Norway and Svalbard
    // per LDB/RFL (8/10/05) we will ignore them as we do not expect to have
    // to handle any maps from those areas.

    String zone;
    double zoneDec = (longitude + 180.0) / 6.0;
    int zoneInt = (int) zoneDec;

    if (zoneDec - zoneInt > 0)
      zoneInt++;
    if (zoneInt <= 0)
      zoneInt = 1;
    if (zoneInt > 60)
      zoneInt = 60;

    if (latitude >= 0)
      zone = zoneInt + "N";
    else
      zone = zoneInt + "S";

    return zone;
  }

  public UTM_Projection getProjection(String zone) {
    for (UTM_Projection utmProjection : UTM_Projections) {
      if (zone.equals(utmProjection.getZone())) {
        return utmProjection;
      }
    }

    return null;
  }

  public String getCentralMeridian(String zone) {
    for (UTM_Projection utmProjection : UTM_Projections) {
      if (zone.equals(utmProjection.getZone())) {
        return utmProjection.getCentralMeridian();
      }
    }

    return null;
  }

  public static void createMultiStringFile(PlugInContext context,
      String inputFile, String outputFile) {
    if (new File(inputFile).exists()) {
      try {
        List<String> fileList = FileUtil.getContents(inputFile);
        String fileName = fileList.get(0);
        List<String> projStringList = FileUtil.getContents(fileName);

        for (int i = 1; i < fileList.size(); i++) {
          fileName = fileList.get(i);
          List<String> fileContents = FileUtil.getContents(fileName);
          projStringList.add(fileContents.get(0));
        }

        FileUtil.setContents(outputFile, projStringList);
      } catch (IOException ex) {
        context.getWorkbenchFrame().getOutputFrame().addText(ex.getMessage());
      }
    }
  }
}
