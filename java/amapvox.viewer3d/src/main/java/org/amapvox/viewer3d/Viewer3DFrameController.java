package org.amapvox.viewer3d;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import org.amapvox.commons.util.ColorGradient;
import org.amapvox.commons.util.filter.CombinedFloatFilter;
import org.amapvox.commons.util.filter.FloatFilter;
import org.amapvox.viewer3d.object.camera.TrackballCamera;
import org.amapvox.commons.util.filter.CombinedFilterItem;
import org.amapvox.viewer3d.loading.shader.InstanceLightedShader;
import org.amapvox.viewer3d.loading.shader.InstanceShader;
import org.amapvox.viewer3d.object.scene.SceneObject;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javax.imageio.ImageIO;
import javax.vecmath.Vector3f;
import org.amapvox.voxelisation.output.OutputVariable;

/**
 * FXML Controller class
 *
 * @author Julien Heurtebize (julienhtbe@gmail.com)
 */
public class Viewer3DFrameController implements Initializable {

    private final static Logger LOGGER = Logger.getLogger(Viewer3DFrameController.class.getCanonicalName());

    private SimpleViewer viewer3D;
    private final FileChooser fcScreenshot = new FileChooser();

    private TreeItem<SceneObjectWrapper> root;

    private Stage stage;

    public double maxHeight;

    private VoxelSpaceSceneObject voxelSpace;

    @FXML
    private ComboBox<String> comboBoxAttributeToShow;
    @FXML
    private ComboBox<String> comboboxGradient;
    @FXML
    private TextField textFieldVoxelScale;
    @FXML
    private Button buttonApplyVoxelSize;
    @FXML
    private TextField textFieldMinValue;
    @FXML
    private TextField textFieldMaxValue;
    @FXML
    private CheckBox checkboxStretched;
    @FXML
    private ComboBox<String> comboBoxScalarField;
    @FXML
    private ListView<CombinedFilterItem> listviewFilters;
    @FXML
    private RadioButton radiobuttonDisplay;
    @FXML
    private RadioButton radiobuttonDontDisplay;
    @FXML
    private TextField textfieldFilteringRange;
    @FXML
    private AnchorPane anchorPaneRoot;
    @FXML
    private TableColumn<Attribut, String> tableColumnName;
    @FXML
    private TableColumn<Attribut, String> tableColumnValue;
    @FXML
    private TableView<Attribut> tableviewAttribut;
    @FXML
    private CheckMenuItem checkMenuItemShowColorScale;
    @FXML
    private CheckMenuItem checkMenuItemPerspective;
    @FXML
    private TextField textFieldPerspective;
    @FXML
    private CheckMenuItem checkMenuItemOrthographic;
    @FXML
    private ColorPicker colorpickerLightingAmbientColor;
    @FXML
    private ColorPicker colorpickerLightingDiffuseColor;
    @FXML
    private ColorPicker colorpickerLightingSpecularColor;
    @FXML
    private ColorPicker colorPickerBackgroundColor;
    @FXML
    private CheckMenuItem checkMenuItemEnableLighting;
    @FXML
    private TextField textfieldCameraNear;
    @FXML
    private TextField textfieldCameraFar;
    @FXML
    private TreeView<SceneObjectWrapper> treeviewSceneObjects;
    @FXML
    private ImageView imageviewSkyColor;

    private class Attribut {

        private final String name;
        private final double value;

        public Attribut(String name, double value) {
            this.name = name;
            this.value = value;
        }

        public String getName() {
            return name;
        }

        public double getValue() {
            return value;
        }
    }
    
    public final static List<String> AVAILABLE_GRADIENT_COLOR_NAMES = new ArrayList<>();
    public final static List<Color[]> AVAILABLE_GRADIENT_COLORS = new ArrayList<>();

    static {
        try {

            Class c = ColorGradient.class;
            Field[] fields = c.getFields();

            for (Field field : fields) {

                String type = field.getType().getSimpleName();
                if (type.equals("Color[]")) {
                    AVAILABLE_GRADIENT_COLOR_NAMES.add(field.getName());
                    AVAILABLE_GRADIENT_COLORS.add((Color[]) field.get(c));
                }
            }

        } catch (IllegalArgumentException | IllegalAccessException ex) {
            LOGGER.log(Level.SEVERE, "Cannot retrieve avaialble color gradients", ex);
        }
    }


    /**
     * Initializes the controller class.
     *
     * @param url
     * @param rb
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {

        treeviewSceneObjects.setCellFactory((TreeView<SceneObjectWrapper> param) -> new SceneObjectTreeCell());

        root = new TreeItem<>();
        root.setExpanded(true);
        treeviewSceneObjects.setRoot(root);

        checkMenuItemPerspective.selectedProperty().addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
            checkMenuItemOrthographic.setSelected(!newValue);
        });

        checkMenuItemOrthographic.selectedProperty().addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
            checkMenuItemPerspective.setSelected(!newValue);
        });

        checkMenuItemOrthographic.selectedProperty().addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
            if (newValue) {

                try {
                    float near = Float.valueOf(textfieldCameraNear.getText());
                    float far = Float.valueOf(textfieldCameraFar.getText());
                    viewer3D.getJoglContext().getScene().getCamera().setViewToOrthographic(near, far, far, far, near, far);
                    viewer3D.getJoglContext().updateCamera();
                    viewer3D.getJoglContext().refresh();
                } catch (Exception e) {
                }

            }
        });

        checkMenuItemPerspective.selectedProperty().addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
            if (newValue) {

                try {
                    float fov = Float.valueOf(textFieldPerspective.getText());
                    float near = Float.valueOf(textfieldCameraNear.getText());
                    float far = Float.valueOf(textfieldCameraFar.getText());

                    viewer3D.getJoglContext().getScene().getCamera().setViewToPerspective(fov, near, far);
                    viewer3D.getJoglContext().updateCamera();
                    viewer3D.getJoglContext().refresh();

                } catch (Exception e) {
                }

            }
        });

        textFieldPerspective.textProperty().addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
            try {
                float fov = Float.valueOf(newValue);
                TrackballCamera camera = viewer3D.getJoglContext().getScene().getCamera();
                viewer3D.getJoglContext().getScene().getCamera().setPerspective(fov, camera.getAspect(), camera.getNearPersp(), camera.getFarPersp());
                viewer3D.getJoglContext().refresh();

            } catch (Exception e) {
            }
        });

        tableColumnName.setCellValueFactory((TableColumn.CellDataFeatures<Attribut, String> param) -> new SimpleStringProperty(param.getValue().getName()));

        tableColumnValue.setCellValueFactory((TableColumn.CellDataFeatures<Attribut, String> param) -> new SimpleStringProperty(String.valueOf(param.getValue().getValue())));

        comboboxGradient.getItems().addAll(AVAILABLE_GRADIENT_COLOR_NAMES);
        comboboxGradient.getSelectionModel().select("HEAT");

        colorPickerBackgroundColor.valueProperty().addListener((ObservableValue<? extends javafx.scene.paint.Color> observable, javafx.scene.paint.Color oldValue, javafx.scene.paint.Color newValue) -> {
            if (viewer3D != null) {
                viewer3D.getJoglContext().setWorldColor(new Vector3f((float) newValue.getRed(), (float) newValue.getGreen(), (float) newValue.getBlue()));
                viewer3D.getJoglContext().refresh();
            }
        });

        comboboxGradient.getSelectionModel().selectedItemProperty().addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
            String gradient = newValue;
            Color[] gradientColor = ColorGradient.GRADIENT_RAINBOW;

            for (int i = 0; i < AVAILABLE_GRADIENT_COLORS.size(); i++) {

                if (AVAILABLE_GRADIENT_COLOR_NAMES.get(i).equals(gradient)) {
                    gradientColor = AVAILABLE_GRADIENT_COLORS.get(i);
                    i = AVAILABLE_GRADIENT_COLOR_NAMES.size() - 1;
                }
            }

            //recalculate voxel color with the new gradient
            voxelSpace.updateColorValue(gradientColor);

            //update instance color buffer to gpu
            voxelSpace.updateInstanceColorBuffer();

            viewer3D.getJoglContext().refresh();
        });

        colorPickerBackgroundColor.setValue(new javafx.scene.paint.Color(0.8, 0.8, 0.8, 1));
        colorpickerLightingAmbientColor.setValue(new javafx.scene.paint.Color(1.0, 1.0, 1.0, 1));
        colorpickerLightingDiffuseColor.setValue(new javafx.scene.paint.Color(1.0, 1.0, 1.0, 1));
        colorpickerLightingSpecularColor.setValue(new javafx.scene.paint.Color(1.0, 1.0, 1.0, 1));

        colorpickerLightingAmbientColor.valueProperty().addListener((ObservableValue<? extends javafx.scene.paint.Color> observable, javafx.scene.paint.Color oldValue, javafx.scene.paint.Color newValue) -> {
            viewer3D.getJoglContext().getScene().setLightAmbientValue(new Vector3f((float) newValue.getRed(), (float) newValue.getGreen(), (float) newValue.getBlue()));
            viewer3D.getJoglContext().refresh();
        });

        colorpickerLightingDiffuseColor.valueProperty().addListener((ObservableValue<? extends javafx.scene.paint.Color> observable, javafx.scene.paint.Color oldValue, javafx.scene.paint.Color newValue) -> {
            viewer3D.getJoglContext().getScene().setLightDiffuseValue(new Vector3f((float) newValue.getRed(), (float) newValue.getGreen(), (float) newValue.getBlue()));
            viewer3D.getJoglContext().refresh();
        });

        colorpickerLightingSpecularColor.valueProperty().addListener((ObservableValue<? extends javafx.scene.paint.Color> observable, javafx.scene.paint.Color oldValue, javafx.scene.paint.Color newValue) -> {
            viewer3D.getJoglContext().getScene().setLightSpecularValue(new Vector3f((float) newValue.getRed(), (float) newValue.getGreen(), (float) newValue.getBlue()));
            viewer3D.getJoglContext().refresh();
        });

        comboBoxAttributeToShow.getSelectionModel().selectedItemProperty().addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
            try {
                voxelSpace.resetRange();
                voxelSpace.setAttribute(newValue);
                voxelSpace.updateVao();
                voxelSpace.updateInstanceColorBuffer();
                viewer3D.getJoglContext().refresh();
                textFieldMinValue.setText(String.valueOf(voxelSpace.getRealAttributValueMin()));
                textFieldMaxValue.setText(String.valueOf(voxelSpace.getRealAttributValueMax()));

            } catch (Exception e) {
            }
        });

        final InstanceLightedShader ils = new InstanceLightedShader();
        final InstanceShader is = new InstanceShader();

        checkMenuItemEnableLighting.selectedProperty().addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
            if (newValue) {
                voxelSpace.setShader(ils);
            } else {
                voxelSpace.setShader(is);
            }

            viewer3D.getJoglContext().refresh();
        });

        /*textFieldFilterValues.setOnKeyReleased(new EventHandler<KeyEvent>() {

            @Override
            public void handle(KeyEvent event) {
                
                if(event.getCode() == KeyCode.ENTER){
                    updateValuesFilter();
                }
            }
        });*/
        checkboxStretched.selectedProperty().addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
            if (newValue) {
                voxelSpace.setStretched(true);
                voxelSpace.updateValue();
                voxelSpace.updateInstanceColorBuffer();

                viewer3D.getJoglContext().refresh();
            } else {
                voxelSpace.setStretched(false);
                voxelSpace.updateValue();
                voxelSpace.updateInstanceColorBuffer();

                viewer3D.getJoglContext().refresh();
            }
        });
        /*
        
        textfieldCameraNear.textProperty().addListener(new ChangeListener<String>() {

            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                try{
                    float near = Float.valueOf(newValue);
                    TrackballCamera camera = joglContext.getScene().getCamera();
                    if(radiobuttonOrthographicCamera.isSelected()){
                        joglContext.getScene().getCamera().setOrthographic(camera.getLeft(), camera.getRight(), camera.getTop(), camera.getBottom(), near, camera.getFarOrtho());
                        joglContext.updateCamera();
                        joglContext.refresh();
                    }else{
                        joglContext.getScene().getCamera().setPerspective(camera.getFovy(), camera.getAspect(), near, camera.getFarPersp());
                        joglContext.updateCamera();
                        joglContext.refresh();
                    }
                    
                    joglContext.refresh();
                    
                }catch(Exception e){}
            }
        });
        
        textfieldCameraFar.textProperty().addListener(new ChangeListener<String>() {

            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                try{
                    float far = Float.valueOf(newValue);
                    TrackballCamera camera = joglContext.getScene().getCamera();
                    if(radiobuttonOrthographicCamera.isSelected()){
                        camera.setOrthographic(camera.getLeft(), camera.getRight(), camera.getTop(), camera.getBottom(), camera.getNearOrtho(), far);
                        joglContext.updateCamera();
                        joglContext.refresh();
                    }else{
                        camera.setPerspective(camera.getFovy(), camera.getAspect(), camera.getNearPersp(), far);
                        joglContext.updateCamera();
                        joglContext.refresh();
                    }
                    
                    joglContext.refresh();
                    
                }catch(Exception e){}
            }
        });
         */

        comboBoxScalarField.setItems(comboBoxAttributeToShow.getItems());
        comboBoxScalarField.getItems().addListener((ListChangeListener.Change<? extends String> c) -> {
            if (c.getList().size() > 0) {
                comboBoxScalarField.getSelectionModel().selectFirst();
            }
        });

        checkMenuItemShowColorScale.selectedProperty().addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
            viewer3D.getScene().getSceneObject("color scale").setVisible(newValue);
            viewer3D.getJoglContext().refresh();
        });

        ToggleGroup group = new ToggleGroup();
        radiobuttonDisplay.setToggleGroup(group);
        radiobuttonDontDisplay.setToggleGroup(group);
    }

    public void setStage(Stage stage) {

        this.stage = stage;
        maxHeight = stage.getHeight();

    }

    public void initContent(final VoxelSpaceSceneObject voxelSpace) {

        this.voxelSpace = voxelSpace;

        Platform.runLater(() -> {
            textFieldVoxelScale.setText(String.valueOf(voxelSpace.getVoxelScale()));
            textFieldMinValue.setText(String.valueOf(voxelSpace.getRealAttributValueMin()));
            textFieldMaxValue.setText(String.valueOf(voxelSpace.getRealAttributValueMax()));
        });
    }

    public void setViewer3D(SimpleViewer viewer3D) {
        this.viewer3D = viewer3D;
    }

    public void setAttributes(String attributeToVisualize, String[] attributes) {

        if (attributeToVisualize == null && attributes.length > 0) {
            attributeToVisualize = attributes[0];
        }

        comboBoxAttributeToShow.getItems().addAll(attributes);
        comboBoxAttributeToShow.getSelectionModel().select(attributeToVisualize);
    }

    public void addDefaultFilters() {

        String attributeToFilter = comboBoxAttributeToShow.getSelectionModel().getSelectedItem();
        // PAD, attenuationor number of hits are more meaningful for NaN and zero filtering 
        // so choose any of them if they are present in the vox file
        for (String attribute : comboBoxAttributeToShow.getItems()) {
            if (attribute.equalsIgnoreCase(OutputVariable.ATTENUATION_PPL_MLE.getShortName())
                    || attribute.equalsIgnoreCase(OutputVariable.ATTENUATION_FPL_BIASED_MLE.getShortName())
                    || attribute.equalsIgnoreCase(OutputVariable.NUMBER_OF_ECHOES.getShortName())) {
                attributeToFilter = attribute;
                break;
            }
        }
        // add filter
        if (null != attributeToFilter) {

            // default NANs filter on attribute to show
            CombinedFloatFilter nanFilter = new CombinedFloatFilter(new FloatFilter("x", Float.NaN, FloatFilter.EQUAL), null, CombinedFloatFilter.AND);
            listviewFilters.getItems().add(new CombinedFilterItem(attributeToFilter, false,
                    nanFilter.getFilter1(), null, nanFilter.getType()));
            // default zero filter on attribute to show
            CombinedFloatFilter zeroFilter = new CombinedFloatFilter(new FloatFilter("x", 0.f, FloatFilter.EQUAL), null, CombinedFloatFilter.AND);
            listviewFilters.getItems().add(new CombinedFilterItem(attributeToFilter, false,
                    zeroFilter.getFilter1(), null, zeroFilter.getType()));
            // update scene
            updateScene();
        }
    }

    @FXML
    private void onActionButtonApplyVoxelScale(ActionEvent event) {

        Task task = new Task() {

            @Override
            protected Object call() throws Exception {

                try {
                    Float scale = Float.valueOf(textFieldVoxelScale.getText());
                    voxelSpace.scaleVoxel(scale);
                    viewer3D.getJoglContext().refresh();

                } catch (NumberFormatException e) {
                    LOGGER.log(Level.SEVERE, "Cannot parse string value to float", e);
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, "Unknown exception", e);
                }

                return null;
            }
        };

        new Thread(task).start();
    }

    @FXML
    private void onActionButtonResetMinMax(ActionEvent event) {

        textFieldMinValue.setText(String.valueOf(voxelSpace.getRealAttributValueMin()));
        textFieldMaxValue.setText(String.valueOf(voxelSpace.getRealAttributValueMax()));

        voxelSpace.resetRange();
        voxelSpace.updateValue();
        voxelSpace.updateColorValue(voxelSpace.getGradient());
        voxelSpace.updateInstanceColorBuffer();
        viewer3D.getJoglContext().refresh();
    }

    @FXML
    private void onActionButtonApplyMinMax(ActionEvent event) {

        try {
            float min = Float.valueOf(textFieldMinValue.getText());
            float max = Float.valueOf(textFieldMaxValue.getText());

            voxelSpace.setRange(min, max);
            voxelSpace.updateValue();
            voxelSpace.updateColorValue(voxelSpace.getGradient());
            voxelSpace.updateInstanceColorBuffer();
            viewer3D.getJoglContext().refresh();

        } catch (NumberFormatException e) {
            LOGGER.log(Level.SEVERE, "Cannot parse string value to float", e);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Unknown exception", e);
        }
    }

    @FXML
    private void onActionButtonViewTop(ActionEvent event) {

        viewer3D.getScene().getCamera().setViewToTop();
        viewer3D.getJoglContext().refresh();
    }

    @FXML
    private void onActionButtonViewRight(ActionEvent event) {

        viewer3D.getScene().getCamera().setViewToRight();
        viewer3D.getJoglContext().refresh();
    }

    @FXML
    private void onActionButtonViewBottom(ActionEvent event) {

        viewer3D.getScene().getCamera().setViewToBottom();
        viewer3D.getJoglContext().refresh();
    }

    @FXML
    private void onActionButtonViewLeft(ActionEvent event) {

        viewer3D.getScene().getCamera().setViewToLeft();
        viewer3D.getJoglContext().refresh();
    }

    @FXML
    private void onActionButtonViewFront(ActionEvent event) {

        viewer3D.getScene().getCamera().setViewToFront();
        viewer3D.getJoglContext().refresh();
    }

    @FXML
    private void onActionButtonViewBack(ActionEvent event) {

        viewer3D.getScene().getCamera().setViewToBack();
        viewer3D.getJoglContext().refresh();
    }

    @FXML
    private void onActionButtonAddFilterToList(ActionEvent event) {

        String selectedItem = comboBoxScalarField.getSelectionModel().getSelectedItem();

        if (selectedItem != null) {

            final String[] valuesArray = textfieldFilteringRange.getText().replace(" ", "").split(",");

            Set<CombinedFloatFilter> filterValues = new HashSet<>();

            for (String value : valuesArray) {
                try {
                    if (value.contains("[") || value.contains("]")) {
                        int index = value.indexOf("->");
                        if (index != -1) {
                            char firstInequality = value.charAt(0);
                            char secondInequality = value.charAt(value.length() - 1);
                            float firstValue = Float.valueOf(value.substring(1, index));
                            float secondValue = Float.valueOf(value.substring(index + 2, value.length() - 1));
                            int firstInequalityID;
                            switch (firstInequality) {
                                case ']':
                                    firstInequalityID = FloatFilter.GREATER_THAN;
                                    break;
                                case '[':
                                    firstInequalityID = FloatFilter.GREATER_THAN_OR_EQUAL;
                                    break;
                                default:
                                    firstInequalityID = FloatFilter.GREATER_THAN_OR_EQUAL;
                            }
                            int secondInequalityID;
                            switch (secondInequality) {
                                case ']':
                                    secondInequalityID = FloatFilter.LESS_THAN_OR_EQUAL;
                                    break;
                                case '[':
                                    secondInequalityID = FloatFilter.LESS_THAN;
                                    break;
                                default:
                                    secondInequalityID = FloatFilter.LESS_THAN_OR_EQUAL;
                            }
                            filterValues.add(new CombinedFloatFilter(
                                    new FloatFilter("x", firstValue, firstInequalityID),
                                    new FloatFilter("x", secondValue, secondInequalityID), CombinedFloatFilter.AND));
                        }
                    } else {
                        filterValues.add(new CombinedFloatFilter(new FloatFilter("x", Float.valueOf(value), FloatFilter.EQUAL), null, CombinedFloatFilter.AND));
                    }
                } catch (Exception e) {
                    LOGGER.log(Level.WARNING, "Filter syntax not correct: " + textfieldFilteringRange.getText() + ". " + e.getMessage());
                }
            }

            for (CombinedFloatFilter combinedFilter : filterValues) {

                ObservableList<CombinedFilterItem> items = listviewFilters.getItems();

                CombinedFilterItem combinedFilterItem = new CombinedFilterItem(comboBoxScalarField.getSelectionModel().getSelectedItem(), radiobuttonDisplay.isSelected(),
                        combinedFilter.getFilter1(), combinedFilter.getFilter2(), combinedFilter.getType());

                String newFilter = combinedFilterItem.toString();

                boolean addFilter = true;

                for (CombinedFilterItem item : items) {
                    if (item.toString().equals(newFilter)) {
                        addFilter = false;
                    }
                }

                if (addFilter) {
                    listviewFilters.getItems().add(combinedFilterItem);
                }
            }

            updateScene();
        }
    }

    private void updateScene() {
        voxelSpace.setFilters(listviewFilters.getItems());
        voxelSpace.updateColorValue(voxelSpace.getGradient());
        voxelSpace.updateVao();
        viewer3D.getJoglContext().refresh();
    }

    @FXML
    private void onActionButtonRemoveFilterFromList(ActionEvent event) {

        ObservableList<CombinedFilterItem> selectedItems = listviewFilters.getSelectionModel().getSelectedItems();
        listviewFilters.getItems().removeAll(selectedItems);
        updateScene();
    }

    public AnchorPane getAnchorPaneRoot() {
        return anchorPaneRoot;
    }

    @FXML
    private void onActionButtonTakeScreenshot(ActionEvent event) {

        viewer3D.getJoglContext().setTakeScreenShot(true, (BufferedImage image) -> {
            Platform.runLater(() -> {
                File selectedFile = fcScreenshot.showSaveDialog(stage);

                if (selectedFile != null) {
                    try {
                        ImageIO.write(image, "png", selectedFile);
                    } catch (IOException ex) {
                        LOGGER.log(Level.SEVERE, "Cannot write screenshot", ex);
                    }
                }
            });
        });
    }

    public void addSceneObject(SceneObject sceneObject, String name) {

        SceneObjectWrapper sceneObjectWrapper = new SceneObjectWrapper(name, new ProgressBar(100));
        root.getChildren().add(new TreeItem<>(sceneObjectWrapper));
        sceneObjectWrapper.setSceneObject(sceneObject);

        sceneObjectWrapper.selectedProperty.addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
            sceneObjectWrapper.getSceneObject().setVisible(newValue);
            viewer3D.getJoglContext().refresh();
        });
    }

    public void setAttributes(LinkedHashMap<String, Double> attributs) {

        tableviewAttribut.getItems().clear();

        Iterator<Map.Entry<String, Double>> iterator = attributs.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, Double> entry = iterator.next();
            tableviewAttribut.getItems().add(new Attribut(entry.getKey(), entry.getValue()));
        }

    }
}
