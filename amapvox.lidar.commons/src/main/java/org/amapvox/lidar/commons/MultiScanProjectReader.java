/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.amapvox.lidar.commons;

import org.amapvox.commons.util.IteratorWithException;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author pverley
 */
public class MultiScanProjectReader extends LidarProjectReader {

    private final List<File> scanFiles;
    private final LidarScanReader scanReader;
    private final String fileExt;

    public MultiScanProjectReader(File file, String fileExt, LidarScanReader scanReader) throws IOException {
        super(file);
        this.fileExt = fileExt.trim().toUpperCase();
        this.scanReader = scanReader;
        scanFiles = new ArrayList();
    }

    private void readHeader() throws FileNotFoundException, IOException {

        try (BufferedReader projectReader = new BufferedReader(new FileReader(getFile()))) {
            String header = fileExt.trim().toUpperCase() + " index file";
            // check 1st line
            if (!projectReader.readLine().trim().toLowerCase().startsWith(header.toLowerCase())) {
                throw new IOException("not a valide multi scan project file (1st line must be \"" + header + "\"");
            }
            // check 2nd line
            String line;
            line = projectReader.readLine().trim();
            if (!line.matches("^-+")) {
                throw new IOException("not a valide multi scan project file (2nd line must contain only minus chars ------ no matter how many)");
            }
        }

    }

    private void readProjectFile() throws FileNotFoundException, IOException {

        // check header
        readHeader();

        try (BufferedReader projectReader = new BufferedReader(new FileReader(getFile()))) {
            // skip line 1 & 2
            projectReader.readLine();
            projectReader.readLine();
            // read file list
            scanFiles.clear();
            String line;
            while ((line = projectReader.readLine()) != null) {
                line = line.trim();
                if (!(line.startsWith("#") || line.length() < 1)) {
                    line = line.replace('\\', File.separatorChar);
                    scanFiles.add(new File(resolve(line, getFile().getParent())));
                }
            }
        }
    }

    public static boolean isValid(File file, String fileExt) {

        try {
            new MultiScanProjectReader(file, fileExt, null).readHeader();
        } catch (IOException ex) {
            return false;
        }
        return true;
    }

    @Override
    public IteratorWithException<LidarScan> iterator() throws Exception {

        readProjectFile();

        Iterator<File> it = scanFiles.iterator();
        return new IteratorWithException() {

            @Override
            public boolean hasNext() {
                return it.hasNext();
            }

            @Override
            public LidarScan next() throws Exception {
                return scanReader.toLidarScan(it.next());
            }
        };
    }

    /**
     * Resolves a file path against the the provided path. If filename is a
     * directory the function ensures the path ends with a separator.
     *
     * @param filename, the file path to resolve
     * @param relativeTo, the path against the file must be resolved
     * @return the resolved file path
     */
    private String resolve(String filename, String relativeTo) {
        String pathname = filename;
        try {
            File file = new File(relativeTo);
            pathname = new File(file.toURI().resolve(filename)).getCanonicalPath();
        } catch (Exception ex) {
            // do nothing, just return the argument
        }
        if (new File(pathname).isDirectory() && !pathname.endsWith(File.separator)) {
            pathname += File.separator;
        }
        return pathname;
    }
}
