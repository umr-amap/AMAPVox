/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.amapvox.commons.util;

/**
 *
 * @author calcul
 */
public class ProcessingAdapter implements ProcessingListener {

    @Override
    public void processingStepProgress(String progressMsg, long progress, long max){}

    @Override
    public void processingFinished(float duration){}
    
}
