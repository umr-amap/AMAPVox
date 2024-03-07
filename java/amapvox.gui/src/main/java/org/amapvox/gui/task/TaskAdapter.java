/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.amapvox.gui.task;

import javafx.concurrent.Service;

/**
 *
 * @author calcul
 */
public abstract class TaskAdapter implements TaskListener{
    
    @Override
    public void onStarted(){}
    
    @Override
    public void onSucceeded(Service service){}
    
    @Override
    public void onCancelled(){}
    
    @Override
    public void onFailed(Throwable ex){}
    
}