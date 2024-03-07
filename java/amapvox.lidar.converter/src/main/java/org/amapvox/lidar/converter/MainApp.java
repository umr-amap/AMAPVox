package org.amapvox.lidar.converter;

import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class MainApp extends Application {

    public static String buildVersion;

    static {

        Logger logger = Logger.getLogger(MainApp.class.getCanonicalName());
        try {
            buildVersion = ResourceBundle.getBundle("version").getString("amapvox.version");
        } catch (Exception ex) {
            logger.log(Level.WARNING, "Failed to read application version", ex);
            buildVersion = "UNDEF";
        }
    }

    @Override
    public void start(Stage stage) throws Exception {

        FXMLLoader loader = new FXMLLoader(MainApp.class.getResource("fxml/Converter.fxml"));
        Parent root = loader.load();
        ConverterController controller = loader.getController();
        controller.setStage(stage);

        Scene scene = new Scene(root);

        stage.getIcons().addAll(
                new Image(MainApp.class.getResource("icons/lidar-converter_256x256.png").toExternalForm()),
                new Image(MainApp.class.getResource("icons/lidar-converter_128x128.png").toExternalForm()),
                new Image(MainApp.class.getResource("icons/lidar-converter_64x64.png").toExternalForm()));
        stage.setTitle("LiDAR Converter " + buildVersion);
        stage.setScene(scene);
        stage.show();
    }

    /**
     * The main() method is ignored in correctly deployed JavaFX application.
     * main() serves only as fallback in case the application can not be
     * launched through deployment artifacts, e.g., in IDEs with limited FX
     * support. NetBeans ignores main().
     *
     * @param args the command line arguments
     */
//    public static void main(String[] args) {
//        javafx.application.Application.launch(args);
//    }
}
