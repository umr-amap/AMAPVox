/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.amapvox.gui.task;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.concurrent.Service;

/**
 *
 * @author calcul
 */
public class TaskElementExecutor {
    
    private final Deque<TaskElement> stack;
    private final int nbCores;
    private SimpleIntegerProperty numberOfCurrentTaskRunning;
    
    public TaskElementExecutor(int nbCores, List<TaskElement> taskElements){
        
        this.nbCores = nbCores;
        
        stack = new ArrayDeque<>();
        
        for(TaskElement element : taskElements){
            stack.add(element);
        }
    }
    
    private void executeNextTask(){
        
        if(!stack.isEmpty()){
            
            TaskElement taskElement = stack.pop();
            
            if(taskElement.getButtonType() == TaskElement.ButtonType.CANCEL){ //Task is already running
                numberOfCurrentTaskRunning.set(numberOfCurrentTaskRunning.getValue()-1);
                executeNextTask();
            }else{
                numberOfCurrentTaskRunning.set(numberOfCurrentTaskRunning.getValue()+1);
                
                taskElement.addTaskListener(new TaskAdapter() {
                    @Override
                    public void onSucceeded(Service service) {
                        numberOfCurrentTaskRunning.set(numberOfCurrentTaskRunning.getValue()-1);
                        executeNextTask();
                    }

                    @Override
                    public void onCancelled() {
                        numberOfCurrentTaskRunning.set(numberOfCurrentTaskRunning.getValue()-1);
                        executeNextTask();
                    }

                    @Override
                    public void onFailed(Throwable ex) {
                        numberOfCurrentTaskRunning.set(numberOfCurrentTaskRunning.getValue()-1);
                        executeNextTask();
                    }
                });

                taskElement.startTask();
            }
            
        }
        
    }
    
    public void execute(){
        
        numberOfCurrentTaskRunning = new SimpleIntegerProperty(0);
     
        while(!stack.isEmpty() && (numberOfCurrentTaskRunning.getValue() < nbCores)){
            
            executeNextTask();            
        }
    }
}
