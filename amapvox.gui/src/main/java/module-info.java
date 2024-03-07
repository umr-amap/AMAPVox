module amapvox.gui {
    
    // java modules
    requires java.datatransfer;
    requires java.logging;
    requires transitive java.desktop;
    
    // javafx modules
    requires transitive javafx.base;
    requires transitive javafx.controls;
    requires transitive javafx.fxml;
    requires transitive javafx.graphics;
    
    // amapvox modules
    requires amapvox.commons.raytracing;
    requires amapvox.commons.spds;
    requires amapvox.lidar.commons;
    requires amapvox.lidar.gridded;
    requires amapvox.lidar.gui;
    requires amapvox.lidar.laszip;
    requires amapvox.lidar.riegl;
    requires transitive amapvox.canopy;
    requires transitive amapvox.commons.javafx;
    requires transitive amapvox.commons.math;
    requires transitive amapvox.commons.raster;
    requires transitive amapvox.commons.util;
    requires transitive amapvox.core;
    requires transitive amapvox.viewer3d;
    
    // external modules
    requires log4j;
    requires org.controlsfx.controls;
    requires org.jfree.jfreechart;
    requires vecmath;
    
    // opens to javafx.fxml
    opens org.amapvox.gui to javafx.fxml;
    opens org.amapvox.gui.configuration to javafx.fxml;
    opens org.amapvox.gui.task to javafx.fxml;
    opens org.amapvox.gui.logging to log4j;
    opens org.amapvox.gui.chart to amapvox.core;
    opens org.amapvox.gui.viewer3d to amapvox.core;
    
    // exports
    exports org.amapvox.gui;

}
