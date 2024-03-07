/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.amapvox.commons.util;

import javax.swing.event.EventListenerList;

/**
 *
 * @author Julien Heurtebize (julienhtbe@gmail.com)
 */
public abstract class Process{
    
    private final EventListenerList listeners= new EventListenerList();
    
    public void fireProgress(String progressMsg, long progress, long max){
        
        for(ProcessingListener listener :listeners.getListeners(ProcessingListener.class)){

            listener.processingStepProgress(progressMsg, progress, max);
        }
    }
    
    public void fireFinished(float duration){
        
        for(ProcessingListener listener :listeners.getListeners(ProcessingListener.class)){
            
            listener.processingFinished(duration);
        }
    }
    
    public void addProcessingListener(ProcessingListener listener){
        listeners.add(ProcessingListener.class, listener);
    }
    
    public void removeProcessingListener(ProcessingListener listener){
        listeners.remove(ProcessingListener.class, listener);
    }
}
