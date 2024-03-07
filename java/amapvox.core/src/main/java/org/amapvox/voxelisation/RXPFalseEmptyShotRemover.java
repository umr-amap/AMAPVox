/*
 * Copyright (C) 2016 UMR AMAP (botAnique et Modélisation de l'Architecture des Plantes et des végétations.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 */
package org.amapvox.voxelisation;

import org.amapvox.lidar.riegl.RxpShot;
import java.util.Iterator;
import java.util.Stack;

/**
 *
 * @author Claudia Lavalley
 */
public class RXPFalseEmptyShotRemover implements Iterable<RxpShot>{
    
    private final Iterator<RxpShot> shotIterator;
    private final static double MIN_RANGE = 1.0; //1 meter
    private final static int MAX_SUCCESSIVE_EMPTY_SHOTS = 10;
    private final static double ANGLE_DIFF = Math.toDegrees(Math.PI/8);
    private int nbThrownShots = 0; // total number of thrown shots
    
    public RXPFalseEmptyShotRemover(Iterator<RxpShot> shotIterator){
        
        this.shotIterator = shotIterator;
    }

    @Override
    public Iterator<RxpShot> iterator() {
                       
        return new Iterator<RxpShot>() {
                
            double lastShotElevation = Double.NaN;
            RxpShot currentShot;
            boolean isNextShotCalled;
            Stack<RxpShot> stack = new Stack<>();
            boolean isLastEchoNear = false;
            boolean isStackValid = false;
            RxpShot prevShot = null;
            boolean changeOfColumn = false;
            int nbMaxStacksKept = 0;  // number of empty shots stacks of max size that have been kept
            
            private double getShotElevation(RxpShot shot){
                
                shot.calculateAngle();
                return shot.angle;
            }
            
            private boolean changeOfCol(RxpShot currShot){
                
                boolean chOfCol = false;
                
                if(prevShot != null){
                    double prevElevation = getShotElevation(prevShot);
                    double currElevation = getShotElevation(currShot);
                    chOfCol = (Math.abs(prevElevation - currElevation) > ANGLE_DIFF); 
                } 
                
                prevShot = new RxpShot(currShot);
                
                return chOfCol;
            }
            
            private void throwStack(){
                isStackValid = false;
                nbThrownShots += stack.size();
                stack = new Stack<>();
            }
                        
            private RxpShot getNextShot(){
                
                if(isStackValid && !stack.empty()){ //elements of the stack are good to keep
                    return stack.pop();
                }else if(isStackValid){  //stack of empty shots is empty
                    isStackValid = false;
                }
                
                RxpShot shot = shotIterator.next();
                
                if(shot == null){ //the shot iterator reached the end
                    return null;
                }
                
                if ( changeOfCol(shot) || (stack.size() > MAX_SUCCESSIVE_EMPTY_SHOTS) ){  //changeOfCol updates prevShot as currentShot
                        isStackValid = true;
                        //ajout 03/05
                        if (shot.nEcho != 0){
                            isLastEchoNear = (shot.ranges[0] < MIN_RANGE);
                        }
                        if (stack.size() > MAX_SUCCESSIVE_EMPTY_SHOTS){
                            nbMaxStacksKept++;
                        }
                        //
                        return shot; // return every first Shot empty or not, keeps stack of Empty shots, if any
                }
                else{
                    if(shot.nEcho == 0){//empty shot
                        stack.push(shot);  //add shot into stack
                        return getNextShot();
                    }               
                    else{ //non empty shot
                        
                        double range = shot.ranges[0];
                        boolean nearEchoDetected = isLastEchoNear;

                        isLastEchoNear = (range < MIN_RANGE);

                        if(isLastEchoNear){  
                            if(nearEchoDetected){
                                if (nbMaxStacksKept < 1){ // if stack size is not to be added to already kept empty shots stacks.... 
                                    throwStack();
                                }
                            }
                            else
                                isStackValid = true;
                        }
                        else{
                            isStackValid = true;
                        }
                        nbMaxStacksKept = 0;
                        return shot;
                    }
                }
                
            }
            
            @Override
            public boolean hasNext() {
                
                currentShot = getNextShot();
                
                isNextShotCalled = true;
                    
                if(currentShot == null){
                    return false;
                }else{
                    return true;
                }
            }

            @Override
            public RxpShot next() {
             
               if(isNextShotCalled){
                   isNextShotCalled = false;
                   return currentShot;
               }else{
                   return getNextShot();
               }
            }
        };
    }

    public int getNbThrownShots() {
        return nbThrownShots;
    }
}