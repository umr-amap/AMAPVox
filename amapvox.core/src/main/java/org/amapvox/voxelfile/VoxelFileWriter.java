/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.amapvox.voxelfile;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;

/**
 *
 * @author Julien Heurtebize
 */
public class VoxelFileWriter {
    
    public static void write(VoxelFile voxelspace, File outputFile) throws IOException{
        
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile))) {
            writer.write(voxelspace.getHeader().toString()+"\n");
            
            for (Iterator it = voxelspace.voxels.iterator(); it.hasNext();) {
                VoxelFileVoxel voxel = (VoxelFileVoxel) it.next();
                writer.write(voxel+"\n");
            }
        }
    }
}
