package org.amapvox.canopy.mmr;

/**
 * Methods for calculating the sky brightness.
 * 
 * @author J. Dauzat - May 2012
 */
public class Sky {

	
	/**
	 * Brightness of Standard OverCast sky (SOC) at a given elevation (normalized over brightness at
	 * zenith) (ref. Anderson...)
	 * 
	 * @param elevation over the horizon (radians)
	 * @return brightness at elevation / brightness at zenith
	 */
	public static float brightnessNormSOC (float elevation) {
		return (float) ((1f + 2f * Math.sin (elevation)) / 3f);
	}

	public static float brightnessSOC (float diffuse, float elevation) {
		if (elevation < 0.01) return 0;
		float brightness = (float) ((1f + 2f * Math.sin (elevation)) / 3f);

		// brightness *= diffuse / 2.3687458;
		// brightness *= diffuse / 2.284617;
		brightness *= diffuse / 2.366345925976101;
		return brightness;
	}

	/**
	 * Brightness of Clear Sky (normalized over brightness at zenith)
	 * 
	 * @param zenith zenith angle in sky [radians]
	 * @param azim azimuth in sky [radians]
	 * @param sun_zen sun elevation [radians]
	 * @param sun_az sun azimuth [radians]
	 * @return brightness at sky position / brightness at zenith
	 */
	public static float brightnessNormClear (float zenith, float azim, float sun_zen, float sun_az) {
		double gamma;
		float brightness;
		if (zenith > (Math.PI / 2) - 0.1) return 0;

		float cosSunZen = (float) Math.cos (sun_zen);
		float cosZenith = (float) Math.cos (zenith);
		gamma = cosZenith * cosSunZen;
		gamma += Math.sin (zenith) * Math.sin (sun_zen) * Math.cos (azim - sun_az);

		brightness = (float) (0.91 + 10 * Math.exp (-3.0 * Math.acos (gamma)));
		brightness += 0.45 * (gamma * gamma);
		brightness *= 1. - Math.exp (-0.32 / cosZenith);
		brightness /= (0.91 + (10 * Math.exp (-3.0 * sun_zen)) + (0.45 * cosSunZen * cosSunZen));
		brightness /= 0.27385; // Normalization over zenith brightness

		return brightness;
	}

	public static float brightnessClear (float diffuse, float zenith, float azimuth, float sunElevation, float sunAzimuth) {
		if (diffuse == 0) return 0;
		float brightness = brightnessNormClear (zenith, azimuth, sunElevation, sunAzimuth);
		/**/
		double[] normFactors = {0.908277553, 0.950170952, 0.994007432, 1.039864475, 1.087820496, 1.13795346, 1.190341995, 1.245065172, 1.302199459, 1.361816544, 1.423977327,
				1.488719141, 1.55602343, 1.625727993, 1.697450423, 1.770957081, 1.846457758, 1.924102308, 2.00385241, 2.085646636, 2.169549182, 2.255718735, 2.344301531,
				2.435362201, 2.528814177, 2.62422568, 2.720852055, 2.818721806, 2.918391859, 3.020059804, 3.123645664, 3.228952498, 3.33580061, 3.44407372, 3.553657542,
				3.66436607, 3.775966032, 3.888282147, 4.001270352, 4.115011601, 4.229630301, 4.34518001, 4.461542482, 4.578354406, 4.695054397, 4.81120834, 4.926762948,
				5.041822055, 5.15639698, 5.270396402, 5.383681632, 5.496085726, 5.607390047, 5.717219176, 5.824868049, 5.929357342, 6.030240676, 6.127903748, 6.222726624,
				6.314766683, 6.403923611, 6.490042974, 6.572844602, 6.651809989, 6.726288322, 6.79595522, 6.860963364, 6.92152575, 6.97765434, 7.02923691, 7.07620127,
				7.118599775, 7.156577868, 7.190289482, 7.219771279, 7.244132175, 7.258168602, 7.264670405, 7.266772108, 7.264883611, 7.259166761, 7.249763062, 7.236810469,
				7.220356907, 7.199506135, 7.171678565, 7.139630583, 7.10471686, 7.067271139, 7.027517496, 6.985658711,};
		/**/
		double[] normFactors2 = {0.900819726, 0.94256221, 0.986249192, 1.031958334, 1.079767103, 1.12975041, 1.181980935, 1.236527958, 1.293453708, 1.352810764, 1.414637812,
				1.478955311, 1.545765898, 1.61506015, 1.686840618, 1.761137908, 1.838005705, 1.917484415, 1.99941645, 2.081869256, 2.165381027, 2.251210433, 2.339448431,
				2.430067558, 2.522966404, 2.617975865, 2.714949921, 2.813856511, 2.914740922, 3.017597829, 3.122319601, 3.228742766, 3.336752204, 3.446278234, 3.55716475,
				3.669022399, 3.78131709, 3.893827485, 4.006779385, 4.120472023, 4.235073956, 4.350609413, 4.466963374, 4.583876661, 4.700928765, 4.817555386, 4.933232167,
				5.04781837, 5.161527729, 5.274508583, 5.386580723, 5.497260319, 5.606093578, 5.712987125, 5.818030826, 5.921108308, 6.021761321, 6.119379553, 6.213613987,
				6.304535202, 6.392400158, 6.477443352, 6.559784561, 6.639331965, 6.715515766, 6.786803032, 6.851619293, 6.910712779, 6.965237856, 7.015541585, 7.061590293,
				7.103231336, 7.140275825, 7.172520988, 7.199709328, 7.221409045, 7.237050821, 7.246487395, 7.250285146, 7.249124042, 7.243406694, 7.233320756, 7.218969113,
				7.200411663, 7.177638905, 7.150650411, 7.119697941, 7.085301701, 7.047991974, 7.008178218, 6.96616831};

		int zenDeg = (int) (Math.toDegrees (sunElevation) + 0.1);
		if (zenDeg > 90) return 0;
		if (zenDeg < 0) return 0;

		// System.out.print (" "+zenDeg+" ");
		brightness *= diffuse / normFactors2[zenDeg];

		return brightness;
	}

	public static float brightnessNorm (float diffuse, float global, float zenith, float azim, float sun_zen, float sun_az) {
		float elevation = (float) ((Math.PI / 2f) - zenith);

		float coeffSOC = SolarRadiation.socInDiffuseHourly (diffuse / global, elevation);
		float coeffClear = 1f - coeffSOC;
		float brightness = coeffSOC * brightnessNormSOC (elevation);
		brightness += (coeffClear * brightnessNormClear (zenith, azim, sun_zen, sun_az));
		brightness *= diffuse;
		return brightness;
	}

	public static float brightness (float diffuse, float global, float zenith, float azim, float sun_zen, float sun_az) {
		if (zenith > Math.PI * 0.499) return 0;

		float elevation = (float) ((Math.PI / 2f) - zenith);

		float coeffSOC = SolarRadiation.socInDiffuseHourly (diffuse / global, elevation);
		// if (coeffSOC > 1) System.out.println ("ALLLEEERRRRTT");
		float coeffClear = 1f - coeffSOC;
		// System.out.println ("Clear: "+coeffClear+"\tSOC: "+coeffSOC);
		float brightness = 0;
		brightness += brightnessSOC (coeffSOC * diffuse, elevation);
		brightness += brightnessClear (coeffClear * diffuse, zenith, azim, sun_zen, sun_az);

		// if (brightness<0) System.out.println ("brightness= "+brightness);
		return brightness;
	}

}
