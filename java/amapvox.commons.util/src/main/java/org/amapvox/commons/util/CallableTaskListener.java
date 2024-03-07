/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.amapvox.commons.util;

import java.util.EventListener;

/**
 *
 * @author calcul
 */
public interface CallableTaskListener extends EventListener{
    
    public void onSucceeded();
    public void onCancelled();
}
