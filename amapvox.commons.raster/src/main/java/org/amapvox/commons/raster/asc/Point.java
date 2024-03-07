
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

package org.amapvox.commons.raster.asc;

import java.util.ArrayList;
import java.util.List;
import javax.vecmath.Point3f;

/**
 *
 * @author Julien Heurtebize (julienhtbe@gmail.com)
 */


public class Point extends Point3f {
    
    /**
     * Référence vers les faces composée de ce point
     */
    public List<Integer> faces;
    
    public Point(float x, float y, float z) {
        
        super(x, y, z);
        faces = new ArrayList<>();
    }
}
