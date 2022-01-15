/*
 * Library name : kml
 * (C) 2011 Larry Becker (ISA)
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
 */

package com.isa.jump.kml;

import com.vividsolutions.jump.workbench.plugin.Extension;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;

/**
 * This is the entry class to declare the kml driver to JUMP.
 * Put the .jar file containing the driver in your installation ext directory.
 */

public class KMLDriverConfiguration extends Extension {

  public static final com.vividsolutions.jump.I18N I18N = com.vividsolutions.jump.I18N.getInstance("com.isa.jump.kml");

  public void configure(PlugInContext context) {
    new KMLDataSourceQueryChooserInstallerPlugIn().initialize(context);
  }

  public String getName() {
    return I18N.get("driver-name");
  }

  public String getVersion() {
    return I18N.get("driver-version");
  }
}
