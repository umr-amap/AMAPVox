/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.amapvox.voxelisation;

import org.amapvox.commons.util.IteratorWithException;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import org.apache.log4j.Logger;

/**
 *
 * @author pverley
 */
public class EchoWeightsFile {

    private final File file;
    private IteratorWithException<EchoesWeight> it;
    private EchoesWeight weight;
    private String scanName;
    // logger
    private final static Logger LOGGER = Logger.getLogger(EchoWeightsFile.class);

    public EchoWeightsFile(File file) {
        this.file = file;
    }

    public void setScanName(String scanName) {
        this.scanName = scanName;
    }

    public File getFile() {
        return file;
    }

    public void init() throws Exception {

        // checks whether file exists
        if (!file.exists()) {
            throw new FileNotFoundException(file.getAbsolutePath());
        }

        // determine whether the provided file is the weight table or
        // the file that links a scan to a weight table
        try {
            // tryout as weight table file
            it = iterator(file);
            weight = it.next();
        } catch (Exception ex1) {
            // it failed, alternatively tryout as weight file map
            HashMap<String, String> echoWeightMap = readCSV(file);
            String key = findKey(echoWeightMap, scanName);
            if (null != key) {
                it = iterator(new File(echoWeightMap.get(key)));
                weight = it.next();
            } else {
                LOGGER.warn("Could not find any echo weight file associated to scan " + scanName + " in parameter file " + file);
            }
        }
    }

    private HashMap<String, String> readCSV(File file) throws FileNotFoundException, IOException {

        HashMap<String, String> map = new HashMap<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            // skip header
            reader.readLine();
            String line;
            while (null != (line = reader.readLine()) && !line.trim().isEmpty()) {
                String[] split = line.split("\t");
                if (2 != split.length) {
                    throw new IOException("Invalid line " + line + " in file " + file.getName() + ". Expect RXP_NAME \t CSV_FILE");
                }
                // expected split[0] = RXP_NAME split[1] CSV_FILE
                String csv = new File(file.toURI().resolve(split[1])).getCanonicalPath();
                map.put(split[0], csv);
            }
        }
        return map;
    }

    private String findKey(HashMap<String, String> map, String rxp) {

        for (String key : map.keySet()) {
            //System.out.println(rxp + " " + key + " startsWith? " + rxp.startsWith(key));
            if (rxp.startsWith(key)) {
                return key;
            }
        }
        return null;
    }

    public IteratorWithException<EchoesWeight> iterator(File weightFile) throws Exception {

        IteratorWE it = new IteratorWE(weightFile);
        it.init();
        return it;
    }

    private class IteratorWE implements IteratorWithException<EchoesWeight> {

        private final File weightFile;
        private boolean hasNextCalled;
        private EchoesWeight currentShot;
        private final String sep = "\t";
        private int l = 1;
        private BufferedReader reader;

        IteratorWE(File weightFile) {
            this.weightFile = weightFile;
        }

        void init() throws Exception {
            try {
                LOGGER.info("Open echoes weight file " + weightFile);
                reader = new BufferedReader(new FileReader(weightFile));
                //skip header
                reader.readLine();
            } catch (Exception ex) {
                throw new Exception("Error reading echo weight file " + weightFile.getName(), ex);
            }
        }

        @Override
        public boolean hasNext() throws Exception {

            if (!hasNextCalled) {
                hasNextCalled = true;
                currentShot = getNextShot();
            }

            return currentShot != null;
        }

        private EchoesWeight getNextShot() throws Exception {

            String line;
            try {
                if ((line = reader.readLine()) != null) {
                    l++;
                    String[] shotLine = line.split(sep);
                    return new EchoesWeight(Integer.valueOf(shotLine[0]), Double.valueOf(shotLine[1]));
                } else {
                    reader.close();
                }
            } catch (IOException | NumberFormatException ex) {
                throw new Exception("Error reading echo weight file " + weightFile.getName() + " at line " + l, ex);
            }
            return null;
        }

        @Override
        public EchoesWeight next() throws Exception {

            if (hasNextCalled) {
                hasNextCalled = false;
                return currentShot;
            } else {
                return getNextShot();
            }
        }
    }

    public double getWeightCorrection(int shotID) throws Exception {

        // correction factor set to one by default (no correction)
        double weightCorr = 1.d;
        // match shot ID
        while (null != weight && weight.shotID < shotID) {
            weight = it.next();
        }
        // if shot ID matched, get weight correction factor
        if (null != weight && weight.shotID == shotID) {
            weightCorr = weight.weight;
        }
        // return weight correction factor
        return weightCorr;
    }

    public class EchoesWeight {

        public final int shotID;
        public final double weight;

        public EchoesWeight(int shotID, double weight) {
            this.shotID = shotID;
            this.weight = weight;
        }
    }

}
