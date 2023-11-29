

module amapvox.lidar.riegl {
    
    // java modules
    requires java.logging;
    
    // umr amap modules
    requires amapvox.commons.util;
    requires amapvox.lidar.commons;
    
    // automatic modules
    requires vecmath;
    requires me.tongfei.progressbar;
    requires com.sun.jna;
    requires org.jdom2;
    
    exports org.amapvox.lidar.riegl;
}
