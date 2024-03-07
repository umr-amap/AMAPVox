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
public class FinalCounter {

    private int value;

    public FinalCounter(int intialValue) {
        value = intialValue;
    }

    public void increment() {
        value++;
    }

    public void decrement() {
        value--;
    }

    public int getValue() {
        return value;
    }

}
