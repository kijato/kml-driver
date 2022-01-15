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

import com.vividsolutions.jump.coordsys.Geographic;
import com.vividsolutions.jump.coordsys.Planar;
import com.vividsolutions.jump.coordsys.Projection;
import com.vividsolutions.jump.coordsys.Spheroid;
import com.vividsolutions.jump.coordsys.impl.TransverseMercator;
import org.locationtech.jts.util.Assert;


public class UniversalTransverseMercator extends Projection {

    private final static double SCALE_FACTOR = 0.9996;
    private final static double FALSE_EASTING = 500000.0;
    private final static double FALSE_NORTHING = 10000000.0;
    
    private final TransverseMercator transverseMercator = new TransverseMercator();

    public UniversalTransverseMercator() { }

    private int zone = -1;
    private boolean zoneSouth = false;
  
  	public void setParameters(int zone, boolean zoneSouth, double centralMeridian) 
  	{
  	    Assert.isTrue(zone <= 60, "UTM zone " + zone + " not supported");
  	    Assert.isTrue(zone >= 1, "UTM zone " + zone + " not supported");
  	    transverseMercator.setParameters(centralMeridian);
  	    this.zone = zone;
  	    this.zoneSouth = zoneSouth;
    }

    public void setSpheroid(Spheroid s) {
      transverseMercator.setSpheroid(s);
    }

    public Geographic asGeographic(Planar p, Geographic q) {

      Assert.isTrue(zone != -1, "Call #setParameters first");
      	
      p.x = (p.x - FALSE_EASTING) / SCALE_FACTOR;
      double falseNorthing = (zoneSouth) ? FALSE_NORTHING : 0;
      p.y = (p.y - falseNorthing) / SCALE_FACTOR;
      transverseMercator.asGeographic(p, q);
      return q;
    }

    public Planar asPlanar(Geographic q0, Planar p) {

      Assert.isTrue(zone != -1, "Call #setParameters first");

      transverseMercator.asPlanar(q0, p);
      p.x = SCALE_FACTOR * p.x + FALSE_EASTING;
      double falseNorthing = (q0.lat<0) ? FALSE_NORTHING : 0;
      p.y = SCALE_FACTOR * p.y + falseNorthing;
      return p;
    }

  }
