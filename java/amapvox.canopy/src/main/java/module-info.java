
module amapvox.canopy {
    
    // java modules
    requires java.desktop;
    
    // modular dependencies
    requires log4j;
    
    // amapvox modules
    requires amapvox.core;
    requires amapvox.commons.math;
    requires amapvox.commons.raytracing;
    requires amapvox.lidar.commons;
    requires amapvox.lidar.riegl;
    
    // exports
    exports org.amapvox.canopy;
    exports org.amapvox.canopy.hemi;
    exports org.amapvox.canopy.transmittance;
    exports org.amapvox.canopy.lai2xxx;
}
