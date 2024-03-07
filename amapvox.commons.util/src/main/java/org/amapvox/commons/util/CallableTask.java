/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.amapvox.commons.util;

import java.util.concurrent.Callable;
import javax.swing.event.EventListenerList;

/**
 *
 * @author Julien Heurtebize
 */
public abstract class CallableTask<T>  extends Process implements Callable<T> {

    private final EventListenerList listeners;

    public CallableTask() {
        this.listeners = new EventListenerList();
    }
    
    @Override
    public abstract T call() throws Exception;
    
    public void addCallableTaskListener(CallableTaskListener taskListener){
        listeners.add(CallableTaskListener.class, taskListener);
    }
    
    public void removeCallableTaskListener(CallableTaskListener taskListener){
        listeners.remove(CallableTaskListener.class, taskListener);
    }
    
    protected void fireCancelled(){
        for(CallableTaskListener listener : listeners.getListeners(CallableTaskListener.class)){
            listener.onCancelled();
        }
    }
    
    protected void fireSucceeded(){
        
        for(CallableTaskListener listener : listeners.getListeners(CallableTaskListener.class)){
            listener.onSucceeded();
        }
    }
}
