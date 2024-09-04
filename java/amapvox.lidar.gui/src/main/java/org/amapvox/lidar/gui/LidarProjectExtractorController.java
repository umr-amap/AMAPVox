/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.amapvox.lidar.gui;

import org.amapvox.lidar.commons.LidarScan;
import org.amapvox.commons.javafx.NodeGestures;
import org.amapvox.commons.javafx.PannableCanvas;
import org.amapvox.commons.javafx.SceneGestures;
import org.amapvox.commons.javafx.SelectableMenuButton;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Service;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBoxTreeItem;
import javafx.scene.control.CheckBoxTreeItem.TreeModificationEvent;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.control.cell.CheckBoxTreeCell;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javax.vecmath.Point3d;

/**
 * FXML Controller class
 *
 * @author calcul
 */
public class LidarProjectExtractorController implements Initializable {

    @FXML
    private SelectableMenuButton selectMenuButton;
    @FXML
    private TreeView<LidarScan> treeView;
    @FXML
    private ProgressBar progressBar;
    @FXML
    private Label labelMsg;
    @FXML
    private Button btnOK;
    @FXML
    private Button btnVisualize;
    @FXML
    private Button btnCancel;

    final TreeItem<LidarScan> root = new TreeItem();
    private Stage stage;
    private Service service;

    public void setScanSelected(boolean selected) {
        root.getChildren().stream().forEach(item -> ((CheckBoxTreeItem) item).setSelected(selected));
    }

    @FXML
    private void onActionButtonOK(ActionEvent event) {
        stage.close();
    }

    @FXML
    private void onActionButtonCancel(ActionEvent event) {
        service.cancel();
        setScanSelected(false);
        stage.close();
    }

    @FXML
    private void onActionButtonVisualize(ActionEvent event) {

        List<LidarScan> selectedScans = getSelectedScans();

        double minX = 0, minY = 0, minZ = 0;
        double maxX = 0, maxY = 0, maxZ = 0;

        for (int i1 = 0; i1 < selectedScans.size(); i1++) {

            LidarScan scan = selectedScans.get(i1);

            double x1 = scan.getMatrix().m03;
            double y1 = scan.getMatrix().m13;
            double z1 = scan.getMatrix().m23;

            if (i1 == 0) {
                minX = x1;
                minY = y1;
                minZ = z1;

                maxX = x1;
                maxY = y1;
                maxZ = z1;
            } else {

                minX = Double.min(x1, minX);
                minY = Double.min(y1, minY);
                minZ = Double.min(z1, minZ);

                maxX = Double.max(x1, maxX);
                maxY = Double.max(y1, maxY);
                maxZ = Double.max(z1, maxZ);
            }
        }

        Point3d minPosition = new Point3d(minX, minY, minZ);
        Point3d maxPosition = new Point3d(maxX, maxY, maxZ);

        int resolution = 10; //ratio meter/pixel

        int padding = 30;
        double sceneWidth = (Math.abs(maxPosition.x - minPosition.x) * resolution) + padding;
        double sceneHeight = (Math.abs(maxPosition.y - minPosition.y) * resolution) + padding;

        Stage stageTest = new Stage();

        // create canvas
        PannableCanvas canvas = new PannableCanvas();
        //canvas.setTranslateX(minPosition.x + sceneWidth/2.0);
        //canvas.setTranslateY(-minPosition.y + sceneHeight/2.0);

        // we don't want the canvas on the top/left in this example => just
        // translate it a bit
        // create sample nodes which can be dragged
        NodeGestures nodeGestures = new NodeGestures(canvas);

        //searching same scanner locations
        for (int i1 = 0; i1 < selectedScans.size(); i1++) {

            LidarScan scan = selectedScans.get(i1);

            double x1 = scan.getMatrix().m03;
            double y1 = scan.getMatrix().m13;
            double z1 = scan.getMatrix().m23;

            Point3d position = new Point3d(x1, y1, z1);

            Point3d canvasPosition = new Point3d(position);

            //on espace les positions pour plus de lisibilité
            canvasPosition.x *= resolution;
            canvasPosition.y *= resolution;

            //l'écriture du fichier vectoriel inverse la direction de l'axe y conventionnel
            canvasPosition.y = -canvasPosition.y;

            //on centre la position 0 au milieu de la scène
            canvasPosition.x += sceneWidth / 2.0;
            canvasPosition.y += sceneHeight / 2.0;

            Circle circle1 = new Circle(canvasPosition.x, canvasPosition.y, 5);
            
            if (position.x == 0 && position.y == 0 && position.z == 0) { //reference scan
                circle1.setStroke(Color.RED);
                circle1.setFill(Color.RED.deriveColor(1, 1, 1, 0.5));
            } else {
                circle1.setStroke(Color.ORANGE);
                circle1.setFill(Color.ORANGE.deriveColor(1, 1, 1, 0.5));
            }

            //circle1.addEventFilter( MouseEvent.MOUSE_PRESSED, nodeGestures.getOnMousePressedEventHandler());
            //circle1.addEventFilter( MouseEvent.MOUSE_DRAGGED, nodeGestures.getOnMouseDraggedEventHandler());
            canvas.getChildren().add(circle1);

            Label label1 = new Label(scan.getName() + " (" + ((Math.round(position.x * 10000)) / 10000.0)
                    + " " + ((Math.round(position.y * 10000)) / 10000.0)
                    + " " + ((Math.round(position.z * 10000)) / 10000.0) + ")");

            label1.setTranslateX(canvasPosition.x);
            label1.setTranslateY(canvasPosition.y);
            label1.addEventFilter(MouseEvent.MOUSE_PRESSED, nodeGestures.getOnMousePressedEventHandler());
            label1.addEventFilter(MouseEvent.MOUSE_DRAGGED, nodeGestures.getOnMouseDraggedEventHandler());

            canvas.getChildren().add(label1);
        }

        /*int minCanvasPositionX = (int) ((minPosition.x*resolution) + sceneWidth/2.0);
            int maxCanvasPositionX = (int) ((maxPosition.x*resolution) + (sceneWidth/2.0));

            int minCanvasPositionY = (int) ((-(minPosition.y*resolution)) + (sceneHeight/2.0));
            int maxCanvasPositionY = (int) ((-(maxPosition.y*resolution)) + (sceneHeight/2.0));

            canvas.addGrid(minCanvasPositionX, maxCanvasPositionY,
                    maxCanvasPositionX - minCanvasPositionX,
                    minCanvasPositionY - maxCanvasPositionY, resolution);*/
        // create scene which can be dragged and zoomed
        Scene scene = new Scene(canvas, sceneWidth, sceneHeight);

        SceneGestures sceneGestures = new SceneGestures(canvas);
        scene.addEventFilter(MouseEvent.MOUSE_PRESSED, sceneGestures.getOnMousePressedEventHandler());
        scene.addEventFilter(MouseEvent.MOUSE_DRAGGED, sceneGestures.getOnMouseDraggedEventHandler());
        scene.addEventFilter(ScrollEvent.ANY, sceneGestures.getOnScrollEventHandler());

        stageTest.setScene(scene);
        stageTest.setResizable(true);

        stageTest.widthProperty().addListener((ObservableValue<? extends Number> observableValue, Number number, Number number2) -> {
            stageTest.setWidth(500);
        });

        stageTest.heightProperty().addListener((ObservableValue<? extends Number> observableValue, Number number, Number number2) -> {
            stageTest.setHeight(500);
        });

        stageTest.hide();
        stageTest.show();

    }

    /**
     * Initializes the controller class.
     *
     * @param url
     * @param rb
     */
    @Override
    @FXML
    public void initialize(URL url, ResourceBundle rb) {

        treeView.setCellFactory(CheckBoxTreeCell.<LidarScan>forTreeView());
        treeView.setRoot(root);
        treeView.setShowRoot(false);
//        treeView.setCellFactory((TreeView<LidarScan> p) -> new CheckBoxTreeCell<LidarScan>() {
//            @Override
//            public void updateItem(LidarScan value, boolean empty) {
//                super.updateItem(value, empty);
//                if (null != value) {
//                    setText(value.getName() + " (" + value.getFile().getName() + ")");
//                }
//            }
//        });

        selectMenuButton.setSelected(true);

        root.addEventHandler(
                CheckBoxTreeItem.<LidarScan>checkBoxSelectionChangedEvent(),
                (TreeModificationEvent<LidarScan> e) -> {
                    int nscan = getSelectedScans().size();
                    if (nscan == root.getChildren().size()) {
                        selectMenuButton.setIndeterminate(false);
                        selectMenuButton.setSelected(true);
                    } else if (nscan == 0) {
                        selectMenuButton.setIndeterminate(false);
                        selectMenuButton.setSelected(false);
                    } else {
                        selectMenuButton.setIndeterminate(true);
                    }
                });

        // select all
        selectMenuButton.setOnActionAll(event -> setScanSelected(true));
        // select none
        selectMenuButton.setOnActionNone(event -> setScanSelected(false));

        BooleanBinding selectedBinding = Bindings.or(selectMenuButton.selectedProperty(), selectMenuButton.indeterminateProperty());
        btnOK.disableProperty().bind(selectedBinding.not());
        btnVisualize.disableProperty().bind(selectedBinding.not());
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public SelectableMenuButton getSelectMenuButton() {
        return selectMenuButton;
    }

    public List<LidarScan> getSelectedScans() {

        return root.getChildren().stream()
                .filter(treeItem -> ((CheckBoxTreeItem) treeItem).isSelected())
                //                .map(treeItem -> treeItem.getValue())
                .map(TreeItem::getValue)
                .collect(Collectors.toList());

    }

    public Button getBtnCancel() {
        return btnCancel;
    }

    public void setDisable(boolean value) {
        treeView.setDisable(value);
        selectMenuButton.setDisable(value);
//        btnOK.setDisable(value);
//        btnVisualize.setDisable(value);
    }

    public TreeView<LidarScan> getTreeView() {
        return treeView;
    }

    public TreeItem<LidarScan> getRoot() {
        return root;
    }

    public void setService(Service service) {
        this.service = service;
        progressBar.progressProperty().unbind();
        progressBar.progressProperty().bind(this.service.progressProperty());
        labelMsg.textProperty().bind(this.service.messageProperty());
    }
}
