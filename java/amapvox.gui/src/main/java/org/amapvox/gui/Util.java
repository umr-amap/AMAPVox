/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.amapvox.gui;

import org.amapvox.commons.javafx.SelectableMenuButton;
import org.amapvox.commons.util.ColorGradient;
import org.amapvox.commons.util.io.file.FileManager;
import org.amapvox.voxelfile.VoxelFileReader;
import java.awt.Color;
import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Predicate;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.binding.Bindings;
import javafx.collections.ListChangeListener;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.apache.log4j.Logger;

/**
 *
 * @author Julien Heurtebize
 */
public class Util {

    final static Logger LOGGER = Logger.getLogger(Util.class);

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
            LOGGER.error("Cannot retrieve avaialble color gradients", ex);
        }
    }

    public final static FileChooser FILE_CHOOSER_TLS = new FileChooser();

    static {
        FILE_CHOOSER_TLS.setTitle("Open TLS file");
        FILE_CHOOSER_TLS.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("All Files", "*.*"),
                new FileChooser.ExtensionFilter("Text Files (*.txt)", "*.txt"),
                new FileChooser.ExtensionFilter("Rxp Files (*.rxp)", "*.rxp"),
                new FileChooser.ExtensionFilter("Project Rsp Files", "*.rsp"));
    }

    public final static FileChooser FILE_CHOOSER_VOXELFILE = new FileChooser();

    static {
        FILE_CHOOSER_TLS.setTitle("Open voxel file");
        FILE_CHOOSER_TLS.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("All Files", "*.*"),
                new FileChooser.ExtensionFilter("Voxel Files  (*.vox)", "*.vox"));
    }

    public static boolean checkIfVoxelFile(File voxelFile) {

        boolean valid = true;

        if (voxelFile != null) {
            String header = FileManager.readHeader(voxelFile.getAbsolutePath());

            if (header != null && header.equals("VOXEL SPACE")) {

            } else {
                valid = false;
            }
        } else {
            valid = false;
        }

        return valid;
    }

    public static String humanReadableByteCount(long bytes, boolean si) {
        int unit = si ? 1000 : 1024;
        if (bytes < unit) {
            return bytes + " B";
        }
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp - 1) + (si ? "" : "i");
        return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
    }

    public static <T> void linkSelectorToList(SelectableMenuButton selector, ListView<T> list) {

        selector.setOnActionAll(event -> list.getSelectionModel().selectAll());
        selector.setOnActionNone(event -> list.getSelectionModel().clearSelection());
        selector.disableProperty().bind(Bindings.isEmpty(list.getItems()));
        list.getSelectionModel().getSelectedIndices().addListener((ListChangeListener.Change<? extends Integer> c) -> {
            if (c.getList().isEmpty()) {
                selector.setIndeterminate(false);
                selector.setSelected(false);
            } else if (c.getList().size() < list.getItems().size()) {
                selector.setIndeterminate(true);
            } else {
                selector.setIndeterminate(false);
                selector.setSelected(true);
            }
        });
    }

    public static void setDragGestureEvents(final TextField textField, Predicate<File> predicate, Consumer<File> action) {

        textField.setOnDragOver(DragAndDropHelper.dragOverEvent);

        textField.setOnDragDropped((DragEvent event) -> {
            Dragboard db = event.getDragboard();
            boolean success = false;
            if (db.hasFiles() && db.getFiles().size() == 1) {
                success = true;
                Predicate<File> nonNull = Objects::nonNull;
                Consumer<File> setText = file -> textField.setText(file.getAbsolutePath());
                db.getFiles().stream()
                        .filter(nonNull.and(predicate))
                        .forEach(setText.andThen(action));
            }
            event.setDropCompleted(success);
            event.consume();
        });
    }

    public static void setDragGestureEvents(final TextField textField, Consumer<File> action) {
        setDragGestureEvents(textField, acceptAll, action);
    }

    public static void setDragGestureEvents(final TextField textField) {
        setDragGestureEvents(textField, acceptAll, doNothing);
    }

    public static void setDragGestureEvents(final ListView listView, Consumer<File> action) {

        listView.setOnDragOver(DragAndDropHelper.dragOverEvent);

        listView.setOnDragDropped((DragEvent event) -> {
            Dragboard db = event.getDragboard();
            boolean success = false;
            if (db.hasFiles()) {
                success = true;
                db.getFiles().forEach(action);
            }
            event.setDropCompleted(success);
            event.consume();
        });
    }

    public static final Consumer<File> doNothing = file -> {
    };

    public static final Predicate<File> acceptAll = file -> true;

    public static final Predicate<File> isVoxelFile = file -> {
        if (VoxelFileReader.isValid(file)) {
            return true;
        } else {
            LOGGER.warn("Failed to drop " + file.getName() + ". Not a valid vox file.");
            return false;
        }
    };

    public static void showErrorDialog(Stage stage, final Throwable e, String prefix) {

        if (null != prefix) {
            LOGGER.error("["  + prefix + "] Error: ", e);
        } else {
            LOGGER.error("Error: ", e);
        }
        DialogHelper.showErrorDialog(stage, e);
    }

    /**
     * Hack from SO to change Tooltip delay time.
     * https://stackoverflow.com/a/27739605
     * 
     * @param tooltip, the tooltip object.
     * @param millis , delay time in milliseconds.
     */
    public static void hackTooltipStartTiming(Tooltip tooltip, long millis) {
        try {
            Field fieldBehavior = tooltip.getClass().getDeclaredField("BEHAVIOR");
            fieldBehavior.setAccessible(true);
            Object objBehavior = fieldBehavior.get(tooltip);

            Field fieldTimer = objBehavior.getClass().getDeclaredField("activationTimer");
            fieldTimer.setAccessible(true);
            Timeline objTimer = (Timeline) fieldTimer.get(objBehavior);

            objTimer.getKeyFrames().clear();
            objTimer.getKeyFrames().add(new KeyFrame(new Duration(millis)));
        } catch (Exception e) {
        }
    }

}
