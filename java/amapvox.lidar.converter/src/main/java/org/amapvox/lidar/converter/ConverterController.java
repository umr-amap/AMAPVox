package org.amapvox.lidar.converter;

import org.amapvox.lidar.riegl.RSPReader;
import org.amapvox.commons.util.io.file.FileManager;
import org.amapvox.lidar.gui.LidarProjectExtractor;
import org.amapvox.lidar.gui.MultiScanProjectExtractor;
import org.amapvox.lidar.gui.PTXProjectExtractor;
import org.amapvox.lidar.gui.RiscanProjectExtractor;
import org.amapvox.lidar.commons.LidarScan;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javax.vecmath.Matrix4d;
import org.amapvox.lidar.leica.ptg.PTGScan;
import org.controlsfx.dialog.ProgressDialog;
import org.controlsfx.validation.ValidationResult;
import org.controlsfx.validation.ValidationSupport;
import org.controlsfx.validation.Validator;

public class ConverterController implements Initializable {

    private FileChooser inputFileChooser;
    private DirectoryChooser outputDirectoryChooser;
    private Stage stage;

    LidarProjectExtractor riscanProjectExtractor = new RiscanProjectExtractor();
    LidarProjectExtractor ptxProjectExtractor = new PTXProjectExtractor();
//    LidarProjectExtractor xybProjectExtractor = new MultiScanProjectExtractor(new XYBReader());

    @FXML
    private ListView<SimpleScan> listViewScans;
    @FXML
    private TextField textFieldOutputDirectory;
    @FXML
    private CheckBox checkboxExportReflectance;
    @FXML
    private CheckBox checkboxExportAmplitude;
    @FXML
    private CheckBox checkboxExportDeviation;
    @FXML
    private CheckBox checkboxImportPOPMatrix;
    @FXML
    private CheckBox checkboxExportIntensity;
    @FXML
    private CheckBox checkboxExportRGB;
    @FXML
    private ComboBox<String> outputFormat;
    @FXML
    private CheckBox checkboxExportTime;
    @FXML
    private CheckBox checkboxExportXYZ;
    @FXML
    private TextField textFieldMinReflectance;
    @FXML
    private TextField textFieldMaxReflectance;
    @FXML
    private HBox hboxLasPrecision;
    @FXML
    private TextField textFieldXPrecision;
    @FXML
    private TextField textFieldYPrecision;
    @FXML
    private TextField textFieldZPrecision;

    private ValidationSupport validationSupport;

    @Override
    public void initialize(URL url, ResourceBundle rb) {

        outputFormat.getItems().setAll("txt", "las", "laz", "shots+echoes", "shots+echoes+shotTimeStamp");

        outputFormat.getSelectionModel().selectFirst();

        listViewScans.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        inputFileChooser = new FileChooser();
        inputFileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("All files", "*"),
                new FileChooser.ExtensionFilter("Riegl Riscan project", "*.rsp"),
                new FileChooser.ExtensionFilter("Riegl scan", "*.rxp"),
                new FileChooser.ExtensionFilter("Leica/Faro scan", "*.ptx", "*.ptg"));

        outputDirectoryChooser = new DirectoryChooser();

        hboxLasPrecision.disableProperty().bind(
                (outputFormat.getSelectionModel().selectedItemProperty().isEqualTo("las")
                        .or(outputFormat.getSelectionModel().selectedItemProperty().isEqualTo("laz"))).not());

        validationSupport = new ValidationSupport();

        validationSupport.registerValidator(textFieldMinReflectance, false, fieldDoubleValidator);
        validationSupport.registerValidator(textFieldMaxReflectance, false, fieldDoubleValidator);

        validationSupport.registerValidator(textFieldXPrecision, false, fieldDoubleValidator);
        validationSupport.registerValidator(textFieldXPrecision, false, fieldDoubleValidator);
        validationSupport.registerValidator(textFieldXPrecision, false, fieldDoubleValidator);

        validationSupport.registerValidator(textFieldOutputDirectory, directoryValidator("Output folder"));
    }

    @FXML
    private void onActionMenuItemSelectAllScans(ActionEvent event) {
        listViewScans.getSelectionModel().selectAll();
    }

    @FXML
    private void onActionMenuItemUnselectAllScans(ActionEvent event) {
        listViewScans.getSelectionModel().clearSelection();
    }

    @FXML
    private void onActionButtonOpenRspProject(ActionEvent event) {

        File selectedFile = inputFileChooser.showOpenDialog(stage);

        if (selectedFile != null) {

            String extension = FileManager.getExtension(selectedFile);

            switch (extension) {

                case ".rxp":
                    listViewScans.getItems().add(new SimpleScan(selectedFile));
                    break;
                case ".rsp":
                    try {

                    RSPReader rsp = new RSPReader(selectedFile);
                    riscanProjectExtractor.read(selectedFile);

                    riscanProjectExtractor.getFrame().setOnHidden((WindowEvent event1) -> {
                        List<LidarScan> selectedScans = riscanProjectExtractor.getController().getSelectedScans();

                        Matrix4d popMatrix;
                        if (checkboxImportPOPMatrix.isSelected()) {

                            popMatrix = rsp.getPopMatrix();
                            System.out.println("POP matrix imported : " + popMatrix.toString());
                        } else {
                            popMatrix = new Matrix4d();
                            popMatrix.setIdentity();
                            System.out.println("POP matrix disabled, set to identity.");
                        }

                        selectedScans.forEach(scan -> {
                            listViewScans.getItems().add(new SimpleScan(scan.getFile(), popMatrix, scan.getMatrix()));
                        });
                    });

                    riscanProjectExtractor.getFrame().showAndWait();

                } catch (IOException ex) {
                    System.err.println(ex);
                } catch (Exception ex) {
                    Logger.getLogger(ConverterController.class.getName()).log(Level.SEVERE, null, ex);
                }
                break;

                case ".ptg":
                case ".PTG":
                    try {

                    LidarProjectExtractor ptgProjectExtractor = new MultiScanProjectExtractor("PTG", f -> {
                        PTGScan scan = new PTGScan(f);
                        scan.readHeader();
                        return new LidarScan(scan.getFile(), new Matrix4d(scan.getHeader().getTransfMatrix()));
                    });

                    ptgProjectExtractor.read(selectedFile);
                    ptgProjectExtractor.getFrame().show();

                    ptgProjectExtractor.getFrame().setOnHidden((WindowEvent event1) -> {
                        final List<LidarScan> selectedScans = ptgProjectExtractor.getController().getSelectedScans();

                        selectedScans.forEach(scan -> {
                            Matrix4d identity = new Matrix4d();
                            identity.setIdentity();
                            listViewScans.getItems().add(new SimpleScan(scan.getFile(), identity, scan.getMatrix()));
                        });
                    });

                } catch (IOException ex) {
                    System.err.println(ex);
                } catch (Exception ex) {
                    System.err.println(ex);
                }
                break;

                case ".ptx":
                case ".PTX":
                    try {

                    ptxProjectExtractor.read(selectedFile);
                    ptxProjectExtractor.getFrame().show();

                    ptxProjectExtractor.getFrame().setOnHidden((WindowEvent event1) -> {
                        final List<LidarScan> selectedScans = ptxProjectExtractor.getController().getSelectedScans();

                        selectedScans.forEach(scan -> {
                            Matrix4d identity = new Matrix4d();
                            identity.setIdentity();
                            listViewScans.getItems().add(new SimpleScan(scan.getFile(), identity, scan.getMatrix()));
                        });
                    });

                } catch (IOException ex) {
                    System.err.println(ex);
                } catch (Exception ex) {
                    System.err.println(ex);
                }
                break;

                default:
                    System.err.println("Invalid extension");
            }
        }
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    @FXML
    private void onActionButtonRemoveScanFromListView(ActionEvent event) {
        ObservableList<SimpleScan> items = listViewScans.getItems();
        listViewScans.getItems().removeAll(items);
    }

    @FXML
    private void onActionButtonChooseOutputDirectory(ActionEvent event) {

        File directory = outputDirectoryChooser.showDialog(stage);

        if (directory != null) {
            textFieldOutputDirectory.setText(directory.getAbsolutePath());
        }
    }

    @FXML
    private void onActionButtonLaunchConversion(ActionEvent event) throws IOException {

        StringBuilder sb = new StringBuilder();

        if (validationSupport.isInvalid()) {
            validationSupport.initInitialDecoration();
            sb.append('\n');
            validationSupport.getValidationResult().getErrors().forEach(error -> sb.append("> ").append(error.getText()).append('\n'));
        }

        if (!sb.toString().isEmpty()) {
            throw new IOException(sb.toString());
        }

        final String outputFormatStr = outputFormat.getSelectionModel().getSelectedItem();
        final boolean exportReflectance = checkboxExportReflectance.isSelected();
        final boolean exportAmplitude = checkboxExportAmplitude.isSelected();
        final boolean exportDeviation = checkboxExportDeviation.isSelected();
        final boolean exportTime = checkboxExportTime.isSelected();
        final boolean exportIntensity = checkboxExportIntensity.isSelected();
        final boolean exportRGB = checkboxExportRGB.isSelected();
        final boolean exportXYZ = checkboxExportXYZ.isSelected();
        float minReflectance = Float.valueOf(textFieldMinReflectance.getText());
        float maxReflectance = Float.valueOf(textFieldMaxReflectance.getText());
        float precisionX = Float.valueOf(textFieldXPrecision.getText());
        float precisionY = Float.valueOf(textFieldYPrecision.getText());
        float precisionZ = Float.valueOf(textFieldZPrecision.getText());

        if (!textFieldOutputDirectory.getText().isEmpty()) {

            final File directory = new File(textFieldOutputDirectory.getText());

            if (Files.exists(directory.toPath())) {

                Service s = new Service() {

                    @Override
                    protected Task createTask() {
                        return new Task() {

                            @Override
                            protected Object call() throws Exception {

                                RXPScanConversion rxpConverter = new RXPScanConversion(minReflectance, maxReflectance,
                                        precisionX, precisionY, precisionZ);
                                PTGScanConversion ptgConverter = new PTGScanConversion(precisionX, precisionY, precisionZ);
                                PTXScanConversion ptxConverter = new PTXScanConversion();

                                System.out.println("Starting conversion");

                                int count = 0;
                                for (SimpleScan scan : listViewScans.getItems()) {

                                    updateProgress(count, listViewScans.getItems().size());
                                    updateMessage("Convert file " + (count + 1) + "/" + listViewScans.getItems().size() + " : " + scan.file.getName());
                                    String extension = FileManager.getExtension(scan.file);

                                    switch (outputFormatStr) {
                                        case "txt":
                                            switch (extension) {
                                                case ".rxp":
                                                    rxpConverter.toTxt(scan, directory, exportReflectance, exportAmplitude, exportDeviation, exportTime);
                                                    break;
                                                case ".PTX":
                                                case ".ptx":
                                                    ptxConverter.toTxt(scan, directory, exportRGB, exportIntensity);
                                                    break;
                                                case ".PTG":
                                                case ".ptg":
                                                    ptgConverter.toTxt(scan, directory, exportRGB, exportIntensity);
                                                    break;
                                                default:
                                                    break;
                                            }

                                            break;
                                        case "las":

                                            switch (extension) {
                                                case ".rxp":
                                                    rxpConverter.toLaz(scan, directory, false, exportTime, exportIntensity, exportDeviation, exportAmplitude);
                                                    break;
                                                case ".PTX":
                                                case ".ptx":
                                                    //ptxConverter.toLaz(scan, directory, false, exportIntensity);
                                                    break;
                                                case ".PTG":
                                                case ".ptg":
                                                    ptgConverter.toLaz(scan, directory, false, exportIntensity);
                                                    break;
                                                default:
                                                    break;
                                            }

                                            break;
                                        case "laz":

                                            switch (extension) {
                                                case ".rxp":
                                                    rxpConverter.toLaz(scan, directory, true, exportTime, exportIntensity, exportDeviation, exportAmplitude);
                                                    break;
                                                case ".PTX":
                                                case ".ptx":
                                                    //ptxConverter.toLaz(scan, directory, true, exportIntensity);
                                                    break;
                                                case ".PTG":
                                                case ".ptg":
                                                    ptgConverter.toLaz(scan, directory, true, exportIntensity);
                                                    break;
                                                default:
                                                    break;
                                            }

                                            break;
                                        case "shots+echoes":
                                            switch (extension) {
                                                case ".rxp":
                                                    rxpConverter.toShots(scan, directory, exportReflectance, exportAmplitude, exportDeviation, exportTime, exportXYZ);
                                                    break;
                                                default:
                                                    break;
                                            }
                                            break;
                                        case "shots+echoes+shotTimeStamp":
                                            switch (extension) {
                                                case ".rxp":
                                                    rxpConverter.toShots2(scan, directory, exportReflectance, exportAmplitude, exportDeviation, exportTime, exportXYZ);
                                                    break;
                                                default:
                                                    break;
                                            }
                                            break;
                                    }

                                    count++;
                                }

                                System.out.println("Conversion terminated");
                                return null;
                            }
                        };
                    }
                };

                ProgressDialog d = new ProgressDialog(s);

                d.show();

                s.start();
            }
        }
    }

    /**
     * Determines if the field is a decimal
     */
    public static Validator fieldDoubleValidator = new Validator<String>() {
        @Override
        public ValidationResult apply(Control t, String s) {

            if (s.isEmpty()) {
                return ValidationResult.fromErrorIf(t, "Field cannot be empty", s.isEmpty());
            } else {
                boolean valid = false;
                try {
                    Double.valueOf(s);
                    valid = true;
                } catch (NumberFormatException ex) {
                }

                return ValidationResult.fromErrorIf(t, "Decimal value expected", !valid);
            }
        }
    };

    /**
     * determines if the directory exists
     *
     * @param name, name of the control
     * @return
     */
    public static Validator directoryValidator(String name) {
        return new Validator<String>() {
            @Override
            public ValidationResult apply(Control t, String s) {
                if (s.isEmpty()) {
                    return ValidationResult.fromErrorIf(t, name + " cannot be empty", s.isEmpty());
                } else {
                    Path path = Paths.get(s);
                    return ValidationResult
                            .fromErrorIf(t, name + " does not exist", !Files.exists(path))
                            .addErrorIf(t, name + " is not a directory", !Files.isDirectory(path));
                }
            }
        };
    }
}
