/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.amapvox.lidar.las;

/**
 *
 * @author pverley
 */
public enum Classification {

    CREATED_NEVER_CLASSIFIED(
            (short) 0, "Created, never classified"),
    UNCLASSIFIED(
            (short) 1, "Unclassified"),
    GROUND(
            (short) 2, "Ground"),
    LOW_VEGETATION(
            (short) 3, "Low vegetation"),
    MEDIUM_VEGETATION(
            (short) 4, "Medium vegetation"),
    HIGH_VEGETATION(
            (short) 5, "High vegetation"),
    BUILDING(
            (short) 6, "Building"),
    LOW_POINT(
            (short) 7, "Low point (noise)"),
    MODEL_KEY_POINT(
            (short) 8, "Model key-point (mass point)"),
    WATER(
            (short) 9, "Water"),
    RESERVED_10(
            (short) 10, "Reserved for ASPRS Definition"),
    RESERVED_11(
            (short) 11, "Reserved for ASPRS Definition"),
    OVERLAP_POINTS(
            (short) 12, "Overlap Points");

    private final short value;
    private final String description;

    private Classification(short value, String description) {
        this.value = value;
        this.description = description;
    }

    public short getValue() {
        return value;
    }

    public String getDescription() {
        return description;
    }
}
