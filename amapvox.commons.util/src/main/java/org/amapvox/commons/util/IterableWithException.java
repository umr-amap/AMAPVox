/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.amapvox.commons.util;

/**
 *
 * @author Julien Heurtebize
 */
public interface IterableWithException <T>{
    
    IteratorWithException<T> iterator() throws Exception;
}
