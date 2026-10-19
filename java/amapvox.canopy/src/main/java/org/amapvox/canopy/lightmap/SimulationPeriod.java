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
package org.amapvox.canopy.lightmap;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import org.amapvox.canopy.util.Time;

/**
 *
 * @author Julien Heurtebize, Philippe Verley
 */
public class SimulationPeriod {

    final private static SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    private final float clearnessCoefficient;
    private final Calendar startDate;
    private final Calendar endDate;

    public SimulationPeriod(Calendar startDate, Calendar endDate, float clearnessCoefficient) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.clearnessCoefficient = clearnessCoefficient;
    }

    public static SimulationPeriod parse(String startDateAsString, String endDateAsString, String clearnessCoefficientAsString) throws IOException {
        return new SimulationPeriod(
                toCalendar(startDateAsString),
                toCalendar(endDateAsString),
                Float.parseFloat(clearnessCoefficientAsString));
    }

    private static Calendar toCalendar(String dateAsString) throws IOException {
        Calendar calendar = Calendar.getInstance();
        try {
            calendar.setTime(DATE_FORMAT.parse(dateAsString));
            return calendar;
        } catch (ParseException ex) {
            StringBuilder sb = new StringBuilder();
            sb.append("Failed to parse date ")
                    .append(dateAsString)
                    .append(" from simulation period. ");
            sb.append("Should be ").append(DATE_FORMAT.getDateFormatSymbols().toString());
            throw new IOException(sb.toString(), ex);
        }
    }

    public float getClearnessCoefficient() {
        return clearnessCoefficient;
    }

    public String startDateToString() {
        return DATE_FORMAT.format(startDate.getTime());
    }

    public Time startDateToTime() {
        return toTime(startDate);
    }

    public String endDateToString() {
        return DATE_FORMAT.format(endDate.getTime());
    }

    public Time endDateToTime() {
        return toTime(endDate);
    }

    @Override
    public String toString() {
        return startDateToString() + " => " + endDateToString();
    }
    
    private Time toTime(Calendar calendar) {
        return new Time(
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.DAY_OF_YEAR),
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE));
    }

}
