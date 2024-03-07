/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.amapvox.commons.util;

import java.util.EventListener;

/**
 *
 * @author Julien Heurtebize (julienhtbe@gmail.com)
 */
public interface ProcessingListener extends EventListener{
    
    void processingStepProgress(String progressMsg, long progress, long max);
    void processingFinished(float duration);
}

