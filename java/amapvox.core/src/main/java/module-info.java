
module amapvox.core {
    
    // java modules
    requires java.logging;
    requires java.desktop;
    
    // umr amap modules
    requires amapvox.commons.util;
    requires amapvox.commons.math;
    requires amapvox.commons.raster;
    requires amapvox.commons.spds;
    requires amapvox.commons.raytracing;
    requires amapvox.lidar.commons;
    requires amapvox.lidar.gridded;
    requires amapvox.lidar.riegl;
    
    // external modules 
    requires log4j;
    
    // automatic modules
    requires commons.math3; 
    requires sis.jhdf5;
    requires cdm;
    requires laszip4j;
    
    // exports
    exports org.amapvox.commons;
    exports org.amapvox.deprecated;
    exports org.amapvox.shot;
    exports org.amapvox.shot.filter;
    exports org.amapvox.shot.weight;
    exports org.amapvox.voxelfile;
    exports org.amapvox.voxelisation;
    exports org.amapvox.voxelisation.gridded;
    exports org.amapvox.voxelisation.las;
    exports org.amapvox.voxelisation.output;
    exports org.amapvox.voxelisation.postproc;
    exports org.amapvox.voxelisation.txt;
    
}
