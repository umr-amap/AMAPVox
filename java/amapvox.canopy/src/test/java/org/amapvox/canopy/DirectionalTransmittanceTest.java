
package org.amapvox.canopy;


/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


import org.amapvox.canopy.LeafAngleDistribution;
import org.amapvox.canopy.GTheta;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author calcul
 */
public class DirectionalTransmittanceTest {
    
    public DirectionalTransmittanceTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of buildTable method, of class GTheta.
     */
    public void testBuildTable() {
    }

    /**
     * Test of getGThetaFromAngle method, of class GTheta.
     */
    @Test
    public void testGetTransmittanceFromAngle() throws URISyntaxException, FileNotFoundException, IOException {
        
        //read directions file
        File directionsFile = new File(GTheta.class.getResource("/directions.txt").toURI());
        
        double[] directions = getValues(directionsFile);
        
        //read lad files
        double[] erectoValues = getValues(new File(GTheta.class.getResource("/gtheta_dart_erectophile.txt").toURI()));
        double[] planoValues = getValues(new File(GTheta.class.getResource("/gtheta_dart_planophile.txt").toURI()));
        double[] extremoValues = getValues(new File(GTheta.class.getResource("/gtheta_dart_extremophile.txt").toURI()));
        double[] plagioValues = getValues(new File(GTheta.class.getResource("/gtheta_dart_plagiophile.txt").toURI()));
        double[] twoBetaValues = getValues(new File(GTheta.class.getResource("/gtheta_dart_two_beta_1.13_1.62.txt").toURI()));
        
        double delta = 0.05;
        
        GTheta erecto = new GTheta(
                new LeafAngleDistribution(LeafAngleDistribution.Type.ERECTOPHILE));
        
        GTheta plano = new GTheta(
                new LeafAngleDistribution(LeafAngleDistribution.Type.PLANOPHILE));
        
        GTheta extremo = new GTheta(
                new LeafAngleDistribution(LeafAngleDistribution.Type.EXTREMOPHILE));
        
        GTheta plagio = new GTheta(
                new LeafAngleDistribution(LeafAngleDistribution.Type.PLAGIOPHILE));
        
        GTheta twoBeta = new GTheta(
                new LeafAngleDistribution(LeafAngleDistribution.Type.TWO_PARAMETER_BETA, 1.13, 1.62));
                
        for(int i=0;i<directions.length;i++){
            
            double direction = directions[i];
            
            double erectoValue = erecto.getGThetaFromAngle(direction, true);
            assertEquals(erectoValues[i], erectoValue, delta);
            
            double planoValue = plano.getGThetaFromAngle(direction, true);
            assertEquals(planoValues[i], planoValue, delta);
            
            double extremoValue = extremo.getGThetaFromAngle(direction, true);
            assertEquals(extremoValues[i], extremoValue, delta);
            
            double plagioValue = plagio.getGThetaFromAngle(direction, true);
            assertEquals(plagioValues[i], plagioValue, delta);
            
            double twoBetaValue = twoBeta.getGThetaFromAngle(direction, true);
            assertEquals(twoBetaValues[i], twoBetaValue, delta);
        }
        
    }
    
    private double[] getValues(File file) throws IOException{
        
        BufferedReader reader = new BufferedReader(new FileReader(file));
        
        double[] values = new double[7];
        
        String line;
        int lineID = 0;
        
        while((line = reader.readLine()) != null){
            
            values[lineID] = Double.valueOf(line);
            lineID++;
        }
        
        reader.close();
        
        return values;
    }
    
}
