package org.amapvox.gui.task;

import java.io.File;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import org.amapvox.commons.AVoxTask;
import org.amapvox.commons.util.ProcessingAdapter;
import org.amapvox.gui.CfgFile;

/**
 * JavaFX Service associated to AVoxTask
 * 
 * @author Philippe VERLEY (philippe.verley@ird.fr)
 */
public class AVoxService extends Service<File[]> {

    private AVoxTask task;
    private final Class className;
    private final CfgFile file;
    private final int ncpu;

    public AVoxService(Class className, CfgFile file, int ncpu) {
        this.className = className;
        this.file = file;
        this.ncpu = ncpu;
    }

    @Override
    protected Task<File[]> createTask() {
        return new Task() {
            @Override
            protected File[] call() throws Exception {

                task = (AVoxTask) className.getConstructor(File.class, int.class).newInstance(file.getFile(), ncpu);
                task.addProcessingListener(new ProcessingAdapter() {
                    @Override
                    public void processingStepProgress(String progressMsg, long progress, long max) {
                        updateMessage(progressMsg);
                        updateProgress(progress, max);
                    }
                });
                task.init();
                updateMessage(task.getName() + " started...");
                updateTitle(task.getName());
                return task.call();
            }

            @Override
            protected void succeeded() {
                super.succeeded();
                updateMessage(task.getName() + " succeeded");
            }

            @Override
            protected void cancelled() {
                super.cancelled();
                task.setCancelled(true);
                updateMessage(task.getName() + " cancelled");
            }
        };
    }
}
