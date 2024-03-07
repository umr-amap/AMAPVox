package org.amapvox.gui.configuration;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import org.amapvox.gui.chart.VoxelFileChart;
import org.amapvox.gui.chart.VoxelsToChart;
import org.amapvox.voxelfile.VoxelFileHeader;
import org.amapvox.voxelisation.output.OutputVariable;
import org.amapvox.voxelfile.VoxelFileReader;
import java.awt.Color;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.amapvox.gui.TextFieldUtil;
import org.amapvox.gui.Util;
import org.amapvox.gui.chart.ChartConfiguration;
import org.apache.log4j.Logger;

/**
 * FXML Controller class
 *
 * @author pverley
 */
public class ChartFrameController extends ConfigurationController {
    
    final private Logger logger = Logger.getLogger(ChartFrameController.class);
    
    private File lastFCOpenVoxelFile;
    
    @FXML
    private HBox hboxMaxChart;
    @FXML
    private TextField textfieldMaxChartNumberInARow;
    @FXML
    private HBox hboxHeighForChart;
    @FXML
    private RadioButton radiobuttonHeightFromAboveGround;
    @FXML
    private RadioButton radiobuttonHeightFromBelowCanopy;
    //
    private final BooleanProperty padDefinedForChart = new SimpleBooleanProperty(false);
    @FXML
    private ComboBox<String> comboboxVariableChart;
    @FXML
    private HBox hboxVariableChart;
    @FXML
    private ListView<VoxelFileChart> listViewVoxelsFilesChart;
    @FXML
    private Button buttonRemoveVoxelFileFromListViewForChart;
    @FXML
    private GridPane gridpaneSelectedScanChart;
    @FXML
    private Label labelSelectedScanChart;
    @FXML
    private TextField textfieldLabelVoxelFileChart;
    @FXML
    private CheckBox checkboxMakeQuadrats;
    @FXML
    private ComboBox<VoxelsToChart.QuadratAxis> comboboxSelectAxisForQuadrats;
    @FXML
    private VBox vboxQuadrats;
    @FXML
    private RadioButton radiobuttonSplitCountForQuadrats;
    @FXML
    private TextField textFieldSplitCountForQuadrats;
    @FXML
    private RadioButton radiobuttonLengthForQuadrats;
    @FXML
    private TextField textFieldLengthForQuadrats;
    @FXML
    private ColorPicker colorPickerSeries;
    
    @Override
    void initComponents(ResourceBundle rb) {
        
        colorPickerSeries.valueProperty().addListener((ObservableValue<? extends javafx.scene.paint.Color> observable, javafx.scene.paint.Color oldValue, javafx.scene.paint.Color newValue)
                -> {
            if (listViewVoxelsFilesChart.getSelectionModel().getSelectedItems().size() == 1) {
                listViewVoxelsFilesChart.getSelectionModel().getSelectedItem().getSeriesParameters().setColor(new Color(
                        (float) newValue.getRed(), (float) newValue.getGreen(), (float) newValue.getBlue(), 1.0f));
            }
        });
        
        ToggleGroup profileChartRelativeHeightType = new ToggleGroup();
        radiobuttonHeightFromAboveGround.setToggleGroup(profileChartRelativeHeightType);
        radiobuttonHeightFromBelowCanopy.setToggleGroup(profileChartRelativeHeightType);
        
        textfieldMaxChartNumberInARow.setTextFormatter(TextFieldUtil.createIntegerTextFormatter(6, TextFieldUtil.Sign.POSITIVE));
        textFieldSplitCountForQuadrats.setTextFormatter(TextFieldUtil.createIntegerTextFormatter(1, TextFieldUtil.Sign.POSITIVE));
        textFieldLengthForQuadrats.setTextFormatter(TextFieldUtil.createIntegerTextFormatter(5, TextFieldUtil.Sign.POSITIVE));
        
        buttonRemoveVoxelFileFromListViewForChart.disableProperty().bind(Bindings.isEmpty(listViewVoxelsFilesChart.getItems()));
        labelSelectedScanChart.textProperty().bind(listViewVoxelsFilesChart.getSelectionModel().selectedItemProperty().asString());
        colorPickerSeries.disableProperty().bind(listViewVoxelsFilesChart.getSelectionModel().selectedItemProperty().isNull());
        textfieldLabelVoxelFileChart.disableProperty().bind(listViewVoxelsFilesChart.getSelectionModel().selectedItemProperty().isNull());
        
        textfieldLabelVoxelFileChart.textProperty().addListener((ObservableValue<? extends String> observable, String oldValue, String newValue)
                -> {
            if (listViewVoxelsFilesChart.getSelectionModel().getSelectedIndex() >= 0) {
                listViewVoxelsFilesChart.getSelectionModel().getSelectedItem().getSeriesParameters().setLabel(newValue);
            }
        });
        
        listViewVoxelsFilesChart.getItems().addListener((ListChangeListener.Change<? extends VoxelFileChart> vfc) -> {
            while (vfc.next()) {
            }

            // update drawable variables
            comboboxVariableChart.getItems().clear();
            String[] variables = intersectVariablesForChart((ObservableList<VoxelFileChart>) vfc.getList());
            if (variables.length > 0) {
                comboboxVariableChart.getItems().addAll(variables);
                comboboxVariableChart.getSelectionModel().selectFirst();
            }

            // check existence of required variables
            checkListViewForChart((ObservableList<VoxelFileChart>) vfc.getList());
        });
        
        listViewVoxelsFilesChart.getSelectionModel().selectedItemProperty()
                .addListener((ObservableValue<? extends VoxelFileChart> observable, VoxelFileChart oldvalue, VoxelFileChart newvalue) -> {
                    if (null != newvalue) {
                        Color color = newvalue.getSeriesParameters().getColor();
                        colorPickerSeries.setValue(javafx.scene.paint.Color.rgb(color.getRed(), color.getGreen(), color.getBlue()));
                        textfieldLabelVoxelFileChart.setText(newvalue.getSeriesParameters().getLabel());
                    }
                });
        
        hboxHeighForChart.disableProperty().bind(Bindings.isEmpty(listViewVoxelsFilesChart.getItems()));
        
        vboxQuadrats.disableProperty().bind(Bindings.or(
                checkboxMakeQuadrats.selectedProperty().not(),
                checkboxMakeQuadrats.disableProperty()));
        
        comboboxSelectAxisForQuadrats.getItems().addAll(VoxelsToChart.QuadratAxis.values());
        comboboxSelectAxisForQuadrats.getSelectionModel().select(1);
        
        radiobuttonSplitCountForQuadrats.selectedProperty().addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue)
                -> {
            textFieldSplitCountForQuadrats.setDisable(!newValue);
            textFieldLengthForQuadrats.setDisable(newValue);
        });
        
        ToggleGroup chartMakeQuadratsSplitType = new ToggleGroup();
        radiobuttonLengthForQuadrats.setToggleGroup(chartMakeQuadratsSplitType);
        radiobuttonSplitCountForQuadrats.setToggleGroup(chartMakeQuadratsSplitType);
        
        Util.setDragGestureEvents(textfieldLabelVoxelFileChart);
        Util.setDragGestureEvents(listViewVoxelsFilesChart, file -> addVoxelFileToListViewForChart(file));
        
    }
    
    @FXML
    private void onActionButtonAddVoxelFileToListViewForChart(ActionEvent event) {
        
        if (lastFCOpenVoxelFile != null) {
            Util.FILE_CHOOSER_VOXELFILE.setInitialDirectory(lastFCOpenVoxelFile.getParentFile());
        }
        
        List<File> selectedFiles = Util.FILE_CHOOSER_VOXELFILE.showOpenMultipleDialog(null);
        if (selectedFiles != null) {
            lastFCOpenVoxelFile = selectedFiles.get(0);
            selectedFiles.forEach(file -> addVoxelFileToListViewForChart(file));
        }
    }
    
    @FXML
    private void onActionButtonRemoveVoxelFileFromListViewForChart(ActionEvent event) {
        
        ObservableList<VoxelFileChart> selectedItems = listViewVoxelsFilesChart.getSelectionModel().getSelectedItems();
        listViewVoxelsFilesChart.getItems().removeAll(selectedItems);
        logger.info("Voxel file removed.");
    }
    
    private void addVoxelFileToListViewForChart(VoxelFileChart vfc) {
        addVoxelFileToListViewForChart(
                vfc.file,
                vfc.getSeriesParameters().getLabel(),
                vfc.getSeriesParameters().getColor());
    }
    
    private void addVoxelFileToListViewForChart(File file) {
        addVoxelFileToListViewForChart(file, null, null);
    }
    
    private void addVoxelFileToListViewForChart(File file, String label, Color color) {

        // check at least nbSampling & nbEchoes or groundDistance variables are provided 
        try {
            VoxelFileHeader header = new VoxelFileReader(file).getHeader();
            if (header.findColumnIndex(OutputVariable.GROUND_DISTANCE) < 0
                    || header.findColumnIndex(OutputVariable.NUMBER_OF_SHOTS) < 0
                    || header.findColumnIndex(OutputVariable.NUMBER_OF_ECHOES) < 0) {
                StringBuilder sb = new StringBuilder();
                sb.append("Failed to add ").append(file.getName())
                        .append(". Some required variables for chart drawing are missing: ")
                        .append(OutputVariable.GROUND_DISTANCE.getShortName()).append(" and/or ")
                        .append(OutputVariable.NUMBER_OF_SHOTS.getShortName()).append(" and/or ")
                        .append(OutputVariable.NUMBER_OF_ECHOES.getShortName()).append("");
                logger.warn(sb.toString());
            } else {
                VoxelFileChart voxelFileChart = new VoxelFileChart(
                        file,
                        null != label ? label : file.getName(),
                        null != color ? color : (Color) VoxelsToChart.DEFAULT_RENDERER.lookupSeriesPaint(listViewVoxelsFilesChart.getItems().size()));
                listViewVoxelsFilesChart.getItems().add(voxelFileChart);
                listViewVoxelsFilesChart.getSelectionModel().selectLast();
            }
        } catch (Exception ex) {
            logger.warn("Failed to add " + file.getName() + ". Not a valid vox file.", ex);
        }
    }
    
    private String[] intersectVariablesForChart(ObservableList<VoxelFileChart> list) {
        
        List<List<String>> allColumns = new ArrayList();
        list.forEach(vfc -> {
            try {
                VoxelFileHeader header = new VoxelFileReader(vfc.file).getHeader();
                allColumns.add(Arrays.asList(
                        Arrays.copyOfRange(header.getColumnNames(), 3, header.getColumnNames().length)));
            } catch (Exception ex) {
                logger.warn(null, ex);
            }
        });
        
        List<String> sharedColumns = allColumns.isEmpty() ? new ArrayList() : allColumns.get(0);
        if (allColumns.size() > 1) {
            for (int i = 1; i < allColumns.size(); i++) {
                sharedColumns.retainAll(allColumns.get(i));
            }
        }
        
        return sharedColumns.toArray(new String[sharedColumns.size()]);
    }
    
    private void checkListViewForChart(ObservableList<VoxelFileChart> list) {
        
        list.stream()
                .map(vfc -> vfc.file)
                .forEach(file -> {
                    // check existence PAD variables
                    try {
                        VoxelFileHeader header = new VoxelFileReader(file).getHeader();
                        padDefinedForChart.set(header.findColumnIndex(OutputVariable.PLANT_AREA_DENSITY) >= 0);
                    } catch (Exception ex) {
                    }
                });
    }
    
    private <T> void forceListRefreshOn(ListView<T> lsv) {
        ObservableList<T> items = lsv.<T>getItems();
        lsv.<T>setItems(null);
        lsv.<T>setItems(items);
    }
    
    @Override
    void saveConfiguration(File file) throws Exception {
        
        ChartConfiguration cfg = new ChartConfiguration();
        
        listViewVoxelsFilesChart.getItems().forEach(vfc -> cfg.addVoxelFileChart(vfc));
        
        cfg.setVariableName(comboboxVariableChart.getValue());
        
        cfg.setLayerReference(radiobuttonHeightFromAboveGround.isSelected()
                ? VoxelsToChart.LayerReference.FROM_ABOVE_GROUND
                : VoxelsToChart.LayerReference.FROM_BELOW_CANOPEE);
        
        cfg.setSplit(checkboxMakeQuadrats.isSelected());
        cfg.setSplitAxis(comboboxSelectAxisForQuadrats.getValue());
        cfg.setSplitCount(radiobuttonSplitCountForQuadrats.isSelected() ? Integer.parseInt(textFieldSplitCountForQuadrats.getText()) : -1);
        cfg.setSplitLength(radiobuttonLengthForQuadrats.isSelected() ? Integer.parseInt(textFieldLengthForQuadrats.getText()) : -1);
        
        cfg.setMaxChartNumberInARow(Integer.parseInt(textfieldMaxChartNumberInARow.getText()));
        
        cfg.write(file);
    }
    
    @Override
    void loadConfiguration(File file) throws Exception {
        
        ChartConfiguration cfg = new ChartConfiguration();
        cfg.read(file);
        
        cfg.getListVoxelFileChart().forEach(vfc -> addVoxelFileToListViewForChart(vfc));
        
        comboboxVariableChart.setValue(cfg.getVariableName());
        
        radiobuttonHeightFromAboveGround.setSelected(cfg.getLayerReference().equals(VoxelsToChart.LayerReference.FROM_ABOVE_GROUND));
        radiobuttonHeightFromBelowCanopy.setSelected(cfg.getLayerReference().equals(VoxelsToChart.LayerReference.FROM_BELOW_CANOPEE));
        
        checkboxMakeQuadrats.setSelected(cfg.isSplit());
        comboboxSelectAxisForQuadrats.setValue(cfg.getSplitAxis());
        
        radiobuttonLengthForQuadrats.setSelected(cfg.getSplitLength() >= 0);
        textFieldLengthForQuadrats.setText(String.valueOf(cfg.getSplitLength()));
        
        radiobuttonSplitCountForQuadrats.setSelected(cfg.getSplitCount() >= 0);
        textFieldSplitCountForQuadrats.setText(String.valueOf(cfg.getSplitCount()));
        
        textfieldMaxChartNumberInARow.setText(String.valueOf(cfg.getMaxChartNumberInARow()));
    }
    
    @Override
    void initValidationSupport() {
    }
    
    @Override
    ObservableValue[] getListenedProperties() {
        
        return new ObservableValue[]{
            listViewVoxelsFilesChart.itemsProperty(),
            comboboxVariableChart.getSelectionModel().selectedIndexProperty(),
            colorPickerSeries.valueProperty(),
            textfieldLabelVoxelFileChart.textProperty(),
            radiobuttonHeightFromAboveGround.selectedProperty(),
            radiobuttonHeightFromBelowCanopy.selectedProperty(),
            checkboxMakeQuadrats.selectedProperty(),
            comboboxSelectAxisForQuadrats.getSelectionModel().selectedIndexProperty(),
            radiobuttonSplitCountForQuadrats.selectedProperty(),
            textFieldSplitCountForQuadrats.textProperty(),
            radiobuttonLengthForQuadrats.selectedProperty(),
            textFieldLengthForQuadrats.textProperty(),
            textfieldMaxChartNumberInARow.textProperty()
        };
    }
    
}
