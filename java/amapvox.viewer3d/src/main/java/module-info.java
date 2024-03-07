
module amapvox.viewer3d {
    
    // java modules
    requires java.desktop;
    requires java.logging;
    
    // javafx module
    requires transitive javafx.fxml;
    
    // automatic modules
    requires vecmath;
    requires jogl.all;
    requires gluegen.rt;
    requires org.apache.commons.lang3;
    requires commons.math3;
    requires trove4j;
    requires org.controlsfx.controls;
    requires exp4j;
    
    // umr amap modules
    requires amapvox.core;
    requires amapvox.commons.util;
    requires amapvox.commons.math;
    requires amapvox.commons.spds;
    requires amapvox.commons.format;
    requires amapvox.commons.raytracing;
    requires amapvox.commons.raster;
    
    // javafx modules
    requires javafx.controls;
    
    // external modules
    
    
    // opens to javafx.fxml
    opens org.amapvox.viewer3d to javafx.fxml;
    
    // exports
    exports org.amapvox.viewer3d;
    exports org.amapvox.viewer3d.event;
    exports org.amapvox.viewer3d.loading.shader;
    exports org.amapvox.viewer3d.loading.texture;
    exports org.amapvox.viewer3d.mesh;
    exports org.amapvox.viewer3d.object.camera;
    exports org.amapvox.viewer3d.object.scene;
    exports org.amapvox.viewer3d.renderer;
    
}
