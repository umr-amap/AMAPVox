/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.amapvox.gui.logging;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javafx.application.Platform;
import javafx.scene.control.TextArea;
import org.apache.log4j.WriterAppender;
import org.apache.log4j.spi.LoggingEvent;

/**
 *
 * @author pverley
 */
public class TextAreaAppender extends WriterAppender {

    private static final ConcurrentLinkedQueue<String> MESSAGE_QUEUE = new ConcurrentLinkedQueue();

    /**
     * Set the target TextArea for the logging information to appear.
     *
     * @param textArea
     */
    public static void setTextArea(final TextArea textArea) {

        if (null == textArea) {
            throw new NullPointerException("logger TextArea cannot be null");
        }

        ScheduledExecutorService exec = Executors.newSingleThreadScheduledExecutor();
        exec.scheduleAtFixedRate(() -> {

            StringBuilder sb = new StringBuilder();
            while (MESSAGE_QUEUE.peek() != null) {
                sb.append(MESSAGE_QUEUE.poll());
            }
            String message = sb.toString();
            if (!message.isBlank()) {
                // do stuff
                Platform.runLater(() -> {
                    textArea.insertText(textArea.getText().length(), message);
                });
            }
        }, 0L, 250, TimeUnit.MILLISECONDS);
    }

    /**
     * Format and then append the loggingEvent to the stored TextArea.
     *
     * @param loggingEvent
     */
    @Override
    public void append(final LoggingEvent loggingEvent) {
        MESSAGE_QUEUE.offer(layout.format(loggingEvent));
    }
}
