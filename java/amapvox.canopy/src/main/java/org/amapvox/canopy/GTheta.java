/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.amapvox.canopy;

import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.analysis.integration.TrapezoidIntegrator;

/**
 * <p>Get the directional transmittance for a specified Leaf Angle Distribution and angle.</p>
 * <h3><u>References:</u></h3>
 *      -Wang W. M., Li Z.-L. and Su H.-B., 2007,
 *          Comparison of leaf angle distribution functions: 
 *          effects on extinction coefficient and sunlit foliage, 
 *          Agricultural and Forest Meteorology, 2007, Vol. 143, NO. 1-2, pp. 106-122.
 * 
 * @author Julien Heurtebize
 */
public class GTheta {
    
    private final LeafAngleDistribution distribution;
    private double[] pdfArray; //probability density function array
    private int nbIntervals;
    private double[] serie_angulaire;
    private double SOM;
    
    private double[] transmittanceFunctions;
    private double res;
    private boolean isBuildingTable;
    
    public static int MIN_STEP_NUMBER = 91;
    public static int DEFAULT_STEP_NUMBER = 181;
    
    /**
     * Case where |cot(theta)*cot(thetaL)| &gt; 1
     */
    private class CustomFunction1 implements UnivariateFunction{

        private final double thetaRad;
        
        public CustomFunction1(double thetaRad){
            this.thetaRad = thetaRad;
        }
        
        @Override
        public double value(double x) {
            return Math.cos(thetaRad)*Math.cos(x);
        }
        
    }
    
    /**
     * Case where |cot(theta)*cot(thetaL)| &lt; 1
     */
    private class CustomFunction2 implements UnivariateFunction{

        private final double thetaRad;
        
        public CustomFunction2(double thetaRad){
            this.thetaRad = thetaRad;
        }
        
        @Override
        public double value(double x) {
            
            double tmp = (Math.tan(thetaRad)*Math.tan(x));
            
            //patch to avoid acos(1.000000000001), causing psi result equals to NaN
            if(tmp < 1){
                tmp = 1;
            }
            
            double psi = Math.acos(1/tmp);

            double result = Math.cos(thetaRad) * Math.cos(x) * (1 + (2 / Math.PI) * (Math.tan(psi) - psi));
            
            return result;
        }
        
    }
    
    
    /**
     * Constructs a new DirectionalTransmittance object with the specified Leaf Angle Distribution
     * @param leafAngleDistribution The Leaf Angle Distribution (LAD)
     */
    public GTheta(LeafAngleDistribution leafAngleDistribution){
        
        this.distribution = leafAngleDistribution;
        
    }
    
    /**
     * Generate the probability density array.
     * @param stepNumber Number of intervals
     */
    private void setupDensityProbabilityArray(int stepNumber){
                
        
        //contains angles from 0 to 90°
        serie_angulaire = new double[stepNumber];
        
        double step = (Math.round((90/(float)(stepNumber-1))*100))/100.0;
        double totalStep = 0;
        
        for(int i = 0; i < stepNumber ; i++){
            
            if(i == 0){
                serie_angulaire[i] = Math.toRadians((Math.round(0.001*1000))/1000.0);
            }else{
                serie_angulaire[i] = Math.toRadians((Math.round(totalStep*1000))/1000.0);
            }
            
            totalStep += step;
        }
        
        nbIntervals = serie_angulaire.length-1;
        
        serie_angulaire[nbIntervals] = Math.toRadians(90) - 0.00000001; //integration failed on the last interval (patch)
        
        //calcul du tableau de densités ou probabilités
        pdfArray = new double[serie_angulaire.length];
        for(int i = 0 ; i < pdfArray.length ; i++){
            
            pdfArray[i] = distribution.getDensityProbability(serie_angulaire[i]);
            SOM += pdfArray[i];
        }
    }
    
    /**
     * Computes a Look Up Table of transmittance functions (optional operation, speeds up processing time)
     * @param stepNumber must be greater or equals than 91
     */
    public void buildTable(int stepNumber){
        
        isBuildingTable = true;
        
        setupDensityProbabilityArray(stepNumber);
        
        res = 90.0 / stepNumber;
        
        double step = (Math.round((90/(float)(stepNumber-1))*100))/100.0;
        double totalStep = 0;
        
        transmittanceFunctions = new double[stepNumber];
        
        for(int i = 0 ; i < stepNumber ; i++){
            transmittanceFunctions[i] = getGThetaFromAngle(totalStep, true);
            totalStep += step;
        }
        
        isBuildingTable = false;
    }
    
    /**
     * <p>Get the transmittance from the specified angle (radians or degrees) and the specified Leaf Angle Distribution.</p>
     * 0° is vertical, 90° is horizontal (zenithal angle, measured from vertical).
     * @param theta Angle
     * @param degrees true if the given angle is in degrees, false otherwise
     * @return directional transmittance (GTheta)
     */
    public double getGThetaFromAngle(double theta, boolean degrees){
        
        if(degrees){
            if(theta > 90){ //get an angle between 0 and 90°
                theta = 180 - theta;
            }
        }else{
            if(theta > (Math.PI/2.0)){ //get an angle between 0 and pi/2
                theta = Math.PI - theta;
            }
        }

        if(transmittanceFunctions != null && !isBuildingTable){ //a table was built
            
            int indice = 0;
            if(degrees){
                indice = (int) (theta/res);
            }else{
                indice = (int) (Math.toDegrees(theta)/res);
            }
            
            if(indice >= transmittanceFunctions.length){
                indice = transmittanceFunctions.length -1;
            }else if(indice < 0){
                indice = 0;
            }
            
            return transmittanceFunctions[indice];
            
        }else{ //no table was built, get transmittance on the fly
            
            if(pdfArray == null){
                setupDensityProbabilityArray(DEFAULT_STEP_NUMBER);
            }
            if (distribution.getType() == LeafAngleDistribution.Type.SPHERIC) {

                return 0.5; //the result for spherical distribution is always 0.5, saving processing time

            } else {

                if (degrees) {
                    theta = Math.toRadians(theta);
                }

                if (theta == 0) {
                    theta = Double.MIN_VALUE;
                }

                if (theta >= Math.PI / 2.0) {
                    theta = (Math.PI / 2.0)-0.00001;
                }

                UnivariateFunction function1 = new CustomFunction1(theta);
                UnivariateFunction function2 = new CustomFunction2(theta);

                TrapezoidIntegrator integrator = new TrapezoidIntegrator();

                double sum = 0;
                for (int j = 0; j < nbIntervals; j++) {

                    double thetaL = (serie_angulaire[j] + serie_angulaire[j + 1]) / 2.0d;
                    double Fi = (pdfArray[j]) / SOM;

                    double cotcot = Math.abs(1 / (Math.tan(theta) * Math.tan(thetaL)));

                    double Hi;

                    if (cotcot > 1 || Double.isInfinite(cotcot)) {
                        Hi = integrator.integrate(10000, function1, serie_angulaire[j], serie_angulaire[j + 1]);
                    } else {
                        Hi = integrator.integrate(10000, function2, serie_angulaire[j], serie_angulaire[j + 1]);
                        //System.out.println("nb evaluations: " + integrator.getEvaluations());
                    }

                    double Gi = Fi * Hi / ((Math.PI / 2) / (double) serie_angulaire.length); //because we need the average value not the actual integral value!!!!
                    sum += Gi;
                }

                return sum;
            }
        }
        
    }
    
    public static void main(String[] args) {
        
       
        LeafAngleDistribution distribution = new LeafAngleDistribution(LeafAngleDistribution.Type.EXTREMOPHILE);
        
        GTheta dirTrans = new GTheta(distribution);
        //dirTrans.buildTable(180);
        
        double densityProbability = dirTrans.getGThetaFromAngle(Math.toRadians(0), false);
        double densityProbability2 = dirTrans.getGThetaFromAngle(Math.toRadians(180), false);
        
        System.out.println("test");
    }
}