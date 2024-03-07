/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.amapvox.gui;

import java.util.function.UnaryOperator;
import javafx.scene.control.TextFormatter;
import javafx.util.converter.FloatStringConverter;
import javafx.util.converter.IntegerStringConverter;

/**
 *
 * @author pverley
 */
public class TextFieldUtil {

    public static TextFormatter<Float> createRatioTextFormatter(int fractionDigits, float defaultValue, boolean oneAccepted) {

        return new TextFormatter(
                new DefaultFloatStringConverter(defaultValue),
                defaultValue,
                new RatioUnaryOperator(fractionDigits, oneAccepted));
    }

    public static TextFormatter<Float> createFloatTextFormatter(float defaultValue, Sign sign) {

        final String pattern;
        switch (sign) {
            case BOTH:
                pattern = "^[-]?[0-9]*([.][0-9]*)?";
                break;
            case POSITIVE:
                pattern = "[0-9]*([.][0-9]*)?";
                break;
            case NEGATIVE:
                pattern = "(^0)|(^[-][0-9]*([.][0-9]*)?)";
                break;
            default:
                pattern = "^[-]?\\d+\\.\\d+";
        }

        UnaryOperator<TextFormatter.Change> filter = change -> {
            if (!change.isContentChange()
                    || change.getControlNewText().matches(pattern)
                    || change.getControlNewText().isEmpty()) {
                return change;
            }
            return null;
        };

//        https://stackoverflow.com/a/31043122
//        DecimalFormat df = new DecimalFormat();
//        UnaryOperator<TextFormatter.Change> filter = c -> {
//            if (c.getControlNewText().isEmpty()) {
//                return c;
//            }
//
//            ParsePosition parsePosition = new ParsePosition(0);
//            Object object = df.parse(c.getControlNewText(), parsePosition);
//
//            if (object == null || parsePosition.getIndex() < c.getControlNewText().length()) {
//                return null;
//            } else {
//                return c;
//            }
//        };


        return new TextFormatter(
                new DefaultFloatStringConverter(defaultValue),
                defaultValue,
                filter);
    }
    
    public static TextFormatter<Float> createFloatTextFormatter(float defaultValue) {
        return createFloatTextFormatter(defaultValue, Sign.BOTH);
    }

    public static TextFormatter<Integer> createIntegerTextFormatter(int defaultValue, Sign sign) {

        final String pattern;
        switch (sign) {
            case BOTH:
                pattern = "^([-][1-9])?[0-9]*";
                break;
            case POSITIVE:
                pattern = "^[0-9]+";
                break;
            case NEGATIVE:
                pattern = "(^0)|(^[-][1-9][0-9]*)";
                break;
            default:
                pattern = "\\d+";
        }

        UnaryOperator<TextFormatter.Change> filter = change -> {
            if (!change.isContentChange()
                    || change.getControlNewText().matches(pattern)
                    || change.getControlNewText().isEmpty()) {
                return change;
            }
            return null;
        };

        return new TextFormatter(
                new DefaultIntegerStringConverter(defaultValue),
                defaultValue,
                filter);
    }

    private static class DefaultIntegerStringConverter extends IntegerStringConverter {

        private final Integer defaultValue;

        private DefaultIntegerStringConverter(Integer defaultValue) {
            this.defaultValue = defaultValue;
        }

        @Override
        public Integer fromString(String value) {

            if (null == value || value.trim().isEmpty()) {
                return defaultValue;
            }
            return super.fromString(value);
        }
    }

    public static enum Sign {
        BOTH,
        POSITIVE,
        NEGATIVE;
    }

    private static class DefaultFloatStringConverter extends FloatStringConverter {

        private final Float f;

        private DefaultFloatStringConverter(Float f) {
            this.f = f;
        }

        @Override
        public Float fromString(String value) {
            if (null == value || value.trim().isEmpty()) {
                return f;
            } else {
                return super.fromString(value);
            }
        }
    }

    private static class RatioUnaryOperator implements UnaryOperator<TextFormatter.Change> {

        private final int fractionDigits;
        private final boolean oneIncluded;

        private RatioUnaryOperator(int fractionDigits, boolean oneIncluded) {
            this.fractionDigits = fractionDigits;
            this.oneIncluded = oneIncluded;
        }

        @Override
        public TextFormatter.Change apply(TextFormatter.Change change) {

            if (!change.isContentChange()) {
                /**
                 * nothing is added or deleted but change must be returned as it
                 * contains selection info and caret position
                 */
                return change;
            }

            String newText = change.getControlNewText();
            String oldText = change.getControlText();
            String text = change.getText();

            String pattern = oneIncluded
                    ? "^(0([.]([0-9])*)?)|^(1([.][0]?)?)|^$"
                    : "^0([.]([0-9])*)?|^$";

            // user copy/paste a new value
            if (text.length() > 1 && text.matches(pattern)) {
                // limited float digits
                int length = Math.min(text.length(), fractionDigits + 2);
                text = text.substring(0, length);
                change.setText(text);
                change.setRange(0, oldText.length());
                change.setAnchor(length);
                change.setCaretPosition(length);
                return change;
            }

            // allows user to overwrite 1st caracter (zero or one)
            if (change.getRangeStart() == 0) {
                if (change.getText().equals("0")) {
                    // accepts the zero
                    change.setText("0.");
                    change.setRange(0, Math.max(change.getRangeEnd(), Math.min(2, oldText.length())));
                    change.setAnchor(2);
                    change.setCaretPosition(2);
                    return change;
                }
                if (oneIncluded && change.getText().equals("1")) {
                    // forces final value 1.0
                    change.setText("1.0");
                    change.setRange(0, oldText.length());
                    change.setAnchor(3);
                    change.setCaretPosition(3);
                    return change;
                }
            }

            // allow user to overwrite the dot
            if (change.getRangeStart() == 1 && change.getText().equals(".")) {
                change.setRange(1, Math.max(change.getRangeEnd(), Math.min(2, oldText.length())));
                return change;
            }

            if (newText.matches(pattern)) {
                if (newText.length() > (fractionDigits + 2)) {
                    if (change.getRangeStart() >= 2 && change.getRangeStart() < (fractionDigits + 2)) {
                        text += oldText.substring(change.getRangeStart() + 1, fractionDigits + 2);
                        change.setText(text);
                        change.setRange(change.getRangeStart(), fractionDigits + 2);
                        return change;
                    }
                    return null;
                }
                return change;
            }
            return null;
        }
    }

}
