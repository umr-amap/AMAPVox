
module amapvox.lidar.laszip {
    
    // java modules
    requires java.logging;
    
    // umr amap modules
    requires amapvox.commons.util;
    
    // automatic modules
    requires com.sun.jna;
    
    exports org.amapvox.lidar.las;
    exports org.amapvox.lidar.laszip;
}
