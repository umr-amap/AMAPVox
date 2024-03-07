/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.amapvox.lidar.commons;

import org.amapvox.commons.util.Cancellable;
import org.amapvox.commons.util.IterableWithException;
import org.amapvox.commons.util.Process;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

/**
 *
 * @author pverley
 */
abstract public class LidarProjectReader extends Process implements Cancellable, IterableWithException<LidarScan> {

    private final File file;
    private boolean cancelled = false;

    public LidarProjectReader(File file) throws FileNotFoundException, IOException {
        this.file = file;
        new FileReader(file).read();
    }

    public File getFile() {
        return file;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

}
