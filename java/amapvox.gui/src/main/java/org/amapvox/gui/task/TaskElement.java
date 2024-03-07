/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.amapvox.gui.task;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.prefs.Preferences;
import javafx.beans.property.StringProperty;
import javafx.concurrent.Service;
import javafx.concurrent.Worker.State;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javax.swing.event.EventListenerList;
import org.amapvox.commons.Configuration;
import org.amapvox.gui.CfgFile;

/**
 * Give control and information about a task execution
 *
 * @author Julien Heurtebize
 */
public class TaskElement extends AnchorPane implements Initializable {

    @FXML
    private Label taskIcon;
    @FXML
    private Button expandButton;
    @FXML
    private Label taskTitle;
    @FXML
    private Label taskMessage;
    @FXML
    private Button controlButton;
    @FXML
    private ProgressBar taskProgress;

    private final EventListenerList listeners;

    private ButtonType buttonType;
    private Service service;
    private CfgFile linkedFile;
    private final Preferences prefs;

    private ImageView stopImage;
    private ImageView startImage;
    private final static Image STOP_IMG = new Image(TaskElement.class.getResource("/org/amapvox/gui/fxml/icons/stop.png").toExternalForm());
    private final static Image START_IMG = new Image(TaskElement.class.getResource("/org/amapvox/gui/fxml/icons/start.png").toExternalForm());

    public enum ButtonType {
        START,
        CANCEL;
    }

    public TaskElement(CfgFile linkedFile, Preferences prefs) {

        this.linkedFile = linkedFile;
        this.prefs = prefs;

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/amapvox/gui/fxml/task/TaskElement.fxml"));
            /**
             * The constructor is leaking 'this' but it seems the way to do it
             * according to Oracle documentation.
             * https://docs.oracle.com/javafx/2/fxml_get_started/custom_control.htm
             */
            loader.setRoot(this);
            loader.setController(this);
            loader.load();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }

        listeners = new EventListenerList();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        int btnIconSize = 16;
        stopImage = new ImageView(STOP_IMG);
        stopImage.setFitWidth(btnIconSize);
        stopImage.setFitHeight(btnIconSize);
        startImage = new ImageView(START_IMG);
        startImage.setFitWidth(btnIconSize);
        startImage.setFitHeight(btnIconSize);

        // set default button as a start button
        buttonType = ButtonType.START;
        controlButton.setGraphic(startImage);

        updateTitle(false);

        controlButton.setOnAction(event -> {
            switch (buttonType) {
                case CANCEL -> {
                    setButtonType(TaskElement.ButtonType.START);
                    service.cancel();
                }
                case START -> {
                    fireStarted();
                    setButtonType(TaskElement.ButtonType.CANCEL);
                    reset();
                    service.start();
                }
            }
        });

        expandButton.setOnAction(event -> {
            if ("+".equals(expandButton.getText())) {
                updateTitle(true);
                expandButton.setText("-");
            } else {
                updateTitle(false);
                expandButton.setText("+");
            }
        });
    }
    
    private Service createService() throws Exception {
        Configuration cfg = Configuration.newInstance(linkedFile.getFile());
        return new AVoxService(cfg.getTaskClass(), linkedFile, prefs.getInt("ncpu", 1));
    }
    
    public void updateFile(CfgFile file) {
        this.linkedFile = file;
        updateTitle("-".equals(expandButton.getText()));
    }

    private void updateTitle(boolean expanded) {

        if (expanded) {
            taskTitle.setText(linkedFile.getFile().getAbsolutePath());
        } else {
            String title = linkedFile.getName();
            if (linkedFile.isDeprecated()) {
                title += " (deprecated)";
            }
            taskTitle.setText(title);
        }
    }

    public void setModified(boolean modified) {
        updateTitle("-".equals(expandButton.getText()));
    }

    public void setButtonDisable(boolean value) {
        controlButton.setDisable(value);
    }

    public boolean isTaskDisable() {
        return controlButton.isDisable();
    }

    public boolean isTaskRunning() {
        return (null == service) ? false : service.isRunning();
    }

    private void initService() {

        taskMessage.textProperty().bind(service.messageProperty());

        service.setOnFailed((Event event) -> {
            setButtonType(ButtonType.START);
            taskMessage.textProperty().unbind();
            taskProgress.progressProperty().unbind();
            taskProgress.setProgress(0);
            taskMessage.setTextFill(new Color(1, 0, 0, 1));
            taskMessage.setText(service.getTitle() + " error!");
            fireFailed(service.getException());
        });

        service.setOnCancelled((Event event) -> {
            setButtonType(ButtonType.START);
            taskMessage.textProperty().unbind();
            taskMessage.setText(service.getTitle() + " cancelled!");
            taskProgress.setDisable(true);
            taskProgress.progressProperty().unbind();
            taskProgress.setProgress(0);
            fireCancelled();
        });

        service.setOnSucceeded((Event event) -> {
            setButtonType(ButtonType.START);
            taskProgress.progressProperty().unbind();
            taskProgress.setProgress(100);
            taskMessage.textProperty().unbind();
            taskMessage.setText(service.getTitle() + " done!");
            fireSucceeded(service);
        });
    }

    public void startTask() {
        fireStarted();
        setButtonType(TaskElement.ButtonType.CANCEL);
        reset();
        service.start();
    }

    private void reset() {
        
        try {
            service = createService();
            
            if (State.READY != service.getState()) {
                service.reset();
            }
            initService();
            taskMessage.setTextFill(new Color(0, 0, 0, 1));
            taskMessage.textProperty().bind(service.messageProperty());
            taskProgress.progressProperty().bind(service.progressProperty());
            taskProgress.setDisable(false);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public void setTaskIcon(Image image) {
        ImageView icon = new ImageView(image);
        icon.setFitHeight(24);
        icon.setFitWidth(24);
        taskIcon.setGraphic(icon);
    }

    public void setTaskTitle(String title) {
        taskTitle.setText(title);
    }

    public void setTaskMessage(String taskMessage) {
        this.taskMessage.setText(taskMessage);
    }

    public StringProperty getTaskMessageProperty() {
        return this.taskMessage.textProperty();
    }

    public ProgressBar getTaskProgress() {
        return taskProgress;
    }

    public void setButtonType(ButtonType type) {

        this.buttonType = type;

        switch (type) {
            case CANCEL -> controlButton.setGraphic(stopImage);
            case START -> controlButton.setGraphic(startImage);
        }
    }

    public ButtonType getButtonType() {

        return buttonType;
    }

    public Button getControlButton() {
        return controlButton;
    }

    public CfgFile getLinkedFile() {
        return linkedFile;
    }

    private void fireStarted() {
        for (TaskListener listener : listeners.getListeners(TaskListener.class)) {
            listener.onStarted();
        }
    }

    private void fireFailed(Throwable ex) {
        for (TaskListener listener : listeners.getListeners(TaskListener.class)) {
            listener.onFailed(ex);
        }
    }

    private void fireCancelled() {
        for (TaskListener listener : listeners.getListeners(TaskListener.class)) {
            listener.onCancelled();
        }
    }

    private void fireSucceeded(Service service) {

        for (TaskListener listener : listeners.getListeners(TaskListener.class)) {
            listener.onSucceeded(service);
        }
    }

    public void addTaskListener(TaskListener taskListener) {
        listeners.add(TaskListener.class, taskListener);
    }

    public void removeTaskListener(TaskListener taskListener) {
        listeners.remove(TaskListener.class, taskListener);
    }

}
