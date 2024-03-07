
module amapvox.lidar.gridded {
    
    // java modules
    requires java.logging;
    
    // umr amap modules
    requires amapvox.commons.math;
    requires amapvox.commons.util;
    requires amapvox.lidar.commons;
    
    // external modules
    requires org.apache.poi.poi;
    
    // automatic modules
    requires vecmath;
    requires commons.math3;    
    
    exports org.amapvox.lidar.gridded;
    exports org.amapvox.lidar.faro;
    exports org.amapvox.lidar.leica.ptx;
    exports org.amapvox.lidar.leica.ptg;
    
}
