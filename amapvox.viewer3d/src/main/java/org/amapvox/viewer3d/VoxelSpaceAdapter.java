/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.amapvox.viewer3d;

/**
 *
 * @author Julien Heurtebize (julienhtbe@gmail.com)
 */
public abstract class VoxelSpaceAdapter implements VoxelSpaceListener{

    @Override
    public void voxelSpaceCreationProgress(int progress) {}

    @Override
    public void voxelSpaceCreationFinished() {}
    
}
