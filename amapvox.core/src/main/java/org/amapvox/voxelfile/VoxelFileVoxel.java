/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.amapvox.voxelfile;

/**
 *
 * @author Philippe VERLEY <philippe.verley@ird.fr>
 */
public class VoxelFileVoxel {

    public final int i, j, k;
    public final String[] variables;

    public VoxelFileVoxel(int i, int j, int k, String[] variables) {
        this.i = i;
        this.j = j;
        this.k = k;
        this.variables = variables;
    }

}
