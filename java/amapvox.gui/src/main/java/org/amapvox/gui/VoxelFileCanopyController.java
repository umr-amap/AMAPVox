package org.amapvox.gui;

import java.io.File;
import java.net.URL;
import java.util.NoSuchElementException;
import java.util.ResourceBundle;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import org.amapvox.canopy.LeafAngleDistribution;
import org.amapvox.voxelfile.VoxelFileHeader;
import org.amapvox.voxelfile.VoxelFileReader;
import org.apache.log4j.Logger;
import org.controlsfx.validation.ValidationSupport;

/**
 * FXML Controller class for voxel input file component with PAD variable
 * selector and leaf angle distribution selector.
 *
 * @author Philippe Verley
 */
public class VoxelFileCanopyController implements Initializable {

    // logger
    private final Logger LOGGER = Logger.getLogger(VoxelFileCanopyController.class);

    @FXML
    private TextField textfieldVoxelFileCanopyPath;
    @FXML
    private CheckMissingVoxelController checkMissingVoxelCanopyController;
    @FXML
    private ComboBox<String> comboboxPADVariable;
    @FXML
    private Button helpButtonPADVariable;
    @FXML
    private HelpButtonController helpButtonPADVariableController;
    // Leaf angle distribution
    @FXML
    private ComboBox<LeafAngleDistribution.Type> comboboxLeafAngleDistribution;
    @FXML
    private HBox hboxTwoBetaParameters;
    @FXML
    private Label labelLADBeta;
    @FXML
    private TextField textFieldTwoBetaAlphaParameter;
    @FXML
    private TextField textFieldTwoBetaBetaParameter;
    @FXML
    private Button helpButtonLeafAngleDistribution;
    @FXML
    private HelpButtonController helpButtonLeafAngleDistributionController;

    // validation support
    private ValidationSupport validationSupport;
    
    // last opened voxel file
    private File lastOpenedVoxelFile;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        Util.setDragGestureEvents(textfieldVoxelFileCanopyPath, Util.isVoxelFile, voxelFile -> updateVoxVariables(voxelFile));

        textfieldVoxelFileCanopyPath.textProperty().addListener(checkMissingVoxelCanopyController);

        helpButtonPADVariable.setOnAction((ActionEvent event) -> {
            helpButtonPADVariableController.showHelpDialog(resources.getString("help_pad_variable"));
        });

        comboboxLeafAngleDistribution.getItems().addAll(LeafAngleDistribution.Type.values());

        comboboxLeafAngleDistribution.getSelectionModel().select(LeafAngleDistribution.Type.SPHERIC);
        comboboxLeafAngleDistribution.getSelectionModel().selectedItemProperty().addListener((ObservableValue<? extends LeafAngleDistribution.Type> observable, LeafAngleDistribution.Type oldValue, LeafAngleDistribution.Type newValue)
                -> {
            if (newValue == LeafAngleDistribution.Type.TWO_PARAMETER_BETA || newValue == LeafAngleDistribution.Type.ELLIPSOIDAL) {
                hboxTwoBetaParameters.setVisible(true);
                if (newValue == LeafAngleDistribution.Type.ELLIPSOIDAL) {
                    labelLADBeta.setVisible(false);
                    textFieldTwoBetaBetaParameter.setVisible(false);
                } else {
                    labelLADBeta.setVisible(true);
                    textFieldTwoBetaBetaParameter.setVisible(true);
                }
            } else {
                hboxTwoBetaParameters.setVisible(false);
            }
        });

        textFieldTwoBetaAlphaParameter.setTextFormatter(TextFieldUtil.createFloatTextFormatter(0.f));
        textFieldTwoBetaBetaParameter.setTextFormatter(TextFieldUtil.createFloatTextFormatter(0.f));

        helpButtonLeafAngleDistribution.setOnAction((ActionEvent event) -> {
            helpButtonLeafAngleDistributionController.showHelpDialog(resources.getString("help_leaf_angle_distribution"));
        });

        validationSupport = new ValidationSupport();
    }

    public void registerValidators() {
        validationSupport.registerValidator(textfieldVoxelFileCanopyPath, true, Validators.fileExistValidator("Voxel file"));
    }

    public void unregisterValidators() {
        validationSupport.registerValidator(textfieldVoxelFileCanopyPath, false, Validators.unregisterValidator);
    }

    public ValidationSupport getValidationSupport() {
        return validationSupport;
    }

    public void setVoxelFile(File voxelFile, String padVariable) {

        textfieldVoxelFileCanopyPath.setText(voxelFile.getAbsolutePath());
        updateVoxVariables(voxelFile);
        if (comboboxPADVariable.getItems().contains(padVariable)) {
            comboboxPADVariable.getSelectionModel().select(padVariable);
        } else {
            LOGGER.warn("Variable " + padVariable + " not found in voxel file " + voxelFile.getName());
        }
    }

    public File getVoxelFile() {
        return textfieldVoxelFileCanopyPath.getText().isBlank()
                ? null
                : new File(textfieldVoxelFileCanopyPath.getText());
    }

    public String getPADVariable() {
        return comboboxPADVariable.getSelectionModel().getSelectedItem();
    }

    private void updateVoxVariables(File voxelFile) {

        if (!VoxelFileReader.isValid(voxelFile)) {
            comboboxPADVariable.getItems().clear();
            comboboxPADVariable.setDisable(true);
            return;
        }

        try {
            VoxelFileHeader header = new VoxelFileReader(voxelFile).getHeader();
            String[] parameters = header.getColumnNames();

            comboboxPADVariable.getItems().clear();
            comboboxPADVariable.getItems().addAll(parameters);

            // select column name containing 'pad'
            try {
                String pad = comboboxPADVariable.getItems().stream()
                        .filter(column -> column.toLowerCase().contains("pad"))
                        .findAny().get();
                comboboxPADVariable.getSelectionModel().select(pad);
            } catch (NoSuchElementException ex) {
                LOGGER.warn("[Canopy analyzer] Did not find any \"plant area density\" variable in voxel file " + voxelFile.getName());
            }

            comboboxPADVariable.setDisable(false);

        } catch (Exception ex) {
            LOGGER.error("[Canopy analyzer] Cannot read voxel file", ex);
        }
    }

    @FXML
    private void onActionButtonOpenVoxelFile(ActionEvent event) {
        
        if (lastOpenedVoxelFile != null) {
            Util.FILE_CHOOSER_VOXELFILE.setInitialDirectory(lastOpenedVoxelFile.getParentFile());
        }

        File selectedFile = Util.FILE_CHOOSER_VOXELFILE.showOpenDialog(null);

        if (selectedFile != null) {
            textfieldVoxelFileCanopyPath.setText(selectedFile.getAbsolutePath());
            updateVoxVariables(selectedFile);
            lastOpenedVoxelFile = selectedFile;
            LOGGER.debug("[Canopy analyzer] Opened voxel file " + selectedFile.getName());
        }
    }

    /**
     * @return the leafAngleDistribution
     */
    public LeafAngleDistribution.Type getLeafAngleDistribution() {
        return comboboxLeafAngleDistribution.getSelectionModel().getSelectedItem();
    }

    /**
     * @param leafAngleDistribution the leafAngleDistribution to set
     */
    public void setLeafAngleDistribution(LeafAngleDistribution.Type leafAngleDistribution) {
        comboboxLeafAngleDistribution.getSelectionModel().select(leafAngleDistribution);
    }

    /**
     * @return the leafAngleDistribution parameters
     */
    public double[] getLeafAngleDistributionParameters() {
        return new double[]{
            Double.parseDouble(textFieldTwoBetaAlphaParameter.getText()),
            Double.parseDouble(textFieldTwoBetaBetaParameter.getText())
        };
    }

    /**
     * @param leafAngleDistributionParameters
     */
    public void setLeafAngleDistributionParameters(double[] leafAngleDistributionParameters) {
        textFieldTwoBetaAlphaParameter.setText(String.valueOf(leafAngleDistributionParameters[0]));
        textFieldTwoBetaAlphaParameter.setText(String.valueOf(leafAngleDistributionParameters[1]));
    }

    public ObservableValue[] getListenedProperties() {
        return new ObservableValue[]{
            comboboxPADVariable.getSelectionModel().selectedIndexProperty(),
            comboboxLeafAngleDistribution.getSelectionModel().selectedIndexProperty(),
            textFieldTwoBetaAlphaParameter.textProperty(),
            textFieldTwoBetaBetaParameter.textProperty(),
            textfieldVoxelFileCanopyPath.textProperty()};
    }

}
