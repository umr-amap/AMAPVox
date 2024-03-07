/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.amapvox.commons.javafx.io;

import java.io.File;
import java.util.List;
import javafx.stage.FileChooser;
import javafx.stage.Window;

/**
 * The filechooser methods cannot be override, this class provides a workaround
 * @author Julien Heurtebize
 */
public class FileChooserContext {
    
    private static File LAST_OPEN_DIRECTORY = null;
    private static File LAST_SAVE_DIRECTORY = null;
    
    private static File DEFAULT_DIRECTORY = null;
    public FileChooser fc;
    public File lastSelectedFile;
    private String defaultFileName;
    
    public FileChooserContext(){
        fc = new FileChooser();
    }
    
    public FileChooserContext(String defaultFileName){
        fc = new FileChooser();
        this.defaultFileName = defaultFileName;
    }
    
    public List<File> showOpenMultipleDialog(Window ownerWindow){
        
        if(lastSelectedFile != null){
            fc.setInitialDirectory(lastSelectedFile.getParentFile());
        }else if(DEFAULT_DIRECTORY != null){
            fc.setInitialDirectory(DEFAULT_DIRECTORY);
        }else if(LAST_OPEN_DIRECTORY != null){
            fc.setInitialDirectory(LAST_OPEN_DIRECTORY);
        }
        
        List<File> resultFile = fc.showOpenMultipleDialog(ownerWindow);
        if(resultFile != null && resultFile.size() > 0){
            lastSelectedFile = resultFile.get(0);
            LAST_OPEN_DIRECTORY = new File(lastSelectedFile.getParentFile().getAbsolutePath());
        }
        
        return resultFile;
    }
    
    public File showOpenDialog(Window ownerWindow){
        
        if(lastSelectedFile != null){
            fc.setInitialDirectory(lastSelectedFile.getParentFile());
        }else if(DEFAULT_DIRECTORY != null){
            fc.setInitialDirectory(DEFAULT_DIRECTORY);
        }else if(LAST_OPEN_DIRECTORY != null){
            fc.setInitialDirectory(LAST_OPEN_DIRECTORY);
        }
        
        File resultFile = fc.showOpenDialog(ownerWindow);
        if(resultFile != null){
            lastSelectedFile = resultFile;
            LAST_OPEN_DIRECTORY = new File(lastSelectedFile.getParentFile().getAbsolutePath());
        }
        
        return resultFile;
    }
    
    public File showSaveDialog(Window ownerWindow){
        
        if(lastSelectedFile != null){
            fc.setInitialDirectory(lastSelectedFile.getParentFile());
            fc.setInitialFileName(lastSelectedFile.getName());
        }else{
            if(defaultFileName != null){
                fc.setInitialFileName(defaultFileName);
            }
            if(LAST_SAVE_DIRECTORY != null){
                fc.setInitialDirectory(LAST_SAVE_DIRECTORY);
            }
        }
        
        File resultFile = fc.showSaveDialog(ownerWindow);
        if(resultFile != null){
            lastSelectedFile = resultFile;
            LAST_SAVE_DIRECTORY = new File(lastSelectedFile.getParentFile().getAbsolutePath());
        }
        
        return resultFile;
    }
    
    /**
     * 
     * @param ownerWindow
     * @param requiredExt Required file extension (example : .ext)
     * @return 
     */
    public File showSaveDialog(Window ownerWindow, String requiredExt){
        
        if(lastSelectedFile != null){
            fc.setInitialDirectory(lastSelectedFile.getParentFile());
            fc.setInitialFileName(lastSelectedFile.getName());
        }else{
            if(defaultFileName != null){
                fc.setInitialFileName(defaultFileName);
            }else{
                fc.setInitialFileName("*"+requiredExt);
            }
            
            if(DEFAULT_DIRECTORY != null){
                fc.setInitialDirectory(DEFAULT_DIRECTORY);
            }else if(LAST_SAVE_DIRECTORY != null){
                fc.setInitialDirectory(LAST_SAVE_DIRECTORY);
            }
        }
        
        File resultFile;
        
        while(true){
            resultFile = fc.showSaveDialog(ownerWindow);
            
            if(resultFile == null){
                return null;
            }else if(resultFile.getName().endsWith(requiredExt)){
               break; 
            }else{
                fc.setInitialFileName(resultFile.getName()+requiredExt);
            }
        }
        /*do{
            resultFile = fc.showSaveDialog(ownerWindow);
            
            if(resultFile == null){
                return null;
            }
        }while(!resultFile.getName().endsWith(requiredExt));*/
        
        lastSelectedFile = resultFile;
        LAST_SAVE_DIRECTORY = new File(lastSelectedFile.getParentFile().getAbsolutePath());
        
        return resultFile;
    }

    public static void setDefaultDirectory(File defaultDirectory) {
        DEFAULT_DIRECTORY = defaultDirectory;
    }
    
    /**
     * Force the last selected directory for saving.
     * @param directory a directory
     */
    public static void forceLastSaveDirectory(File directory){
        
        if(directory != null && directory.isDirectory()){
            LAST_SAVE_DIRECTORY = new File(directory.getAbsolutePath());
        }else{
            throw new IllegalArgumentException("Not a directory !");
        }
    }
    
    /**
     * Force the last selected directory for opening.
     * @param directory a directory
     */
    public static void forceLastOpenDirectory(File directory){
        
        if(directory != null && directory.isDirectory()){
            LAST_OPEN_DIRECTORY = new File(directory.getAbsolutePath());
        }else{
            throw new IllegalArgumentException("Not a directory !");
        }
    }

    public static File getLastOpenDirectory() {
        return LAST_OPEN_DIRECTORY;
    }
    
    public static File getLastSaveDirectory() {
        return LAST_SAVE_DIRECTORY;
    }
}
