/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.amapvox.gui;

import org.amapvox.commons.javafx.io.FileChooserContext;
import org.amapvox.gui.task.TaskElement;
import org.amapvox.commons.Configuration;
import org.amapvox.commons.util.io.file.FileManager;
import org.amapvox.commons.javafx.SelectableMenuButton;
import org.amapvox.voxelfile.VoxelFileHeader;
import org.amapvox.voxelfile.VoxelFileReader;
import org.amapvox.gui.configuration.ConfigurationController;
import org.amapvox.gui.logging.TextAreaAppender;
import org.amapvox.gui.task.AVoxService;
import org.amapvox.gui.task.TaskAdapter;
import org.amapvox.gui.task.TaskElementExecutor;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.logging.Level;
import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.concurrent.Service;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;
import java.awt.Desktop;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.event.Event;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.CustomMenuItem;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuButton;
import javafx.scene.control.TextArea;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.Priority;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import org.apache.log4j.Logger;
import org.jdom2.JDOMException;

/**
 * FXML Controller class
 *
 * @author Julien Heurtebize (julienhtbe@gmail.com)
 */
public class MainFrameController implements Initializable {

    final Logger LOGGER = Logger.getLogger(MainFrameController.class);

    final private List<TaskUI> uiTasks = new LinkedList();
    final private Map<String, MenuItem> newMenuItemMap = new HashMap();

    private Stage stage;

    private PreferencesFrameController preferencesFrameController;
    private SaveOnCloseFrameController saveOnCloseController;

    private Map<CfgFile, CfgUI> configurations;

    private File lastFCAddTask;
    private FileChooser fileChooserOpenConfiguration;
    private FileChooser fileChooserSaveConfiguration;
    private FileChooser fileChooserAddTask;

    final private AtomicInteger fileIndex = new AtomicInteger(0);

    private ResourceBundle rb;

    private Preferences prefs;
    private LinkedHashSet<String> recentFiles;
    private final int maxRecentFiles = 10;

    @FXML
    private Menu newMenu;
    @FXML
    private Menu recentMenu;
    @FXML
    private MenuButton newToolbarButton;
    @FXML
    private Button editToolbarButton;
    @FXML
    private Button closeToolbarButton;
    @FXML
    private Button saveToolbarButton;
    @FXML
    private Button saveAsToolbarButton;
    @FXML
    private Button runToolbarButton;
    @FXML
    private Tab cfgTab;
    @FXML
    private ListView<TaskElement> listViewTaskList;
    @FXML
    private SelectableMenuButton selectorTaskList;
    @FXML
    private TabPane tabPaneIO;
    @FXML
    private TabPane tabPaneEditor;
    @FXML
    private TreeView<File> treeViewOutput;
    @FXML
    private Button clearOutputButton;
    @FXML
    private SplitPane splitPaneVoxelization;
    @FXML
    private TextArea textAreaLog;
    @FXML
    private Button clearLogButton;
    @FXML
    private Button exportLogButton;

    /**
     * Initializes the controller class.
     *
     * @param url
     * @param rb
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {

        this.rb = rb;

        configurations = new LinkedHashMap();

        // save as button disable binding
        saveAsToolbarButton.disableProperty().bind(
                tabPaneEditor.getSelectionModel().selectedItemProperty().isNull());

        // save button disable binding
        tabPaneEditor.getSelectionModel().selectedItemProperty().addListener(
                (ObservableValue<? extends Tab> observable, Tab oldValue, Tab newValue) -> {
                    if (null != newValue) {
                        CfgFile f = findFile(newValue);
                        saveToolbarButton.disableProperty().bind(
                                tabPaneEditor.getSelectionModel().selectedItemProperty().isNull()
                                        .or(f.savedProperty().not())
                                        .or(getCfg(f).getController().changedProperty().not()));
                    }
                }
        );

        // clear task selection when switching from Configuration tab to Output tab
        tabPaneIO.getSelectionModel().selectedItemProperty().addListener(
                (ObservableValue<? extends Tab> observable, Tab oldValue, Tab newValue) -> {
                    if (newValue != cfgTab) {
                        // clear task selection
                        listViewTaskList.getSelectionModel().clearSelection();
                    }
                }
        );

        // feed textAreaLog with LOG4J messages
        TextAreaAppender.setTextArea(textAreaLog);
        LOGGER.info("AMAPVox " + org.amapvox.commons.Util.getVersion());

        /*work around, the divider positions values are defined in the fxml,
        but when the window is initialized the values are lost*/
        Platform.runLater(()
                -> {
            splitPaneVoxelization.setDividerPositions(0.35f);
        });

        // initial set of recent files from preferences
        prefs = Preferences.userRoot().node(this.getClass().getName());
        recentFiles = new LinkedHashSet();
        IntStream.range(0, maxRecentFiles).forEach(i -> {
            String f = prefs.get("recent.file." + i, "");
            if (!f.isBlank()) {
                recentFiles.add(f);
            }
        });
        if (!recentFiles.isEmpty()) {
            recentFiles.forEach(f -> {
                MenuItem recentMenuItem = new MenuItem(f);
                recentMenuItem.setOnAction(event -> {
                    openTask(new CfgFile(new File(f)), false);
                });
                recentMenu.getItems().add(recentMenuItem);
            });
        }
        recentMenu.disableProperty().bind(Bindings.isEmpty(recentMenu.getItems()));

        listViewTaskList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        Util.linkSelectorToList(selectorTaskList, listViewTaskList);
        contextMenuTaskList();

        treeViewOutput.setRoot(new TreeItem());
        treeViewOutput.setShowRoot(false);
        treeViewOutput.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        contextMenuOutputList();

        // custom list cell for listViewProductsFiles 
        treeViewOutput.setCellFactory(tv -> {
            TreeCell<File> cell = new TreeCell<File>() {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy MMM dd HH:mm");

                @Override
                protected void updateItem(File item, boolean empty) {
                    super.updateItem(item, empty);
                    if (null != item) {
                        Tooltip tooltip = new Tooltip(item.getAbsolutePath());
//                        Util.hackTooltipStartTiming(tooltip, 0);
                        setTooltip(tooltip);
                        try {
                            CfgFile file = configurations.keySet().stream()
                                    .filter(f -> f.getFile().equals(item))
                                    .findFirst().get();
                            setText(item.getName());
                            setStyle("-fx-font-weight: bold;");
                            String iconURI = getCfg(file).getIcon();
                            ImageView icon = new ImageView(new Image(MainFrameController.class.getResource(iconURI).toExternalForm()));
                            icon.setFitHeight(24);
                            icon.setFitWidth(24);
                            setGraphic(icon);
                        } catch (NoSuchElementException ex) {
                            StringBuilder sb = new StringBuilder();
                            sb.append("(");
                            sb.append(sdf.format(item.lastModified()));
                            sb.append(", ");
                            sb.append(Util.humanReadableByteCount(item.length(), true));
                            sb.append(") ");
                            sb.append(item.getName());
                            setText(sb.toString());
                            setStyle("-fx-font-weight: normal;");
                            setGraphic(null);
                        }
                    } else if (empty) {
                        setText("");
                        setTooltip(null);
                        setGraphic(null);
                    }
                }
            };
            return cell;
        });

        fileChooserOpenConfiguration = new FileChooser();
        fileChooserOpenConfiguration.setTitle("Choose configuration file");
        fileChooserOpenConfiguration.getExtensionFilters().addAll(
                new ExtensionFilter("XML files (*.xml)", "*.xml"),
                new ExtensionFilter("All Files", "*.*"));

        fileChooserSaveConfiguration = new FileChooser();
        fileChooserSaveConfiguration.setTitle("Choose output file");
        fileChooserSaveConfiguration.getExtensionFilters().addAll(
                new ExtensionFilter("XML files (*.xml)", "*.xml"),
                new ExtensionFilter("All Files", "*.*"));

        fileChooserAddTask = new FileChooser();
        fileChooserAddTask.setTitle("Choose parameter file");
        fileChooserAddTask.getExtensionFilters().addAll(
                new ExtensionFilter("All Files", "*.*"),
                new ExtensionFilter("XML Files (*.xml)", "*.xml"));

        preferencesFrameController = PreferencesFrameController.newInstance(prefs);

        /**
         * DRAG GESTURES
         */
        Util.setDragGestureEvents(listViewTaskList, file -> openTask(new CfgFile(file), false));
        // drag detected
        treeViewOutput.setOnDragDetected((MouseEvent event) -> {
            Dragboard db = treeViewOutput.startDragAndDrop(TransferMode.COPY);
            ClipboardContent content = new ClipboardContent();
            content.putFiles(treeViewOutput.getSelectionModel().getSelectedItems()
                    .stream().map(item -> item.getValue())
                    .collect(Collectors.toList())
            );
            db.setContent(content);
            event.consume();
        });

        BooleanBinding binding1 = Bindings.isEmpty(listViewTaskList.getSelectionModel().getSelectedItems());
        closeToolbarButton.disableProperty().bind(binding1);

        BooleanBinding binding2 = Bindings.createBooleanBinding(() -> {
            return listViewTaskList.getSelectionModel().getSelectedItems().stream()
                    .allMatch(taskElement -> taskElement.isDisabled());
        }, listViewTaskList.getSelectionModel().getSelectedItems());
        editToolbarButton.disableProperty().bind(binding2);
        runToolbarButton.disableProperty().bind(binding2);

        newToolbarButton.getStyleClass().remove("menu-button");
        newToolbarButton.getStyleClass().add("button");
        newToolbarButton.disableProperty().bind(Bindings.isEmpty(newToolbarButton.getItems()));
        newMenu.disableProperty().bind(Bindings.isEmpty(newMenu.getItems()));

        clearOutputButton.disableProperty().bind(treeViewOutput.getRoot().leafProperty());

        clearLogButton.disableProperty().bind(textAreaLog.textProperty().isEmpty());
        exportLogButton.disableProperty().bind(textAreaLog.textProperty().isEmpty());
    }

    void updateNewMenuItem(TaskUI task) {

        MenuItem menuItem = newMenuItemMap.get(task.getClassName());
        if (newMenu.getItems().contains(menuItem)) {
            // remove item
            newMenu.getItems().remove(menuItem);
            newToolbarButton.getItems().remove(menuItem);
        } else {
            // add item
            Comparator<MenuItem> menuItemComparator = (item1, item2) -> {
                return item1.getText().compareTo(item2.getText());
            };
            newToolbarButton.getItems().add(menuItem);
            newToolbarButton.getItems().sort(menuItemComparator);
            newMenu.getItems().add(menuItem);
            newMenu.getItems().sort(menuItemComparator);
        }
    }

    void updateRecentMenu(CfgFile f) {

        recentFiles.addFirst(f.getFile().getAbsolutePath());
        recentMenu.getItems().clear();
        int i = 0;
        for (String rf : recentFiles) {
            if (i >= maxRecentFiles) {
                break;
            }
            MenuItem recentMenuItem = new MenuItem(rf);
            recentMenuItem.setOnAction(event -> {
                openTask(new CfgFile(new File(rf)), false);
            });
            recentMenu.getItems().add(recentMenuItem);
            prefs.put("recent.file." + i, rf);
            i++;
        }
    }

    void addTaskUI(Class<? extends Configuration> clazz, String fxml, String icon, RepoStatus status) throws Exception {

        TaskUI task = new TaskUI(clazz, fxml, icon, status);
        uiTasks.add(task);
        newMenuItemMap.put(task.getClassName(), newMenuItem(task));
        if (status.equals(RepoStatus.ACTIVE)) {
            updateNewMenuItem(task);
        }
        preferencesFrameController.addTasks(task, this);
    }

    private void contextMenuTaskList() {

        // edit task
        MenuItem editMenuItem = new MenuItem("Edit");
        editMenuItem.setOnAction((ActionEvent event) -> {
            List<CfgFile> files = listViewTaskList.getSelectionModel().getSelectedItems()
                    .stream()
                    .map(task -> task.getLinkedFile())
                    .collect(Collectors.toList());
            files.forEach(f -> editTask(f));
        });

        // copy path to clipboard
        MenuItem copyPathMenuItem = new MenuItem("Copy path to clipboard");
        copyPathMenuItem.setOnAction((ActionEvent event) -> {
            StringSelection path = new StringSelection(listViewTaskList.getSelectionModel().getSelectedItem().getLinkedFile().getFile().getAbsolutePath());
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(path, null);
        });

        // open folder
        MenuItem openFolderMenuItem = new MenuItem("Open folder");
        openFolderMenuItem.setOnAction((ActionEvent event) -> {
            final File file = listViewTaskList.getSelectionModel().getSelectedItem().getLinkedFile().getFile();
            if (null != file) {
                if (Desktop.isDesktopSupported()) {
                    new Thread(() -> {
                        try {
                            Desktop.getDesktop().open(file.getParentFile());
                        } catch (IOException ex) {
                            LOGGER.error("Cannot open folder " + file.getParent(), ex);
                        }
                    }).start();
                }
            }
        });

        // open with text editor
        MenuItem showSourceMenuItem = new MenuItem("Show source");
        showSourceMenuItem.setOnAction((ActionEvent event) -> {
            final File file = listViewTaskList.getSelectionModel().getSelectedItem().getLinkedFile().getFile();
            if (null != file) {
                Stage xmlStage = new Stage();
                VBox box = new VBox();
                box.setPadding(new Insets(10));
                box.setAlignment(Pos.TOP_RIGHT);
                box.setSpacing(10);
                VBox.setVgrow(box, Priority.ALWAYS);
                Label label = new Label("Configuration file: " + file.getAbsolutePath());
                Text xmlText = new Text();
                StringBuilder sb = new StringBuilder();
                try {
                    Files.lines(file.toPath()).forEach((line) -> {
                        sb.append(line).append('\n');
                    });
                } catch (IOException ex) {
                    sb.append("Failed to read configuration file...").append('\n');

                }
                xmlText.setText(sb.toString());
                ScrollPane scrollPane = new ScrollPane();
                scrollPane.setMaxHeight(Double.MAX_VALUE);
                scrollPane.setFitToWidth(true);
                scrollPane.setContent(xmlText);
                Button btnClose = new Button();
                btnClose.setText("Close");
                btnClose.setOnAction((ActionEvent) -> {
                    xmlStage.close();
                });
                box.getChildren().add(label);
                box.getChildren().add(scrollPane);
                box.getChildren().add(btnClose);
                Scene scene = new Scene(box, 500, 500);
                xmlStage.setScene(scene);
                xmlStage.initModality(Modality.APPLICATION_MODAL);
                xmlStage.show();
            }
        });

        // add context menu
        listViewTaskList.setOnContextMenuRequested((ContextMenuEvent event) -> {
            if (listViewTaskList.getSelectionModel().getSelectedIndices().size() == 1) {
                ContextMenu contextMenuTaskList = new ContextMenu();
                contextMenuTaskList.getItems().addAll(editMenuItem, copyPathMenuItem, openFolderMenuItem, showSourceMenuItem);
                contextMenuTaskList.show(listViewTaskList, event.getScreenX(), event.getScreenY());
            }
        });
    }

    private void contextMenuOutputList() {

        ContextMenu contextMenuProductsList = new ContextMenu();

        // copy path to clipboard
        MenuItem copyPathMenuItem = new MenuItem("Copy path to clipboard");
        copyPathMenuItem.setOnAction((ActionEvent event) -> {
            StringSelection path = new StringSelection(treeViewOutput.getSelectionModel().getSelectedItem().getValue().getAbsolutePath());
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(path, null);
        });

        // remove output file
        MenuItem removeMenuItem = new MenuItem("Remove");
        removeMenuItem.setOnAction((ActionEvent event) -> {
            treeViewOutput.getSelectionModel().getSelectedItems()
                    .forEach(item -> removeOutputItem(item));
        });

        MenuItem imageMenuItem = new MenuItem("Open with image viewer");
        imageMenuItem.setOnAction((ActionEvent event)
                -> {
            File selectedFile = treeViewOutput.getSelectionModel().getSelectedItem().getValue();

            showImage(selectedFile);
        });

        MenuItem voxelspaceMenuItem = new MenuItem("Show header");

        voxelspaceMenuItem.setOnAction((ActionEvent event)
                -> {
            Alert alert = new Alert(AlertType.INFORMATION);

            File selectedItem = treeViewOutput.getSelectionModel().getSelectedItem().getValue();

            if (selectedItem != null) {
                try {
                    VoxelFileHeader header = new VoxelFileReader(selectedItem).getHeader();
                    alert.setTitle("Voxel space");
                    alert.setHeaderText("Voxel space header");
                    alert.setContentText(header.toString());
                    alert.show();
                } catch (Exception ex) {
                    Util.showErrorDialog(stage, ex, "[Voxel space]");
                }

            }
        });

        final MenuItem menuItemOpenContainingFolder = new MenuItem("Open item location");

        menuItemOpenContainingFolder.setOnAction((ActionEvent event)
                -> {
            final File selectedItem = treeViewOutput.getSelectionModel().getSelectedItem().getValue();

            if (selectedItem != null) {
                if (Desktop.isDesktopSupported()) {
                    new Thread(()
                            -> {
                        try {
                            Desktop.getDesktop().open(selectedItem.getParentFile());
                        } catch (IOException ex) {
                            LOGGER.error("Cannot open directory " + selectedItem);
                        }
                    }).start();
                }

            }
        });

        treeViewOutput.setOnContextMenuRequested((ContextMenuEvent event)
                -> {
            if (treeViewOutput.getSelectionModel().getSelectedIndices().size() == 1) {

                File selectedFile = treeViewOutput.getSelectionModel().getSelectedItem().getValue();
                String extension = FileManager.getExtension(selectedFile);
                contextMenuProductsList.getItems().clear();
                contextMenuProductsList.getItems().addAll(removeMenuItem, copyPathMenuItem, menuItemOpenContainingFolder);
                switch (extension) {
                    case ".png":
                    case ".bmp":
                    case ".jpg":
                        contextMenuProductsList.getItems().add(imageMenuItem);
                        break;
                    case ".vox":
                        if (VoxelFileReader.isValid(selectedFile)) {
                            contextMenuProductsList.getItems().add(voxelspaceMenuItem);
                        }
                }
                contextMenuProductsList.show(treeViewOutput, event.getScreenX(), event.getScreenY());
            }
        });
    }

    private MenuItem newMenuItem(TaskUI task) throws Exception {

        Configuration cfg = Configuration.newInstance(task.getClassName());

        Label menuLabel = new Label(cfg.getLongName());
        menuLabel.setPrefWidth(300);
        ImageView icon = new ImageView(new Image(MainFrameController.class.getResource(task.getIcon()).toExternalForm()));
        menuLabel.setGraphic(icon);

        Tooltip tooltip = new Tooltip();
        tooltip.setText(cfg.getDescription());
        Util.hackTooltipStartTiming(tooltip, 0L);

        CustomMenuItem item = new CustomMenuItem(menuLabel);
        item.setMnemonicParsing(false);
        Tooltip.install(item.getContent(), tooltip);
        item.setText(cfg.getLongName()); // trick to be able to sort menuItem
        item.setOnAction(event -> {
            LOGGER.debug("[" + cfg.getLongName() + "] New configuration.");
            // update file chooser to last opened configuration file
            if (null != lastFCAddTask) {
                fileChooserSaveConfiguration.setInitialDirectory(lastFCAddTask.getParentFile());
                fileChooserSaveConfiguration.setInitialFileName(lastFCAddTask.getName());
            }
            try {
                CfgFile cfgFile = CfgFile.create(fileIndex.incrementAndGet());
                cfg.write(cfgFile.getFile(), true);
                LOGGER.info(cfg.getLongName() + " '" + cfgFile.getName() + "' created");
                openTask(cfgFile, true);
            } catch (IOException | JDOMException ex) {
                Util.showErrorDialog(stage, ex, null);
            }
        });
        return item;
    }

    @FXML
    private void onActionMenuEditTask(ActionEvent event) {
        getSelectedTasks().forEach(f -> editTask(f));
    }

    @FXML
    private void onActionMenuAddTask(ActionEvent event) {
        addTask(false);
    }

    @FXML
    private void onActionMenuSaveTask(ActionEvent event) {
        CfgFile f = getSelectedTab();
        if (null != f) {
            if (!f.savedProperty().getValue()) {
                // not yet saved to file, switch to save as function
                saveAsTask(f);
            } else {
                saveTask(f);
            }
        }
    }

    @FXML
    private void onActionMenuSaveAsTask(ActionEvent event) {
        CfgFile f = getSelectedTab();
        if (null != f) {
            saveAsTask(f);
        }
    }

    private boolean saveAsTask(CfgFile source) {

        // update file chooser to last opened configuration file
        fileChooserSaveConfiguration.setInitialDirectory(source.getFile().getParentFile());
        fileChooserSaveConfiguration.setInitialFileName(source.getName());

        File selectedFile = fileChooserSaveConfiguration.showSaveDialog(stage);
        if (selectedFile != null) {
            // add xml extension
            if (!selectedFile.getName().endsWith(".xml")) {
                selectedFile = new File(selectedFile.getAbsolutePath() + ".xml");
            }
            // save as itself == save
            if (selectedFile.equals(source.getFile())) {
                return saveTask(source);
            }
            try {
                CfgFile target = new CfgFile(selectedFile);
                // if target is alread opened, close it first
                if (configurations.containsKey(target)) {
                    getCfg(target).getController().unload();
                    removeTask(target, false);
                }
                // swap source and target
                CfgUI sourceUI = getCfg(source);
                sourceUI.updateLinkedFile(target);
                configurations.remove(source);
                configurations.put(target, sourceUI);
                // save target file
                Files.copy(
                        Paths.get(source.getFile().toURI()),
                        Paths.get(selectedFile.toURI()),
                        StandardCopyOption.REPLACE_EXISTING);
                target.setSaved();
                // clear and select current tab to update the save button binding
                int tabIndex = tabPaneEditor.getSelectionModel().getSelectedIndex();
                tabPaneEditor.getSelectionModel().clearSelection();
                tabPaneEditor.getSelectionModel().select(tabIndex);
                // save current modifications if any
                if (sourceUI.getController().changedProperty().get()) {
                    saveTask(target, true);
                }
                LOGGER.info(source.getName() + " saved as " + target.getName() + ".");
                if (source.savedProperty().get()) {
                    // re open source unless it was a temp file
                    int pos = listViewTaskList.getItems().indexOf(sourceUI.getTask());
                    openTask(source, pos, false, true);
                    listViewTaskList.getSelectionModel().clearAndSelect(pos + 1);
                }
                return true;
            } catch (Exception ex) {
                Util.showErrorDialog(stage,
                        new IOException("Cannot write configuration file.", ex), getCfg(source).getClassName());
            }
        }
        return false;
    }

    @FXML
    private void onActionMenuExit(ActionEvent event) {
        Platform.exit();
        System.exit(0);
    }

    @FXML
    private void onActionMenuClearWindow(ActionEvent event) {
        try {
            resetComponents();
        } catch (Exception ex) {
            java.util.logging.Logger.getLogger(MainFrameController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @FXML
    private void onActionMenuPreferences(ActionEvent event) {
        preferencesFrameController.getStage().show();
    }

    @FXML
    private void onActionMenuAbout(ActionEvent event) {

        try {
            FXMLLoader loader = new FXMLLoader(MainFrameController.class.getResource("fxml/AboutDialog.fxml"));
            Parent root = loader.load();
            AboutDialogController controller = loader.getController();
            Stage about = new Stage();
            about.setScene(new Scene(root));
            about.setTitle("About");
            about.initModality(Modality.APPLICATION_MODAL);
            about.getIcons().addAll(stage.getIcons());
            controller.setStage(about);
            about.show();
        } catch (IOException ex) {
            LOGGER.error("AboutFrame error", ex);
        }
    }

    @FXML
    private void onActionMenuRemoveTask(ActionEvent event) {
        getSelectedTasks().forEach(f -> removeTask(f, event));
    }

    @FXML
    private void onActionMenuRunTask(ActionEvent event) {

        List<TaskElement> taskElements = listViewTaskList.getSelectionModel().getSelectedItems()
                .stream()
                .filter(task -> !(task.isTaskRunning() | task.isTaskDisable()))
                .collect(Collectors.toList());
        runTask(taskElements);
    }

    @FXML
    private void onActionButtonClearOutput(ActionEvent event) {
        treeViewOutput.getRoot().getChildren().clear();
    }

    @FXML
    private void onActionButtonClearLog(ActionEvent event) {
        textAreaLog.setText("");
    }

    @FXML
    private void onActionButtonExportLog(ActionEvent event) {

        StringBuilder fname = new StringBuilder();
        fname.append("amapvox-").append(org.amapvox.commons.Util.getVersion()).append("_");
        fname.append(LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE));
        fname.append("-");
        fname.append(LocalTime.now().format(DateTimeFormatter.ofPattern("HHmm")));
        fname.append("_log.txt");
        FileChooserContext fc = new FileChooserContext(fname.toString());

        File file = fc.showSaveDialog(stage);
        if (null != file) {
            try (FileWriter writer = new FileWriter(file)) {
                writer.write(textAreaLog.getText());
            } catch (IOException ex) {
                LOGGER.error("Failed to save log file", ex);
            }
            LOGGER.info("Log file saved " + file.getAbsolutePath());
        }
    }

    private List<CfgFile> getSelectedTasks() {
        return listViewTaskList.getSelectionModel().getSelectedItems()
                .stream()
                .map(task -> task.getLinkedFile())
                .collect(Collectors.toList());
    }

    private CfgFile getSelectedTab() {

        if (null != tabPaneEditor.getSelectionModel().getSelectedItem()) {
            return findFile(tabPaneEditor.getSelectionModel().getSelectedItem());
        }
        return null;
    }

    private CfgFile findFile(Tab tab) {

        return configurations.keySet()
                .stream()
                .filter(file -> getCfg(file).getTab().equals(tab))
                .findFirst().get();
    }

    private void showImage(File file) {

        try {
            ImageView iv = new ImageView(new Image(file.toURI().toURL().toString()));
            iv.setPreserveRatio(true);
            Stage imgStage = new Stage();

            final DoubleProperty zoomProperty = new SimpleDoubleProperty(200);

            zoomProperty.addListener((javafx.beans.Observable observable)
                    -> {
                iv.setFitWidth(zoomProperty.get() * 4);
                iv.setFitHeight(zoomProperty.get() * 3);
            });

            ScrollPane sp = new ScrollPane(iv);
            imgStage.addEventFilter(ScrollEvent.ANY, (ScrollEvent event)
                    -> {
                if (event.getDeltaY() > 0) {
                    zoomProperty.set(zoomProperty.get() * 1.1);
                } else if (event.getDeltaY() < 0) {
                    zoomProperty.set(zoomProperty.get() / 1.1);
                }
            });

            imgStage.setScene(new Scene(new Group(sp)));

            imgStage.sizeToScene();
            imgStage.initModality(Modality.APPLICATION_MODAL);
            imgStage.show();
        } catch (IOException ex) {
            Util.showErrorDialog(stage, ex, null);
        }

    }

    public void setStage(final Stage stage) {

        this.stage = stage;

        this.stage.setOnCloseRequest(windowEvent -> {
            List<CfgFile> modified = configurations.values()
                    .stream()
                    .filter(o -> (null != o.getController()) && (o.getController().changedProperty().get() & o.getTask().getLinkedFile().savedProperty().getValue()))
                    .map(o -> o.getTask().getLinkedFile())
                    .collect(Collectors.toList());
            if (!modified.isEmpty()) {
                try {
                    FXMLLoader loader = new FXMLLoader(MainFrameController.class.getResource("fxml/SaveOnCloseFrame.fxml"));
                    Parent root = loader.load();
                    saveOnCloseController = loader.getController();
                    saveOnCloseController.setMainController(this);
                    modified.forEach(f -> saveOnCloseController.addFile(f));
                    Stage dialog = new Stage();
                    dialog.setScene(new Scene(root));
                    dialog.setTitle("Save");
                    saveOnCloseController.setStage(dialog);
                    dialog.setOnHidden(windowEvent1 -> {
                        if (saveOnCloseController.isCancelled()) {
                            windowEvent.consume();
                        }
                    });
                    dialog.showAndWait();
                } catch (IOException ex) {
                    LOGGER.error("Cannot load fxml file", ex);
                }
            }
        });

    }

    private void resetComponents() throws Exception {

        Platform.runLater(()
                -> {
            try {
                FXMLLoader loader = new FXMLLoader(MainFrameController.class.getResource("fxml/MainFrame.fxml"));
                Parent root = loader.load();

                Scene scene = new Scene(root);

                scene.getStylesheets().add(MainFX.class.getResource("styles/Styles.css").toExternalForm());

                //stage.setTitle("AMAPVox");
                stage = new Stage();
                stage.setTitle("AMAPVox " + org.amapvox.commons.Util.getVersion());
                stage.setScene(scene);
            } catch (IOException ex) {
                java.util.logging.Logger.getLogger(MainFrameController.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
    }

    private void addTaskListeners(TaskElement task) {

        // listen to task success and retrieve output files
        task.addTaskListener(new TaskAdapter() {

            @Override
            public void onStarted() {
                CfgFile file = task.getLinkedFile();
                if (getCfg(file).getController().changedProperty().get()) {
                    saveTask(file);
                }
            }

            @Override
            public void onSucceeded(Service service) {
                Platform.runLater(()
                        -> {
                    // single output file
                    if ((service.getValue() instanceof File outputFile) && (outputFile != null)) {
                        addFileToOutput(task.getLinkedFile().getFile(), outputFile);
                    }
                    // multiple output files
                    if ((service.getValue() instanceof File[] outputFiles) && (null != outputFiles)) {
                        for (File outputFile : outputFiles) {
                            addFileToOutput(task.getLinkedFile().getFile(), outputFile);
                        }
                    }
                });
            }

            @Override
            public void onFailed(Throwable ex) {
                Util.showErrorDialog(stage, ex, null);
            }
        });

    }

    private void openTask(CfgFile file, boolean edit) {
        openTask(file, -1, edit, false);
    }

    private void openTask(CfgFile file, int position, boolean edit, boolean quiet) {

        if (!configurations.containsKey(file)) {
            TaskElement taskElement = null;
            try {
                Configuration cfg = Configuration.newInstance(file.getFile());
                file.setDeprecated(cfg.isDeprecated());
                // ncpu available for the task
                int ncpu = preferencesFrameController.getNCPU();
                // create new task element
                taskElement = new TaskElement(file,
                        new AVoxService(cfg.getTaskClass(), file, ncpu));
                addTaskListeners(taskElement);
                // add it to listView
                CfgUI cfgUI = new CfgUI(cfg.getClass());
                cfgUI.setTask(taskElement);
                configurations.put(file, cfgUI);
                if (position < 0 || position > listViewTaskList.getItems().size()) {
                    listViewTaskList.getItems().add(taskElement);
                } else {
                    listViewTaskList.getItems().add(position, taskElement);
                }
                // preload task controller
                if (cfg.isDeprecated()) {
                    if (!quiet) {
                        LOGGER.info(file.getName() + " is deprecated.");
                        LOGGER.info(cfg.getDescription());
                    }
                    taskElement.setButtonDisable(true);
                    taskElement.setDisable(true);
                } else {
                    taskElement.setButtonDisable(file.savedProperty().not().get());
                    preloadTask(file, edit);
                    if (!quiet) {
                        LOGGER.info(file.getName() + " opened.");
                    }
                }
            } catch (Exception ex) {
                Util.showErrorDialog(stage, ex, null);
                listViewTaskList.getItems().remove(taskElement);
                configurations.remove(file);
                return;
            }
        } else if (edit) {
            editTask(file);
        }
        tabPaneIO.getSelectionModel().select(cfgTab);
        listViewTaskList.getSelectionModel().clearSelection();
        if (!getCfg(file).getTask().isDisabled()) {
            listViewTaskList.getSelectionModel().select(getCfg(file).getTask());
        }
        updateRecentMenu(file);

    }

    private void setTaskModified(CfgFile file, boolean modified) {

        try {
            Tab tab = getCfg(file).getTab();
            if (modified) {
                tab.setText("*" + file.getName());
                tab.setStyle("-fx-font-weight: bold;");
                getCfg(file).getTask().setModified(true);
            } else {
                tab.setText(file.getName());
                tab.setStyle("-fx-font-weight: normal;");
                getCfg(file).getTask().setModified(false);
            }
        } catch (NullPointerException ex) {
        }

    }

    private void preloadTask(CfgFile file, boolean thenEdit) throws IOException {

        Service s = new Service() {

            @Override
            protected Task createTask() {
                return new Task() {

                    FXMLLoader loader;
                    Parent frame;

                    @Override
                    protected Object call() throws Exception {
                        String url = getCfg(file).getFxml();
                        loader = new FXMLLoader(MainFrameController.class.getResource(url), rb);
                        frame = loader.load();
                        return null;
                    }

                    @Override
                    protected void succeeded() {
                        ConfigurationController controller = loader.getController();
                        controller.setStage(stage);
                        Tab tab = new Tab();
                        tab.setContent(frame);
                        ConfigurationChangeListener listener = new ConfigurationChangeListener(file);
                        controller.changedProperty().addListener(listener);
                        String iconURI = getCfg(file).getIcon();
                        Image icon = new Image(MainFrameController.class.getResource(iconURI).toExternalForm());
                        tab.setGraphic(new ImageView(icon));
                        getCfg(file).setTab(tab);
                        getCfg(file).setController(controller);
                        getCfg(file).setListener(listener);
                        getCfg(file).getTask().setTaskIcon(icon);
                        getCfg(file).updateLinkedFile(file);
                        setTaskModified(file, false);
                        // edit ?
                        if (thenEdit) {
                            editTask(file);
                        }
                    }

                    @Override
                    protected void failed() {
                        LOGGER.error("PRELOAD FAILED", getException());
                    }
                };
            }
        };

        s.start();
    }

    private void runTask(List<TaskElement> taskElements) {

        TaskElementExecutor executor;
        int ncpu = 1;
        if (taskElements.size() > 1) {
            ChoiceDialog<String> choiceDialog = new ChoiceDialog<>("Sequential execution",
                    "Sequential execution", "Parallel execution");
            choiceDialog.showAndWait();
            String result = choiceDialog.getResult();
            if (result != null) {
                ncpu = result.equalsIgnoreCase("Sequential execution")
                        ? 1 : preferencesFrameController.getNCPU();
            }
        }
        executor = new TaskElementExecutor(ncpu, taskElements);
        executor.execute();
    }

    private boolean saveTask(CfgFile file, boolean quiet) {

        try {
            getCfg(file).getController().save(file.getFile());
            setTaskModified(file, false);
            getCfg(file).getTask().setButtonDisable(false);
            if (!quiet) {
                LOGGER.info(file.getName() + " saved.");
            }
            return true;
        } catch (Exception ex) {
            Util.showErrorDialog(stage,
                    new IOException("Cannot write configuration file.", ex), getCfg(file).getClassName());
        }
        getCfg(file).getTask().setButtonDisable(true);
        return false;
    }

    protected boolean saveTask(CfgFile file) {
        return saveTask(file, false);
    }

    private void addTask(boolean edit) {

        if (lastFCAddTask != null) {
            fileChooserAddTask.setInitialDirectory(lastFCAddTask.getParentFile());
        }

        List<File> selectedFiles = fileChooserAddTask.showOpenMultipleDialog(stage);

        if (selectedFiles != null) {
            lastFCAddTask = selectedFiles.get(0).getAbsoluteFile();
            selectedFiles.forEach(f -> openTask(new CfgFile(f), edit));
        }
    }

    private void editTask(CfgFile file) {

        Tab tab = getCfg(file).getTab();
        if (null != tab && !tabPaneEditor.getTabs().contains(tab)) {
            if (!getCfg(file).controller.isLoaded()) {
                // load configuration 
                try {
                    Configuration cfg = Configuration.newInstance(file.getFile());
                    if (!cfg.isEmpty()) {
                        getCfg(file).getController().load(file.getFile());
                    }
                } catch (Exception e) {
                    LOGGER.error("File " + file.getName() + ". Loading error.", e);
                    Alert alert = new Alert(AlertType.CONFIRMATION);
                    alert.setHeaderText(file.getName() + " loading error");
                    StringBuilder str = new StringBuilder();
                    str.append("Error:").append('\n')
                            .append(e.getMessage()).append('\n').append('\n')
                            .append("Edit the file anyway?");
                    alert.setContentText(str.toString());
                    Optional<ButtonType> result = alert.showAndWait();
                    if (result.get() == ButtonType.CANCEL) {
                        // do not edit
                        return;
                    } else if (result.get() == ButtonType.OK) {
                        // edit but cannot run
                        getCfg(file).getTask().setButtonDisable(true);
                    }
                }
            }
            // add the tab
            tabPaneEditor.getTabs().add(tab);
        }
        // select the tab
        tabPaneEditor.getSelectionModel().select(tab);
    }

    private void checkSaveTask(CfgFile file, Event e) {

        if (null != getCfg(file).getController()) {
            if (getCfg(file).getController().changedProperty().get()) {
                // save modified file ?
                ButtonType saveButtonType = new ButtonType("Save", ButtonData.YES);
                ButtonType discardButtonType = new ButtonType("Discard", ButtonData.NO);
                ButtonType cancelButtonType = ButtonType.CANCEL;
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                        "File " + file.getName() + " is modified. Save?",
                        discardButtonType, saveButtonType, cancelButtonType
                );
                alert.setTitle("Question");
                Optional<ButtonType> result = alert.showAndWait();
                if (result.isPresent()) {
                    if (result.get() == saveButtonType) {
                        // save task
                        saveTask(file);
                    } else if (result.get() == discardButtonType) {
                        // discard change
                        getCfg(file).getController().unload();
                    } else if (result.get() == cancelButtonType) {
                        // cancel close request
                        e.consume();
                    }
                } else {
                    // cancel close request
                    e.consume();
                }
            }
        }
    }

    protected void removeTask(CfgFile file, ActionEvent event) {

        Tab tab = getCfg(file).getTab();
        if ((null != tab) && tabPaneEditor.getTabs().contains(tab)) {
            // close tab
            checkSaveTask(file, event);
        }
        // remove from list
        if (!event.isConsumed()) {
            removeTask(file, false);
        }
    }

    private void removeTask(CfgFile file, boolean quiet) {

        Tab tab = getCfg(file).getTab();
        tabPaneEditor.getTabs().remove(tab);
        listViewTaskList.getItems().remove(getCfg(file).getTask());
        configurations.remove(file);
        if (!quiet) {
            LOGGER.info(file.getName() + " closed.");
        }
    }

    private void addFileToOutput(File cfg, File output) {

        if (Files.exists(output.toPath())) {
            boolean cfgExist = false;
            for (TreeItem<File> cfgItem : treeViewOutput.getRoot().getChildren()) {
                if (cfgItem.getValue().equals(cfg)) {
                    boolean outputExist = false;
                    for (TreeItem<File> outputItem : cfgItem.getChildren()) {
                        if (outputItem.getValue().equals(output)) {
                            outputItem.setValue(output);
                            outputExist = true;
                            break;
                        }
                    }
                    if (!outputExist) {
                        cfgItem.getChildren().add(new TreeItem(output));
                        cfgItem.setExpanded(true);
                    }
                    cfgExist = true;
                    break;
                }
            }
            if (!cfgExist) {
                TreeItem<File> cfgItem = new TreeItem(cfg);
                cfgItem.setExpanded(true);
                cfgItem.getChildren().add(new TreeItem(output));
                treeViewOutput.getRoot().getChildren().add(cfgItem);
            }
        }
    }

    private void removeOutputItem(TreeItem<File> removeItem) {
        removeOutputItem(treeViewOutput.getRoot(), removeItem);
    }

    private boolean removeOutputItem(TreeItem<File> parent, TreeItem<File> removeItem) {

        boolean found = false;
        // work on first level
        for (TreeItem<File> item : parent.getChildren()) {
            if (item.getValue().equals(removeItem.getValue())) {
                found = true;
                break;
            }
        }
        if (found) {
            parent.getChildren().remove(removeItem);
            if (parent.isLeaf()) {
                try {
                    parent.getParent().getChildren().remove(parent);
                } catch (NullPointerException ex) {
                }
            }
            return true;
        } else {
            // not found on first level, go to next level
            if (parent.getChildren()
                    .stream()
                    .anyMatch(item -> (!item.isLeaf() && removeOutputItem(item, removeItem)))) {
                return true;
            }
        }
        // removeItem not found
        return false;
    }

    public Stage getStage() {
        return stage;
    }

    private CfgUI getCfg(CfgFile cfgFile) {
        return configurations.get(cfgFile);
    }

    private class CfgUI {

        final private String className;
        final private String fxml;
        final private String icon;
        private Tab tab;
        private ConfigurationController controller;
        private ConfigurationChangeListener changeListener;
        private TaskElement task;

        CfgUI(Class clazz) {
            this.className = clazz.getCanonicalName();
            this.fxml = findTaskUI(className).getFxml();
            this.icon = findTaskUI(className).getIcon();
        }

        private TaskUI findTaskUI(String className) {
            return uiTasks.stream()
                    .filter(t -> t.getClassName().equals(className))
                    .findFirst().get();
        }

        private void updateLinkedFile(CfgFile file) {

            // tab title
            tab.setText(file.getName());
            // configuration change listener
            changeListener.setFile(file);
            // check save on close
            tab.setOnCloseRequest(e -> {
                checkSaveTask(file, e);
            });
            // tool tip
            Tooltip tooltip = new Tooltip(file.getFile().getAbsolutePath());
            Util.hackTooltipStartTiming(tooltip, 0);
            tab.setTooltip(tooltip);
            // edit on double click
            task.setOnMouseClicked(mouseEvent -> {
                if (mouseEvent.getButton().equals(MouseButton.PRIMARY)) {
                    if (mouseEvent.getClickCount() == 2) {
                        editTask(file);
                    }
                }
            });
            task.updateFile(file);
        }

        /**
         * @return the className
         */
        public String getClassName() {
            return className;
        }

        /**
         * @return the fxml
         */
        public String getFxml() {
            return fxml;
        }

        /**
         * @return the icon
         */
        public String getIcon() {
            return icon;
        }

        /**
         * @return the tab
         */
        public Tab getTab() {
            return tab;
        }

        /**
         * @param tab the tab to set
         */
        public void setTab(Tab tab) {
            this.tab = tab;
        }

        /**
         * @return the controller
         */
        public ConfigurationController getController() {
            return controller;
        }

        /**
         * @param controller the controller to set
         */
        public void setController(ConfigurationController controller) {
            this.controller = controller;
        }

        /**
         * @return the task
         */
        public TaskElement getTask() {
            return task;
        }

        /**
         * @param task the task to set
         */
        public void setTask(TaskElement task) {
            this.task = task;
        }

        /**
         * @return the listener
         */
        public ConfigurationChangeListener getListener() {
            return changeListener;
        }

        /**
         * @param listener the listener to set
         */
        public void setListener(ConfigurationChangeListener listener) {
            this.changeListener = listener;
        }
    }

    private class ConfigurationChangeListener implements ChangeListener<Boolean> {

        private CfgFile file;

        ConfigurationChangeListener(CfgFile file) {
            this.file = file;
        }

        @Override
        public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
            if (null != oldValue && null != newValue) {
                boolean modified = (newValue & !oldValue)
                        | !((oldValue & !newValue));
                setTaskModified(file, modified);
            }
        }

        public void setFile(CfgFile file) {
            this.file = file;
        }
    }

    class TaskUI {

        final private String className;
        final private String fxml;
        final private String icon;
        final private RepoStatus status;

        TaskUI(Class<? extends Configuration> clazz, String fxml, String icon, RepoStatus status) {
            this.className = clazz.getCanonicalName();
            this.fxml = fxml;
            this.icon = icon;
            this.status = status;
        }

        /**
         * @return the className
         */
        String getClassName() {
            return className;
        }

        /**
         * @return the fxml
         */
        String getFxml() {
            return fxml;
        }

        /**
         * @return the icon
         */
        String getIcon() {
            return icon;
        }

        /**
         * @return the status
         */
        RepoStatus getStatus() {
            return status;
        }
    }
}
