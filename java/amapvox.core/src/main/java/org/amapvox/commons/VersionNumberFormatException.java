/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.amapvox.commons;

/**
 *
 * @author pverley
 */
/**
 * Thrown to indicate that the application has attempted to convert a string to
 * version number {major}.{minor}.{patch}, but that the string does not have the
 * appropriate format. Copy of NumberFormatException.java
 */
public class VersionNumberFormatException extends IllegalArgumentException {

    /**
     * Constructs a <code>VersionNumberFormatException</code> with no detail
     * message.
     */
    public VersionNumberFormatException() {
        super();
    }

    /**
     * Constructs a <code>VersionNumberFormatException</code> with the specified
     * detail message.
     *
     * @param s the detail message.
     */
    public VersionNumberFormatException(String s) {
        super(s);
    }

    /**
     * Factory method for making a <code>VersionNumberFormatException</code>
     * given the specified input which caused the error.
     *
     * @param s the input causing the error
     */
    static VersionNumberFormatException forInputString(String s) {
        return new VersionNumberFormatException("For input string: \"" + s + "\" (expected {major}(.{minor}(.{patch})))");
    }
}
