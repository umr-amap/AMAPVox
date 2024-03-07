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

import java.text.DateFormat;
import java.util.Calendar;

/**
 *
 * @author calcul
 */


public class Period {
    
    public Calendar startDate;
    public Calendar endDate;
    
    @Override
    public String toString(){
        
        return getDate(startDate)+"_"+getDate(endDate);
    }
    
    public static String getDate(Calendar c){
        DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
        return dateFormat.format(c.getTime());/*
        return c.get(Calendar.DAY_OF_MONTH) + "/"+c.get(Calendar.MONTH)+"/"+c.get(Calendar.YEAR)+
                " "+c.get(Calendar.HOUR_OF_DAY)+":"+c.get(Calendar.MINUTE);*/
    }
}
