package org.amapvox.canopy.util;

/**
 * A time class.
 *
 * @author J. Dauzat - May 2012
 */
public class Time {

    public int year;
    public int doy; // Day of Year
    public int hour;
    public int minutes;
    public float hourDecimal;

    public Time() {
        //
    }

    public Time(int year, int doy, float hd) {
        this.year = year;
        this.doy = doy;
        this.hourDecimal = hd;
        hourMinutes(hd);
    }

    public Time(int year, int doy, int hour, int minutes) {
        this.year = year;
        this.doy = doy;
        this.hourDecimal = decimalHour(hour, minutes);
    }

    public Time(String codedString) throws Exception {
        String[] tokens = codedString.split("/");

        this.year = Integer.valueOf(tokens[0].trim());
        this.doy = Integer.valueOf(tokens[1].trim());
        this.hourDecimal = Float.valueOf(tokens[2].trim());
        hourMinutes(this.hourDecimal);
    }

    public static float decimalHour(int hour, int minutes) {
        return hour + (minutes / 60f);
    }

    public static float decimalHour(int hour, int minutes, int secunds) {
        return hour + (minutes / 60f) + (secunds / 3600f);
    }

    @Deprecated
    public static String hourMinute(float hourDecimal) {
        StringBuilder hm = new StringBuilder();
        int h = (int) hourDecimal;
        hm.append(h + "h");
        int m = (int) ((hourDecimal - h) * 60);
        hm.append(m);

        return hm.toString();
    }

    public void hourMinutes(float hourDecimal) {
        hour = (int) hourDecimal;
        minutes = (int) ((hourDecimal - hour) * 60);
    }

    @Override
    public String toString() {
        return "y: " + year + "\tdoy: " + doy + "\t" + hour + "h" + minutes;
    }

    public void setMinutes(int min) {
        this.minutes = min;
        this.hourDecimal = decimalHour(this.hour, this.minutes);
    }

}
