/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.amapvox.commons.util;

/**
 *
 * @author Julien Heurtebize (julienhtbe@gmail.com)
 */
public class PairingFunction {
    
    public static int generateID(int nb1, int nb2){
        
        int id=(int) (0.5*(nb1+nb2)*(nb1+nb2+1)+nb2);

	return id;
    }
    
    public static int[] getIDsFromID(int z){
        
        int w,t,x,y;

	w=(int) Math.floor((Math.sqrt((8*z)+1)-1)/2);
	t=((w*w)+w)/2;
	y=z-t;
	x=w-y;
        
        int[] result = new int[2];
        result[0] = x;
        result[1] = y;

	return result;
    }
}
