/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package org.amapvox.shot.weight;

import java.io.IOException;
import org.amapvox.shot.Echo;
import org.amapvox.shot.Shot;
import org.amapvox.voxelisation.VoxelizationCfg;

/**
 *
 * @author pverley
 */
public abstract class EchoWeight {
    
    abstract public void init(VoxelizationCfg cfg) throws IOException;

    abstract public void setWeight(Shot shot);

    abstract public double getWeight(Echo echo);

}
