
/*
This software is distributed WITHOUT ANY WARRANTY and without even the
implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.

This program is open-source LGPL 3 (see copying.txt).
Authors:
    Gregoire Vincent    gregoire.vincent@ird.fr
    Julien Heurtebize   julienhtbe@gmail.com
    Jean Dauzat         jean.dauzat@cirad.fr
    Rémi Cresson        cresson.r@gmail.com

For further information, please contact Gregoire Vincent.
 */

package org.amapvox.commons.math.geometry;

import javax.vecmath.Point2f;


/**
 *
 * @author Julien Heurtebize (julienhtbe@gmail.com)
 */


public class BoundingBox2F {
    
    public Point2f min;
    public Point2f max;
    
    public BoundingBox2F(){
        min = new Point2f();
        max = new Point2f();
    }

    public BoundingBox2F(Point2f min, Point2f max) {
        this.min = min;
        this.max = max;
    }
    
    
}
