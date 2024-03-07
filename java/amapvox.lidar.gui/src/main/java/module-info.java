
module amapvox.lidar.gui {
    
    // java modules
    requires java.desktop;
    requires java.logging;
    
    // umr amap modules
    requires amapvox.commons.math;
    requires amapvox.commons.util;
    requires amapvox.commons.javafx;
    requires amapvox.lidar.commons;
    requires amapvox.lidar.gridded;
    requires amapvox.lidar.riegl;
    
    // automatic modules
    requires vecmath;
    
    // javafx modules
    requires javafx.base;
    requires javafx.controls;
    requires javafx.fxml;
    
    // opens controller and resources to FXML
    opens org.amapvox.lidar.gui to javafx.fxml;
    
    // exports
    exports org.amapvox.lidar.gui;
}
