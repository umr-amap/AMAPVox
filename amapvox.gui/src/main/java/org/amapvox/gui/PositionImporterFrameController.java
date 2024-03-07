/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.amapvox.gui;

import org.amapvox.commons.javafx.io.FileChooserContext;
import org.amapvox.voxelfile.VoxelFileHeader;
import org.amapvox.voxelfile.VoxelFileReader;
import org.amapvox.commons.javafx.io.TextFileParserFrameController;
import org.amapvox.voxelfile.VoxelFileVoxel;
import org.amapvox.voxelisation.output.OutputVariable;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import javafx.application.Platform;
import javafx.beans.binding.BooleanBinding;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javax.vecmath.Point3d;
import org.apache.log4j.Logger;

/**
 * FXML Controller class
 *
 * @author Julien
 */
public class PositionImporterFrameController implements Initializable {

    private FileChooserContext fileChooserOpenVoxelFile;
    private FileChooserContext fileChooserOpenPositionFile;
    private Stage stage;
    private Parent root;
    private TextFileParserFrameController textFileParserFrameController;

    private final static Logger logger = Logger.getLogger(PositionImporterFrameController.class);
    private float mnt[][];

    @FXML
    private TextField textfieldVoxelFile;
    @FXML
    private ListView<Point3d> listViewCanopyAnalyzerSensorPositions;
    @FXML
    private TextField textFieldXPosition;
    @FXML
    private TextField textFieldYPosition;
    @FXML
    private TextField textFieldZPosition;
    @FXML
    private TextField textfieldScannerHeightOffset;
    @FXML
    private TextField textfieldScannerSeedPosition;
    @FXML
    private Button buttonGenerateGridPosition;
    @FXML
    private ImageView imageViewLoading;
    @FXML
    private Button buttonValidatePosition;
    @FXML
    private Button buttonRemovePosition;

    public static PositionImporterFrameController newInstance() {

        PositionImporterFrameController controller = null;

        try {

            FXMLLoader loader = new FXMLLoader(PositionImporterFrameController.class.getResource("/org/amapvox/gui/fxml/PositionImporterFrame.fxml"));
            Parent root = loader.load();
            controller = loader.getController();
            controller.root = root;

        } catch (IOException ex) {
            java.util.logging.Logger.getLogger(PositionImporterFrameController.class.getName()).log(Level.SEVERE, "Cannot load PositionImporterFrame.fxml", ex);
        }

        return controller;
    }

    /**
     * Initializes the controller class.
     *
     * @param url
     * @param rb
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        textfieldVoxelFile.setOnDragOver(DragAndDropHelper.dragOverEvent);
        textfieldVoxelFile.setOnDragDropped((DragEvent event)
                -> {
            Dragboard db = event.getDragboard();
            boolean success = false;

            if (db.hasFiles() && db.getFiles().size() == 1) {
                success = true;
                db.getFiles().stream()
                        .filter(file -> (file != null))
                        .forEach(file -> {
                            textfieldVoxelFile.setText(file.getAbsolutePath());
                        });
            }

            event.setDropCompleted(success);
            event.consume();
        });

        fileChooserOpenVoxelFile = new FileChooserContext();
        fileChooserOpenPositionFile = new FileChooserContext();

        listViewCanopyAnalyzerSensorPositions.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        BooleanBinding noItemSelected = listViewCanopyAnalyzerSensorPositions.getSelectionModel().selectedItemProperty().isNull();
        buttonValidatePosition.disableProperty().bind(noItemSelected);
        buttonRemovePosition.disableProperty().bind(noItemSelected);

        textFileParserFrameController = TextFileParserFrameController.newInstance();
    }

    @FXML
    private void onActionButtonOpenVoxelFile(ActionEvent event) {
        File selectedFile = fileChooserOpenVoxelFile.showOpenDialog(stage);

        if (selectedFile != null) {
            textfieldVoxelFile.setText(selectedFile.getAbsolutePath());
        }
    }

    @FXML
    private void onActionMenuItemPositionsSelectionAll(ActionEvent event) {
        listViewCanopyAnalyzerSensorPositions.getSelectionModel().selectAll();
    }

    @FXML
    private void onActionMenuItemPositionsSelectionNone(ActionEvent event) {
        listViewCanopyAnalyzerSensorPositions.getSelectionModel().clearSelection();
    }

    @FXML
    private void onActionImportPositionsFromFile(ActionEvent event) {

        final File selectedFile = fileChooserOpenPositionFile.showOpenDialog(stage);

        if (selectedFile != null) {
            textFileParserFrameController.setColumnAssignment(true);
            textFileParserFrameController.setColumnAssignmentValues("X", "Y", "Z", "Ignore");
            textFileParserFrameController.setColumnAssignmentDefaultSelectedIndex(0, 0);
            textFileParserFrameController.setColumnAssignmentDefaultSelectedIndex(1, 1);
            textFileParserFrameController.setColumnAssignmentDefaultSelectedIndex(2, 2);
            textFileParserFrameController.setHeaderExtractionEnabled(false);

            try {
                textFileParserFrameController.setTextFile(selectedFile);
                Stage textFileParserFrame = textFileParserFrameController.getStage();
                textFileParserFrame.setOnHidden((WindowEvent wevent) -> {
                    String separator = textFileParserFrameController.getSeparator();
                    List<String> columnAssignment = textFileParserFrameController.getAssignedColumnsItems();
                    final int numberOfLines = textFileParserFrameController.getNumberOfLines();
                    final int headerIndex = textFileParserFrameController.getHeaderIndex();
                    int skipNumber = textFileParserFrameController.getSkipLinesNumber();
                    int xIndex = -1, yIndex = -1, zIndex = -1;
                    for (int i = 0; i < columnAssignment.size(); i++) {

                        String item = columnAssignment.get(i);

                        if (item != null) {
                            switch (item) {
                                case "X":
                                    xIndex = i;
                                    break;
                                case "Y":
                                    yIndex = i;
                                    break;
                                case "Z":
                                    zIndex = i;
                                    break;
                                default:
                            }
                        }
                    }
                    if (headerIndex != -1) {
                        skipNumber++;
                    }
                    final int finalSkipNumber = skipNumber;
                    final int finalXIndex = xIndex;
                    final int finalYIndex = yIndex;
                    final int finalZIndex = zIndex;
                    Service service = new Service() {

                        @Override
                        protected Task createTask() {
                            return new Task() {

                                @Override
                                protected Object call() throws Exception {

                                    final List<Point3d> positions = new ArrayList<>();

                                    BufferedReader reader;
                                    try {
                                        reader = new BufferedReader(new FileReader(selectedFile));

                                        String line;
                                        int count = 0;

                                        for (int i = 0; i < finalSkipNumber; i++) {
                                            reader.readLine();
                                        }

                                        while ((line = reader.readLine()) != null) {

                                            if (count == numberOfLines) {
                                                break;
                                            }

                                            String[] lineSplitted = line.split(separator);

                                            double x = 0, y = 0, z = 0;

                                            try {

                                                if (finalXIndex != -1) {
                                                    x = Double.valueOf(lineSplitted[finalXIndex]);
                                                }
                                                if (finalYIndex != -1) {
                                                    y = Double.valueOf(lineSplitted[finalYIndex]);
                                                }
                                                if (finalZIndex != -1) {
                                                    z = Double.valueOf(lineSplitted[finalZIndex]);
                                                }

                                                positions.add(new Point3d(x, y, z));
                                            } catch (Exception e) {
                                                logger.error("Cannot parse line " + (count + 1));
                                            }

                                            count++;
                                        }

                                        reader.close();

                                    } catch (FileNotFoundException ex) {
                                        logger.error("Cannot load point file", ex);
                                    } catch (IOException ex) {
                                        logger.error("Cannot load point file", ex);
                                    }

                                    Platform.runLater(() -> listViewCanopyAnalyzerSensorPositions.getItems().addAll(positions));
                                    return null;
                                }
                            };
                        }
                    };
                    service.start();
                });
                textFileParserFrame.show();
            } catch (IOException ex) {
                logger.error(ex);
            }
        }

    }

    @FXML
    private void onActionButtonValidatePosition(ActionEvent event) {
        stage.hide();
    }

    @FXML
    private void onActionButtonRemovePosition(ActionEvent event) {

        ObservableList<Point3d> selectedItems = listViewCanopyAnalyzerSensorPositions.getSelectionModel().getSelectedItems();
        listViewCanopyAnalyzerSensorPositions.getItems().removeAll(selectedItems);
    }

    @FXML
    private void onActionButtonImportPositions(ActionEvent event) {
        stage.close();
    }

    public Stage getStage() {
        if (null == stage) {
            stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Position Importer");
            stage.initModality(Modality.APPLICATION_MODAL);
        }
        return stage;
    }

    private List<Point3d> generateGridPositions() throws Exception {

        float step = Float.valueOf(textfieldScannerSeedPosition.getText());
        float zOffset = Float.valueOf(textfieldScannerHeightOffset.getText());

        final List<Point3d> positions = new ArrayList<>();

        VoxelFileReader reader = new VoxelFileReader(new File(textfieldVoxelFile.getText()));
        VoxelFileHeader header = reader.getHeader();
        Point3d voxSize = new Point3d();
        voxSize.x = (header.getMaxCorner().x - header.getMinCorner().x) / (double) header.getDimension().x;
        voxSize.y = (header.getMaxCorner().y - header.getMinCorner().y) / (double) header.getDimension().y;
        voxSize.z = (header.getMaxCorner().z - header.getMinCorner().z) / (double) header.getDimension().z;

        // allocate MNT
        logger.info("Initializes DTM");
        mnt = new float[header.getDimension().x][];
        for (int x = 0; x < header.getDimension().x; x++) {
            mnt[x] = new float[header.getDimension().y];
            for (int y = 0; y < header.getDimension().y; y++) {
                mnt[x][y] = 999999999;
            }
        }

        float[][][] groudDistances = new float[header.getDimension().x][header.getDimension().y][header.getDimension().z];
        Iterator<VoxelFileVoxel> iterator = reader.iterator();
        int groundDistanceColumn = reader.findColumn(OutputVariable.GROUND_DISTANCE);
        if (groundDistanceColumn < 0) {
            throw new IOException("[position importer] Output variable \"ground distance\" is missing");
        }
        while (iterator.hasNext()) {
            VoxelFileVoxel voxel = iterator.next();
            groudDistances[voxel.i][voxel.j][voxel.k] = Float.valueOf(voxel.variables[groundDistanceColumn]);
        }

        for (int i = 0; i < header.getDimension().x; i++) {

            for (int j = 0; j < header.getDimension().y; j++) {

                for (int k = header.getDimension().z - 1; k >= 0; k--) {

                    if ((groudDistances[i][j][k] > 0)) {

                        double posZ = header.getMinCorner().z + (header.getVoxelSize().z / 2.0d) + (k * header.getVoxelSize().z);
                        double diff = groudDistances[i][j][k];
                        mnt[i][j] = (float) (posZ - diff + (header.getVoxelSize().z / 2.0d));
                        break; // break inner loop on k
                    }
                }

            }
        }

        double middleX = header.getMinCorner().x + (header.getMaxCorner().x - header.getMinCorner().x);
        double middleY = header.getMinCorner().y + (header.getMaxCorner().y - header.getMinCorner().y);

        //calcul du x min
        double xWidth = (header.getMaxCorner().x - header.getMinCorner().x) / 2.0;
        int nbPossibleXStep = (int) (xWidth / step) * 2;
        double xStart = middleX - (nbPossibleXStep * step);

        double yWidth = (header.getMaxCorner().y - header.getMinCorner().y) / 2.0;
        int nbPossibleYStep = (int) (yWidth / step) * 2;
        double yStart = middleY - (nbPossibleYStep * step);

        for (int i = 0; i <= nbPossibleXStep; i++) {

            for (int j = 0; j <= nbPossibleYStep; j++) {

                double posX = xStart + i * step;
                double posY = yStart + j * step;

                int indiceX = (int) ((posX - header.getMinCorner().x) / header.getVoxelSize().x);
                int indiceY = (int) ((posY - header.getMinCorner().y) / header.getVoxelSize().y);

                if (indiceX == header.getDimension().x) {
                    indiceX--;
                }

                if (indiceY == header.getDimension().y) {
                    indiceY--;
                }

                positions.add(new Point3d(posX, posY, mnt[indiceX][indiceY] + zOffset));
            }
        }

        return positions;
    }

    @FXML
    private void onActionButtonGenerateGridPosition(ActionEvent event) {

        buttonGenerateGridPosition.setDisable(true);
        imageViewLoading.setVisible(true);

        Service s = new Service() {

            @Override
            protected Task createTask() {
                return new Task() {

                    @Override
                    protected Object call() throws Exception {

                        try {

                            List<Point3d> positions = generateGridPositions();

                            Platform.runLater(() -> {
                                listViewCanopyAnalyzerSensorPositions.getItems().addAll(positions);
                                buttonGenerateGridPosition.setDisable(false);
                                imageViewLoading.setVisible(false);
                            });

                        } catch (Exception e) {
                            logger.error(e);
                        }

                        return null;
                    }
                };
            }
        };

        s.start();

        s.setOnFailed((Event event1) -> {
            ErrorDialog.show(new Exception(s.getException()));
        });

    }

    public List<Point3d> getPositions() {

        return listViewCanopyAnalyzerSensorPositions.getItems();
    }

    @FXML
    private void onActionButtonAddSinglePosition(ActionEvent event) {

        try {
            Point3d position = new Point3d(Double.valueOf(textFieldXPosition.getText()),
                    Double.valueOf(textFieldYPosition.getText()),
                    Double.valueOf(textFieldZPosition.getText()));

            listViewCanopyAnalyzerSensorPositions.getItems().add(position);

        } catch (NumberFormatException e) {
            ErrorDialog.show(e);
        }
    }

    public void setInitialVoxelFile(File file) {
        textfieldVoxelFile.setText(file.getAbsolutePath());
    }

}
