/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.amapvox.gui.task;

import java.util.EventListener;
import javafx.concurrent.Service;

/**
 *
 * @author calcul
 */
public interface TaskListener extends EventListener{
    
    public void onStarted();
    public void onSucceeded(Service service);
    public void onCancelled();
    public void onFailed(Throwable ex);
}
