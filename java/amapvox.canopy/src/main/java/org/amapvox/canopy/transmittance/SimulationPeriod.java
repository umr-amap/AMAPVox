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

package org.amapvox.canopy.transmittance;


/**
 *
 * @author Julien Heurtebize
 */


public class SimulationPeriod {
    
    private final Period period;
    private final float clearnessCoefficient;

    public SimulationPeriod(Period period, float clearnessCoefficient) {
        
        this.period = period;
        this.clearnessCoefficient = clearnessCoefficient;
    }
    
    public Period getPeriod() {
        return period;
    }

    public float getClearnessCoefficient() {
        return clearnessCoefficient;
    }
    
}
