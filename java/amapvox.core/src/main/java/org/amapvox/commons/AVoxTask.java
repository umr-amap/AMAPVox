/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.amapvox.commons;

import org.amapvox.commons.util.CallableTask;
import org.amapvox.commons.util.Cancellable;
import org.amapvox.commons.util.ProcessingListener;
import org.amapvox.commons.Configuration;
import java.io.File;

/**
 * AMAPVox task based on CallableTask and Cancellable. Returns an array of
 * files.
 *
 * @author Philippe Verley (philippe.verley@ird.fr)
 */
abstract public class AVoxTask extends CallableTask<File[]> implements Cancellable, ProcessingListener {

    private final File file;
    private final int ncpu;
    private Configuration cfg;
    private boolean cancelled;

    /**
     * Creates a new task with given configuration file and number of threads
     * allocated to the task.
     *
     * @param file, the XML configuration file.
     * @param ncpu, the number of threads allocated to the task.
     */
    public AVoxTask(File file, int ncpu) {
        this.file = file;
        this.ncpu = ncpu;
        cancelled = false;
    }

    /**
     * Return the Configuration class.
     * 
     * @return the configuration class.
     */
    protected abstract Class<? extends Configuration> getConfigurationClass();

    /**
     * Initialises the task. Must be light-weighted step.
     *
     * @throws Exception
     */
    protected abstract void doInit() throws Exception;

    /**
     * Return the name of the task.
     *
     * @return the name of the task.
     */
    abstract public String getName();

    final public void init() throws Exception {
        
        // instantiate configuration
        cfg = (Configuration) getConfigurationClass().getConstructor().newInstance();
        cfg.read(file);
        // specific initialization
        doInit();
        setCancelled(false);
    }
    
    public Configuration getConfiguration() {
        return cfg;
    }

    /**
     * Return the XML configuration file associated to the task.
     *
     * @return the configuration file.
     */
    public File getFile() {
        return file;
    }

    public int getNCPU() {
        return ncpu;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    @Override
    public void processingStepProgress(String progressMsg, long progress, long max) {
        fireProgress(progressMsg, progress, max);
    }

    @Override
    public void processingFinished(float duration) {
        fireFinished(duration);
    }
}
