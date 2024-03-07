package org.amapvox.gui;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.Locale;
import java.util.ResourceBundle;
import javafx.application.Application;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.amapvox.canopy.hemi.HemiPhotoCfg;
import org.amapvox.canopy.lai2xxx.CanopyAnalyzerCfg;
import org.amapvox.canopy.transmittance.TransmittanceCfg;
import org.amapvox.deprecated.ButterflyCfg;
import org.amapvox.deprecated.CroppingCfg;
import org.amapvox.gui.chart.ChartConfiguration;
import org.amapvox.gui.viewer3d.Viewer3dConfiguration;
import org.amapvox.voxelisation.VoxelizationCfg;
import org.amapvox.voxelisation.postproc.ObjExporterCfg;
import org.amapvox.deprecated.MergingCfg;

public class MainFX extends Application {

    @Override
    public void start(Stage stage) throws Exception {

        ResourceBundle rb = ResourceBundle.getBundle("bundle_help",
                Locale.ENGLISH, new URLClassLoader(new URL[]{MainFX.class.getResource("strings/")}));
        FXMLLoader loader = new FXMLLoader(MainFX.class.getResource("fxml/MainFrame.fxml"), rb);
        Parent root = loader.load();
        MainFrameController controller = loader.getController();
        ObservableList<Screen> screens = Screen.getScreensForRectangle(0, 0, 10, 10);

        if (screens != null && !screens.isEmpty()) {
            stage.setWidth(screens.get(0).getBounds().getWidth());
            stage.setHeight(screens.get(0).getBounds().getHeight());
        }

        Scene scene = new Scene(root);
        scene.getStylesheets().add(MainFX.class.getResource("styles/Styles.css").toExternalForm());

        stage.setTitle("AMAPVox " + org.amapvox.commons.Util.getVersion());
        stage.setScene(scene);
        stage.getIcons().addAll(new Image(MainFX.class.getResource("icons/amapvox-icon_256x256.png").toExternalForm()),
                new Image(MainFX.class.getResource("icons/amapvox-icon_128x128.png").toExternalForm()),
                new Image(MainFX.class.getResource("icons/amapvox-icon_64x64.png").toExternalForm()));

        controller.setStage(stage);

        // add UI tasks
        controller.addTaskUI(HemiPhotoCfg.class,
                "fxml/configuration/HemiPhotoFrame.fxml",
                "fxml/icons/hemispherical.png",
                RepoStatus.INACTIVE);
        controller.addTaskUI(CanopyAnalyzerCfg.class,
                "fxml/configuration/CanopyAnalyzerFrame.fxml",
                "fxml/icons/lai2200.png",
                RepoStatus.INACTIVE);
        controller.addTaskUI(ObjExporterCfg.class,
                "fxml/configuration/ObjExporterFrame.fxml",
                "fxml/icons/obj.png",
                RepoStatus.INACTIVE);
        controller.addTaskUI(TransmittanceCfg.class,
                "fxml/configuration/TransmittanceMapFrame.fxml",
                "fxml/icons/sun.png",
                RepoStatus.INACTIVE);
        controller.addTaskUI(VoxelizationCfg.class,
                "fxml/configuration/VoxelizationFrame.fxml",
                "fxml/icons/voxelization.png",
                RepoStatus.ACTIVE);
        controller.addTaskUI(ChartConfiguration.class,
                "fxml/configuration/ChartFrame.fxml",
                "fxml/icons/charts.png",
                RepoStatus.INACTIVE);
        controller.addTaskUI(Viewer3dConfiguration.class,
                "fxml/configuration/Viewer3dFrame.fxml",
                "fxml/icons/cubes.png",
                RepoStatus.ACTIVE);
        controller.addTaskUI(ButterflyCfg.class,
                null,
                "fxml/icons/butterfly.png",
                RepoStatus.MOVED);
        controller.addTaskUI(CroppingCfg.class,
                null,
                "fxml/icons/gtk-cut.png",
                RepoStatus.MOVED);
        controller.addTaskUI(MergingCfg.class,
                null,
                "fxml/icons/merging.png",
                RepoStatus.MOVED);

        stage.addEventFilter(WindowEvent.WINDOW_HIDDEN, (WindowEvent event) -> {
            System.exit(0);
        });

        stage.show();
    }
}
