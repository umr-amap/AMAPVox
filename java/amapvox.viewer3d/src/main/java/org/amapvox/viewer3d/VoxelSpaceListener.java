/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.amapvox.viewer3d;

import java.util.EventListener;

/**
 *
 * @author Julien Heurtebize (julienhtbe@gmail.com)
 */
public interface VoxelSpaceListener extends EventListener{
    
    void voxelSpaceCreationProgress(int progress);
    void voxelSpaceCreationFinished();
}
