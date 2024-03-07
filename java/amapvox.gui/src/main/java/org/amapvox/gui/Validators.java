/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.amapvox.gui;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import javafx.collections.ObservableList;
import javafx.scene.control.Control;
import javax.vecmath.Point3d;
import org.controlsfx.validation.ValidationResult;
import org.controlsfx.validation.Validator;

/**
 *
 * @author calcul
 */
public class Validators {

    /**
     * Determines if the field is a decimal
     */
    public static Validator<String> fieldDoubleValidator = (Control t, String s) -> {
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
    };

    public static Validator<String> fieldNonZeroDecimalValidator = (Control t, String s) -> {
        if (s.isEmpty()) {
            return ValidationResult.fromErrorIf(t, "Field cannot be empty", s.isEmpty());
        } else {
            boolean valid = false;
            try {
                double d = Double.parseDouble(s);
                valid = (d != 0.d);
            } catch (NumberFormatException ex) {
            }
            
            return ValidationResult.fromErrorIf(t, "Non-zero decimal value expected", !valid);
        }
    };

    /**
     * Determines if the field is an integer
     */
    public static Validator<String> fieldIntegerValidator = (Control t, String s) -> {
        if (s.isEmpty()) {
            return ValidationResult.fromErrorIf(t, "Field cannot be empty", s.isEmpty());
        } else {
            boolean valid = false;
            try {
                Integer.valueOf(s);
                valid = true;
            } catch (NumberFormatException ex) {
            }
            
            return ValidationResult.fromErrorIf(t, "Integer value expected", !valid);
        }
    };

    /**
     * determines if the file exists
     *
     * @param name, the name of the control
     * @return a file exist Validator
     */
    public static Validator<String> fileExistValidator(String name) {
        return (Control t, String s) -> {
            if (s.isEmpty()) {
                return ValidationResult.fromErrorIf(t, name + " cannot be empty", s.isEmpty());
            } else {
                return ValidationResult.fromErrorIf(t, name + " does not exist", !Files.exists(new File(s).toPath()));
            }
        };
    }

    /**
     * determines if the specified file can be written
     *
     * @param name
     * @return
     */
    public static Validator<String> fileValidityValidator(String name) {
        return (Control t, String s) -> {
            if (s.isEmpty()) {
                return ValidationResult.fromErrorIf(t, name + " cannot be empty", s.isEmpty());
            } else {
                Path path = Paths.get(s);
                return ValidationResult
                        .fromErrorIf(t, name + " parent folder does not exist", !Files.exists(path.getParent()))
                        .addErrorIf(t, name + " is a directory", Files.isDirectory(path));
            }
        };
    }

    /**
     * determines if the directory exists
     *
     * @param name, name of the control
     * @return
     */
    public static Validator<String> directoryValidator(String name) {
        return (Control t, String s) -> {
            if (s.isEmpty()) {
                return ValidationResult.fromErrorIf(t, name + " cannot be empty", s.isEmpty());
            } else {
                Path path = Paths.get(s);
                return ValidationResult
                        .fromErrorIf(t, name + " does not exist", !Files.exists(path))
                        .addErrorIf(t, name + " is not a directory", !Files.isDirectory(path));
            }
        };
    }

    /**
     * determines if the list is empty
     */
    public static Validator emptyListValidator = new Validator<ObservableList<Point3d>>() {
        @Override
        public ValidationResult apply(Control t, ObservableList<Point3d> u) {
            return ValidationResult.fromErrorIf(t, "The list is empty", u.isEmpty());
        }
    };

    /**
     * determines if a table view is empty
     */
    public static Validator emptyTableValidator = new Validator<ObservableList>() {
        @Override
        public ValidationResult apply(Control t, ObservableList u) {
            return ValidationResult.fromErrorIf(t, "The list is empty", u.isEmpty());
        }
    };

    /**
     * unregister validator (patch because unregister doesn't exist yet)
     */
    public static Validator<Object> unregisterValidator = (Control t, Object u) -> ValidationResult.fromErrorIf(t, "", false);
}
