package org.amapvox.gui;

import java.util.Optional;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;

/**
 * @author Tristan Muller <tristan.muller@cirad.fr>
 */
public class ConfirmationWindow {
    
    public static Optional<ButtonType> showConfirmation(
            final String headerText, final String title,
            final String contentText) {
        return showAlert(headerText, title, contentText,
                AlertType.CONFIRMATION);
    }
    
    private static Optional<ButtonType> showAlert(final String headerText,
            final String title, final String contentText,
            final AlertType alertType) {

        ButtonType[] buttons;

        if (alertType.equals(AlertType.CONFIRMATION)) {
            buttons = new ButtonType[2];
            buttons[0] = ButtonType.YES;
            buttons[1] = ButtonType.NO;
        } else {
            buttons = new ButtonType[1];
            buttons[0] = ButtonType.OK;
        }

        Alert alert = new Alert(alertType, contentText, buttons);

        alert.setHeaderText(headerText);
        alert.setTitle(title);

        if (alertType.equals(AlertType.CONFIRMATION))
            return alert.showAndWait();
        else {
            alert.show();
            return Optional.empty();
        }
    }
    
}
