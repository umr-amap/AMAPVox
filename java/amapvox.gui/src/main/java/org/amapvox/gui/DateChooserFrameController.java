package org.amapvox.gui;

/*
This software is distributed WITHOUT ANY WARRANTY and without even the
implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.

This program is open-source LGPL 3 (see copying.txt).
Authors:
    Gregoire Vincent    gregoire.vincent@ird.fr
    Julien Heurtebize   julienhtbe@gmail.com
    Jean Dauzat         jean.dauzat@cirad.fr
    Rémi Cresson        cresson.r@gmail.com

For further information, please contact Gregoire Vincent.
 */



import java.io.IOException;
import org.amapvox.canopy.transmittance.SimulationPeriod;
import org.amapvox.canopy.transmittance.Period;
import java.net.URL;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
import java.util.ResourceBundle;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Control;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.controlsfx.validation.ValidationResult;
import org.controlsfx.validation.ValidationSupport;
import org.controlsfx.validation.Validator;

/**
 * FXML Controller class
 *
 * @author calcul
 */


public class DateChooserFrameController implements Initializable {
    
    private boolean confirmed;
    private Stage stage;
    private Parent root;
    private int currentStartHour;
    private int currentStartMinute;
    private int currentEndHour;
    private int currentEndMinute;
    
    private Validator<LocalDate> startDateValidator;
    private Validator<LocalDate> endDateValidator;
    private Validator<String> numberValidator;
    private ValidationSupport validationSupport;
    
    private NumberFormat numberFormat;
    
    @FXML
    private DatePicker datepickerStartDate;
    @FXML
    private DatePicker datepickerEndDate;
    @FXML
    private TextField textfieldClearnessCoefficient;
    @FXML
    private TextField textfieldEndHour;
    @FXML
    private TextField textfieldEndMinute;
    @FXML
    private TextField textfieldStartHour;
    @FXML
    private TextField textfieldStartMinute;
    @FXML
    private Button buttonAccept;
    
    public static DateChooserFrameController newInstance() {

        DateChooserFrameController controller = null;

        try {

            FXMLLoader loader = new FXMLLoader(DateChooserFrameController.class.getResource("/org/amapvox/gui/fxml/DateChooserFrame.fxml"));
            Parent root = loader.load();
            controller = loader.getController();
            controller.root = root;

        } catch (IOException ex) {
            Logger.getLogger(DateChooserFrameController.class.getName()).log(Level.SEVERE, "Cannot load DateChooserFrame.fxml", ex);
        }

        return controller;
    }


    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        
        confirmed = false;
        
        currentStartHour = Integer.valueOf(textfieldStartHour.getText());
        currentStartMinute = Integer.valueOf(textfieldStartMinute.getText());
        currentEndHour = Integer.valueOf(textfieldEndHour.getText());
        currentEndMinute = Integer.valueOf(textfieldEndMinute.getText());
        
        numberFormat = NumberFormat.getInstance();
        numberFormat.setMinimumIntegerDigits(2);
        
        datepickerStartDate.valueProperty().addListener(new ChangeListener<LocalDate>() {
            @Override
            public void changed(ObservableValue<? extends LocalDate> observable, LocalDate oldValue, LocalDate newValue) {
                if(datepickerEndDate.getValue() == null){
                    datepickerEndDate.setValue(newValue);
                }
            }
        });
        
        startDateValidator = new Validator<LocalDate>() {
            @Override
            public ValidationResult apply(Control t, LocalDate s) {
                
                if(datepickerEndDate.getValue() != null && s != null){
                    return ValidationResult.fromErrorIf(t, "The start period should be before the end period",
                            (datepickerEndDate.getValue().compareTo(s) < 0));
                }
                
                return ValidationResult.fromErrorIf(t, "A value is required", s == null);
            }
        };
        
        endDateValidator = new Validator<LocalDate>() {
            @Override
            public ValidationResult apply(Control t, LocalDate u) {
                
                if(datepickerStartDate.getValue() != null && u != null){
                    
                    int compare = datepickerStartDate.getValue().compareTo(u);
                    return ValidationResult.fromErrorIf(t, "The end period should be after the start period",compare > 0);
                }
                
                return ValidationResult.fromErrorIf(t, "A value is required", u == null);
            }
        };
        
        numberValidator = new Validator<String>() {
            @Override
            public ValidationResult apply(Control t, String s) {
                
                if(s == null || s.isEmpty()){
                    return ValidationResult.fromErrorIf(t, "A value is required", true);
                }else{
                    boolean valid;
                    try{
                        Float value = Float.valueOf(s);
                        valid = value >= 0;
                    }catch(Exception e){
                        valid = false;
                    }
                
                    return ValidationResult.fromErrorIf(t, "A number is required", !valid);
                }
            }
        };
        
        validationSupport = new ValidationSupport();
        
        validationSupport.registerValidator(datepickerStartDate, startDateValidator);
        validationSupport.registerValidator(datepickerEndDate, endDateValidator);
        validationSupport.registerValidator(textfieldClearnessCoefficient, numberValidator);
        
        validationSupport.validationResultProperty().addListener(new ChangeListener<ValidationResult>() {
            @Override
            public void changed(ObservableValue<? extends ValidationResult> observable, ValidationResult oldValue, ValidationResult newValue) {
                if(newValue.getErrors().isEmpty() && newValue.getMessages().isEmpty() && newValue.getWarnings().isEmpty()){
                    buttonAccept.setDisable(false);
                }else{
                    buttonAccept.setDisable(true);
                }
            }
        });
    }    

    @FXML
    private void onActionButtonAcceptDate(ActionEvent event) {
        confirmed = true;
        stage.close();
    }

    
    public void reset(){
        confirmed = false;
    }

    public boolean isConfirmed() {
        return confirmed;
    }
    
    public SimulationPeriod getDateRange(){
        
        try{
            LocalDate startDate = datepickerStartDate.getValue();
            LocalDate endDate = datepickerEndDate.getValue();

            Period period = new Period();
            Date date1 = Date.from(startDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
            Date date2 = Date.from(endDate.atStartOfDay(ZoneId.systemDefault()).toInstant());

            Calendar startTime = Calendar.getInstance(TimeZone.getDefault());

            //start time
            startTime.setTime(date1);
            startTime.set(Calendar.HOUR, currentStartHour);
            startTime.set(Calendar.MINUTE, currentStartMinute);
            period.startDate = startTime;

            //end time
            Calendar endTime = Calendar.getInstance(TimeZone.getDefault());
            endTime.setTime(date2);
            endTime.set(Calendar.HOUR, currentEndHour);
            endTime.set(Calendar.MINUTE, currentEndMinute);
            period.endDate = endTime;

            return new SimulationPeriod(period, Float.valueOf(textfieldClearnessCoefficient.getText()));
            
        }catch(Exception e){
            return null;
        }
        
    }
    
    public Stage getStage() {
         if (null == stage) {
            stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Calendar");
            stage.initModality(Modality.APPLICATION_MODAL);
        }
        return stage;
    }

    @FXML
    private void onActionButtonIncreaseEndHour(ActionEvent event) {
        
        if(currentEndHour < 23){
            currentEndHour++;
        }else{
            currentEndHour = 0;
        }
        
        textfieldEndHour.setText(numberFormat.format(currentEndHour));
    }

    @FXML
    private void onActionButtonDecreaseEndHour(ActionEvent event) {
        
        if(currentEndHour > 0){
            currentEndHour--;
        }else{
            currentEndHour = 23;
        }
        
        textfieldEndHour.setText(numberFormat.format(currentEndHour));
    }

    @FXML
    private void onActionButtonIncreaseEndMinute(ActionEvent event) {
        
        if(currentEndMinute < 59){
            currentEndMinute++;
        }else{
            currentEndMinute = 0;
        }
        
        textfieldEndMinute.setText(numberFormat.format(currentEndMinute));
    }

    @FXML
    private void onActionButtonDecreaseEndMinute(ActionEvent event) {
        
        if(currentEndMinute > 0){
            currentEndMinute--;
        }else{
            currentEndMinute = 59;
        }
        
        textfieldEndMinute.setText(numberFormat.format(currentEndMinute));
    }

    @FXML
    private void onActionButtonIncreaseStartHour(ActionEvent event) {
        
        if(currentStartHour < 23){
            currentStartHour++;
        }else{
            currentStartHour = 0;
        }
        
        textfieldStartHour.setText(numberFormat.format(currentStartHour));
    }

    @FXML
    private void onActionButtonDecreaseStartHour(ActionEvent event) {
        
        if(currentStartHour > 0){
            currentStartHour--;
        }else{
            currentStartHour = 23;
        }
        
        textfieldStartHour.setText(numberFormat.format(currentStartHour));
    }

    @FXML
    private void onActionButtonIncreaseStartMinute(ActionEvent event) {
        
        if(currentStartMinute < 59){
            currentStartMinute++;
        }else{
            currentStartMinute = 0;
        }
        
        textfieldStartMinute.setText(numberFormat.format(currentStartMinute));
    }

    @FXML
    private void onActionButtonDecreaseStartMinute(ActionEvent event) {
        
        if(currentStartMinute > 0){
            currentStartMinute--;
        }else{
            currentStartMinute = 59;
        }
        
        textfieldStartMinute.setText(numberFormat.format(currentStartMinute));
    }

    
    
}
