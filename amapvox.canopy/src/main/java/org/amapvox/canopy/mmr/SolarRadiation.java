package org.amapvox.canopy.mmr;

import org.amapvox.canopy.util.Time;

/**
 * A utility class for computing the components of solar radiation.
 *
 * @author J. Dauzat - May 2012
 */
public class SolarRadiation {

    /**
     * Partitioning of global into direct and diffuse components. Can be used
     * for periods of one hour or shorter (from de Jong 1980, cited by Spitters
     * et al., 1986)
     *
     * @param ir in Wm-2
     * @param clearness index: global / extra-terrestrial radiation;
     * @param sunElevation in radians
     */
    public static void globalPartitioningHourly(IncidentRadiation ir,
            float clearness, float sunElevation) {
        float R, K;
        if (ir.global <= 0) {
            ir.direct = ir.diffuse = 0;
            return;
        }
        if (sunElevation <= 0.0) {
            ir.diffuse = ir.global;
            ir.direct = 0;
            return;
        }

        if (clearness <= 0.22) {
            ir.diffuse = ir.global;
            ir.direct = 0;
            return;
        }
        if (clearness <= 0.35) {
            ir.diffuse = (float) (ir.global * (1. - (6.4 * (clearness - 0.22) * (clearness - 0.22))));
            ir.direct = ir.global - ir.diffuse;
            return;
        }

        R = diffuseInGlobalHourlyClear(sunElevation);
        K = (float) ((1.47 - R) / 1.66);

        if (clearness <= K) {
            ir.diffuse = (float) (ir.global * (1.47 - (1.66 * clearness)));
            ir.direct = ir.global - ir.diffuse;
            return;
        } else {
            ir.diffuse = ir.global * R;
            ir.direct = ir.global - ir.diffuse;
            return;
        }
    }

    /**
     * Partitioning of global into direct and diffuse components. Can be used
     * for periods of one day or more (from de Jong 1980, cited by Spitters et
     * al., 1986)
     *
     * @param ir ir
     * @param clearness index: daily global:daily extra-terrestrial radiation
     */
    static public void globalPartitioningDaily(IncidentRadiation ir,
            float clearness) {
        if (clearness < 0.07) {
            ir.diffuse = ir.global;
        } else if (clearness < 0.35) {
            ir.diffuse = ir.global * (1 - 2.3f * (clearness - 0.07f));
        } else if (clearness < 0.75) {
            ir.diffuse = ir.global * (1.33f - 1.46f * clearness);
        } else {
            ir.diffuse = ir.global * 0.23f;
        }

        ir.direct = ir.global - ir.diffuse;
    }

    /**
     * Computes directional global fluxes in turtle sectors
     *
     * @param ir ir
     * @param sun sun
     * @param turtle turtle
     */
    static public void globalInTurtle(IncidentRadiation ir, Sun sun, Turtle turtle) {

        float[] turtleDirect;
        float[] turtleDiffuse = new float[turtle.directions.length];
        ir.directionalGlobals = new float[turtle.directions.length];

        if (ir.global > 0) {

            float directDir = (float) (ir.direct / Math.cos(sun.zenith));
            turtleDirect = Sun.directInTurtle(directDir, sun.direction, turtle); // TODO
            float totalDirect = 0;
            float totalDiffuse = 0;

            for (int d = 0; d < turtle.directions.length; d++) {
                float zenith = (float) turtle.getZenithAngle(d);
                float azim = (float) turtle.getAzimuthAngle(d);
                turtleDiffuse[d] = Sky.brightnessNorm(ir.diffuse, ir.global,
                        zenith, azim, sun.zenith, sun.azimuth);
                float coeff = (float) Math.cos(zenith);
                // convert to flux as measured on horizontal plane
                turtleDirect[d] *= coeff;
                turtleDiffuse[d] *= coeff;
                totalDirect += turtleDirect[d];
                totalDiffuse += turtleDiffuse[d];
            }

            for (int d = 0; d < turtle.directions.length; d++) {
                turtleDiffuse[d] *= ir.diffuse / totalDiffuse;
                if (totalDirect > 0) {
                    turtleDirect[d] *= ir.direct / totalDirect;
                }

                ir.directionalGlobals[d] = turtleDirect[d] + 0.0f + turtleDiffuse[d];
            }
        }
    }

    public static IncidentRadiation globalTurtleIntegrate(Turtle t, float latitudeRadian, float clearness, Time time1, Time time2) {

        IncidentRadiation ir = new IncidentRadiation(t.directions.length);
        ir.setDirections(t.directions);
        
        Sun sun = new Sun();

        int doy;
        int doy1 = time1.doy;
        int doy2 = time2.doy;
        float hd1, hd2;
        for (doy = doy1; doy <= doy2; doy++) {

            if (doy == doy1) {
                hd1 = time1.hourDecimal;
            } else {
                hd1 = 0f;
            }
            if (doy == doy2) {
                hd2 = time2.hourDecimal;
            } else {
                hd2 = 24f;
            }

            float timeStep = 0.5f;
            float duration = timeStep;

            for (float h = hd1; h < hd2; h += timeStep) {
                
                duration = Math.min(timeStep, hd2 - h);
                float hd = h + (duration / 2);
                sun.position(latitudeRadian, doy, hd);
                float globalMJ = clearness
                        * SolarRadiation.extraTerrestrialHourly(latitudeRadian,
                                doy, h, h + duration);
                
                if ((globalMJ > 0) && (duration > 0)) {
                    IncidentRadiation radi = new IncidentRadiation(ir.getSize());
                    radi.global = globalMJ * 1000000 / (duration * 3600);
                    globalPartitioningHourly(radi, clearness, sun.elevation);
                    globalInTurtle(radi, sun, t);
                    globalCumulateMJ(ir, radi, duration);
                }
            }
        }

        return ir;
    }

    /**
     * Assess proportion of SOC in diffuse radiation by comparing
     * (diffuse/global) to diffuseGlobalHourlyClear (the ratio is between R, for
     * clear sky, and 1, for overcast sky (SOC)
     *
     * @param diffuseGlobalRatio diffuse global ratio
     * @param sunElevation (radians)
     * @return ratio (diffuse SOC : diffuse total)
     */
    public static float socInDiffuseHourly(float diffuseGlobalRatio,
            float sunElevation) {
        float fractionSoc;

        if (sunElevation <= 0) {
            fractionSoc = 0.5f;
            return fractionSoc;
        }

        float R = diffuseInGlobalHourlyClear(sunElevation);
        fractionSoc = (diffuseGlobalRatio - R) / (1 - R);
        fractionSoc = Math.max(fractionSoc, 0);
        // System.out.print("\tdiffuse/global= "+diffuseGlobalRatio+"\tR= "+
        // R+"\t"+"fraction SOC "+fractionSoc+"\t");

        return fractionSoc;
    }

    /**
     * Assess proportion of SOC in diffuse radiation by comparing
     * (diffuse/global) to diffuseGlobalDailyClear ((diffuse/global) is 0.23 for
     * clear sky and 1 for overcast sky (SOC)
     *
     * @param ir ir
     * @return ratio (diffuse SOC : diffuse total)
     */
    public static float socInDiffuseDaily(IncidentRadiation ir) {
        float fractionSOC = ((ir.diffuse / ir.global) - 0.23f) / (1 - 0.23f);
        fractionSOC = Math.max(fractionSOC, 0);

        return fractionSOC;
    }

    /**
     * R is the the ratio (diffuse:global) under clear sky conditions (from de
     * Jong 1980, cited by Spitters et al., 1986)
     *
     * @param sunElevation (radians)
     * @return R= diffuse / global
     */
    private static float diffuseInGlobalHourlyClear(float sunElevation) {
        double sinSunEl = Math.sin(sunElevation);
        float R = (float) (0.847 - (1.61 * sinSunEl) + (1.04 * sinSunEl * sinSunEl));
        return R;
    }

    public static void globalDirectDiffuseDaily(IncidentRadiation ir, int doy,
            float clearness, float latitude) {
        ir.global = clearness * extraTerrestrialDaily(latitude, doy);
        globalPartitioningDaily(ir, clearness);
    }

    /**
     * Daily incident solar radiation above the atmosphere
     *
     * @param latitude latitude
     * @param doy Day of Year
     * @return incident flux in MJ m-2
     */
    public static float extraTerrestrialDaily(float latitude, int doy) {
        float solarCste = 0.0820f; // in MJ m-2 min-1 (<=> SolarCste= 1367 in J
        // m-2 s-1)
        double doyAngle = 2 * Math.PI * doy / 365f;
        double declination = 0.409 * Math.sin(doyAngle - 1.39); // !!!
        double sun_earth = 1 + (0.033 * Math.cos(doyAngle)); // inverse relative
        // distance
        // double sunSetHourAngle= -Math.tan(latitude)*Math.tan(declination);
        // sunSetHourAngle= Math.max(-1, sunSetHourAngle);
        // sunSetHourAngle= Math.min( 1, sunSetHourAngle);
        // sunSetHourAngle= Math.acos(sunSetHourAngle);
        double sunsetHourAngle = Sun.sunsetHourAngle(latitude,
                (float) declination);

        double extra_rad;
        extra_rad = sunsetHourAngle * Math.sin(latitude)
                * Math.sin(declination);
        extra_rad += Math.cos(latitude) * Math.cos(declination)
                * Math.sin(sunsetHourAngle);
        extra_rad *= (24 * 60 * solarCste * sun_earth / Math.PI);

        return (float) extra_rad;
    }

    /**
     * Hourly (or shorter time laps) incident solar radiation above the
     * atmosphere
     *
     * @param latitudeRadian (degrees)
     * @param doy Day of Year
     * @param time1 begin of period in decimal hour
     * @param time2 : end of period in decimal hour
     * @return incident flux in MJ m-2
     */
    public static float extraTerrestrialHourly(float latitudeRadian, int doy,
            float time1, float time2) {
        double b = 2 * Math.PI * (doy - 81) / 364.;

        // Sc: seasonal correction for solar time [hour]
        double Sc = (0.1645 * Math.sin(2 * b)) - (0.1255 * Math.cos(b))
                - (0.025 * Math.sin(b));
        double doyAngle = 2 * Math.PI * doy / 365.;
        double declination = 0.409 * Math.sin(doyAngle - 1.39); // !!!

        double sunSetHAngle = Sun.sunsetHourAngle(latitudeRadian,
                (float) declination);
        // solar time angles
        double sTA1 = (Math.PI / 12.) * (time1 + Sc - 12);
        double sTA2 = (Math.PI / 12.) * (time2 + Sc - 12);

        sTA1 = Math.max(sTA1, -sunSetHAngle);
        sTA2 = Math.min(sTA2, sunSetHAngle);
        if (sTA2 - sTA1 <= 0) {
            return 0;
        }

        double solarCste = 0.0820; // in MJ m-2 min-1 (<=> SolarCste= 1367 in J
        // m-2 s-1)
        double sun_earth = 1 + (0.033 * Math.cos(doyAngle)); // inverse relative
        // distance

        double extra_rad;
        extra_rad = (sTA2 - sTA1) * Math.sin(latitudeRadian)
                * Math.sin(declination);
        extra_rad += Math.cos(latitudeRadian) * Math.cos(declination)
                * (Math.sin(sTA2) - Math.sin(sTA1));
        extra_rad *= (12 * 60 * solarCste * sun_earth / Math.PI);

        return (float) extra_rad;
    }

    public static void globalCumulateMJ(IncidentRadiation ir1,
            IncidentRadiation ir2, float durationHd) {
        
        // transform Watt s-1 m-2 to MJ m-2 (note: Watt= Joule s-1)
        float factor = durationHd * 3600 / 1000000;
        ir1.global += factor * ir2.global;
        ir1.direct += factor * ir2.direct;
        ir1.diffuse += factor * ir2.diffuse;

        if (ir2.global > 0) {

            for (int dir = 0; dir < ir1.directionalGlobals.length; dir++) {
                ir1.directionalGlobals[dir] += factor * ir2.directionalGlobals[dir];
                ir1.directionalGlobals[dir] += 0.0f;
            }

        }

        // if (ir2.globalTurtle[0] != null)
        // {
        // if (this.globalTurtle==null)
        // {
        // this.globalTurtle= new Float [ir2.globalTurtle.length];
        // for (int dir=0; dir<this.globalTurtle.length; dir++)
        // this.globalTurtle[dir]= 0f;
        // }
        //
        // for (int dir=0; dir<globalTurtle.length; dir++)
        // {
        // if ((ir2.globalTurtle[dir]!=null)&&(ir2.globalTurtle[dir]>0)){
        // this.globalTurtle[dir] += factor * ir2.globalTurtle[dir];
        // }
        // }
        // }
    }

    /**
     * This method computes irradiances of 46 directions of diffuse light. The
     * direct irradiance is stored in 47 directions of turtle
     *
     * @param ir ir
     * @param sun sun
     * @param turtle turtle
     */
    static public void diffuseDirectInTurtle(IncidentRadiation ir, Sun sun,
            Turtle turtle) {
        float[] turtleDiffuse = new float[turtle.directions.length];
        ir.directionalGlobals = new float[turtle.directions.length + 1];
        if (sun.elevation > 0) {
            ir.directionalGlobals[turtle.directions.length] = (float) (ir.direct);
        } // ir.directionalGlobals[turtle.directions.length] = (float) (ir.direct/
        // Math.cos (sun.zenith));
        else {
            ir.directionalGlobals[turtle.directions.length] = 0;
        }

        if (ir.global > 0) {

            float totalDiffuse = 0;
            for (int d = 0; d < turtle.directions.length; d++) {
                float zenith = (float) turtle.getZenithAngle(d);
                float azim = (float) turtle.getAzimuthAngle(d);
                turtleDiffuse[d] = Sky.brightnessNorm(ir.diffuse, ir.global,
                        zenith, azim, sun.zenith, sun.azimuth);
                float coeff = (float) Math.cos(zenith);

                turtleDiffuse[d] *= coeff;
                totalDiffuse += turtleDiffuse[d];
            }
            for (int d = 0; d < turtle.directions.length; d++) {
                turtleDiffuse[d] *= ir.diffuse / totalDiffuse;
                ir.directionalGlobals[d] = turtleDiffuse[d];
            }
        }
    }

}
