/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.amapvox.shot.filter;

import org.amapvox.commons.util.IteratorWithException;
import org.amapvox.commons.util.filter.Filter;
import org.amapvox.shot.Shot;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Objects;
import org.amapvox.shot.Echo;
import org.apache.log4j.Logger;

/**
 *
 * @author pverley
 */
public class EchoRankFilter implements Filter<Echo> {

    private final File file;
    private final Behavior behavior;
    private IteratorWithException<Echoes> iterator;
    private Echoes echoes;
    private String scanName;
    // logger
    private final static Logger LOGGER = Logger.getLogger(EchoRankFilter.class);

    public EchoRankFilter(String file, Behavior behavior) {
        this.file = new File(file);
        this.behavior = behavior;
    }

    public void setScanName(String scanName) {
        this.scanName = scanName;
    }

    @Override
    public boolean equals(Object o) {

        if (null != o && o instanceof EchoRankFilter) {
            EchoRankFilter f = (EchoRankFilter) o;
            return file.equals(f.getFile()) && behavior.equals(f.behavior);
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 79 * hash + Objects.hashCode(this.file);
        hash = 79 * hash + Objects.hashCode(this.behavior);
        return hash;
    }

    @Override
    public void init() throws Exception {

        // checks whether file exists
        if (!file.exists()) {
            throw new FileNotFoundException(file.getAbsolutePath());
        }

        // determine whether the provided file is the echo filter table or
        // the file that links a scan to an echo filter file.
        try {
            // tryout as echo filter file
            iterator = iterator(file);
            echoes = iterator.next();
        } catch (Exception ex1) {
            // it failed, alternatively tryout as filter file map
            HashMap<String, String> echoFilterMap = readCSV(file);
            String key = findKey(echoFilterMap, scanName);
            if (null != key) {
                iterator = iterator(new File(echoFilterMap.get(key)));
                echoes = iterator.next();
            } else {
                LOGGER.warn("Could not find any echo filter file associated to scan " + scanName + " in parameter file " + file);
            }
        }
    }

    public File getFile() {
        return file;
    }

    public Behavior behavior() {
        return behavior;
    }

    @Override
    public boolean accept(Echo echo) throws Exception {

        if (echo.getRank() >= 0 && null != echoes) {
            int shotID = echo.getShot().index;
            while (null != echoes && echoes.shotID < shotID) {
                echoes = iterator.next();
            }
            if (null != echoes && echoes.shotID == shotID) {
                Shot shot = echo.getShot();
                if (shot.getEchoesNumber() != echoes.retained.length) {
                    StringBuilder sb = new StringBuilder();
                    sb.append("Inconsistent number of echoes in filter ");
                    sb.append(file.getName());
                    sb.append(", shot ").append(shot.index);
                    sb.append(", nEcho ").append(shot.getEchoesNumber());
                    sb.append(", nColumn ").append(echoes.retained.length);
                    throw new java.lang.ArrayIndexOutOfBoundsException(sb.toString());
                }
                return echoes.retained[echo.getRank()];
            }
        }
        // by default accept all echoes from shot not listed in CSV file
        return true;
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
                // expected split[0] = RXP_NAME, split[1] CSV_FILE
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

    private IteratorWithException<Echoes> iterator(File filterFile) throws Exception {

        IteratorWE it = new IteratorWE(filterFile);
        it.init();
        return it;
    }

    private class IteratorWE implements IteratorWithException<Echoes> {

        final private File filterFile;
        private boolean hasNextCalled;
        private Echoes currentShot;
        private final String sep = "\t";
        private int l = 1;
        private BufferedReader reader;

        IteratorWE(File filterFile) {
            this.filterFile = filterFile;
        }

        void init() throws Exception {
            try {
                LOGGER.info("Open echoes filtering file " + filterFile);
                reader = new BufferedReader(new FileReader(filterFile));
                //skip header
                reader.readLine();
            } catch (IOException ex) {
                throw new Exception("Error reading echo filter file " + filterFile.getName(), ex);
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

        private Echoes getNextShot() throws Exception {

            String line;
            try {
                if ((line = reader.readLine()) != null) {
                    l++;
                    String[] shotLine = line.split(sep);
                    return new Echoes(Integer.valueOf(shotLine[0]), toBoolean(Arrays.copyOfRange(shotLine, 1, shotLine.length), behavior.equals(Behavior.DISCARD)));
                } else {
                    reader.close();
                }
            } catch (IOException | NumberFormatException ex) {
                throw new Exception("Error reading echo filter file " + filterFile.getName() + " at line " + l, ex);
            }
            return null;
        }

        @Override
        public Echoes next() throws Exception {

            if (hasNextCalled) {
                hasNextCalled = false;
                return currentShot;
            } else {
                return getNextShot();
            }
        }

        /**
         * Converts string array of integers into boolean array. reverse
         * arguments returns the negation of the boolean array
         */
        private boolean[] toBoolean(String[] str, boolean reverse) {
            boolean[] bln = new boolean[str.length];
            for (int i = 0; i < bln.length; i++) {
                bln[i] = reverse
                        ? (Integer.valueOf(str[i]) != 1)
                        : (Integer.valueOf(str[i]) == 1);
            }
            return bln;
        }
    }

    private class Echoes {

        private final int shotID;
        private final boolean[] retained;

        public Echoes(int shotID, boolean[] retained) {
            this.shotID = shotID;
            this.retained = retained;
        }
    }
}
