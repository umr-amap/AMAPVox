

module amapvox.commons.javafx {
    
    // java modules
    requires java.desktop;
    requires java.logging;
    
    // umr amap modules
    requires amapvox.commons.math;
    requires amapvox.commons.util;
    
    // automatic modules
    requires vecmath;
    requires org.apache.pdfbox;
    requires org.jfree.jfreechart;
    requires org.jfree.fxgraphics2d;
    requires org.jfree.jfreesvg;
    
    // javafx modules
    requires javafx.base;
    requires javafx.controls;
    requires javafx.graphics;
    requires javafx.fxml;
    
    // opens commons-javafx modules for fxml
    opens org.amapvox.commons.javafx to javafx.fxml;
    opens org.amapvox.commons.javafx.io to javafx.fxml;
    opens org.amapvox.commons.javafx.matrix to javafx.fxml;    
    
    // exports commons-javafx
    exports org.amapvox.commons.javafx;
    exports org.amapvox.commons.javafx.chart;
    exports org.amapvox.commons.javafx.io;
    exports org.amapvox.commons.javafx.matrix;
}
