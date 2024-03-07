package org.amapvox.shot.weight;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import org.amapvox.commons.util.IteratorWithException;
import org.amapvox.shot.Echo;
import org.amapvox.shot.Shot;
import org.amapvox.voxelisation.VoxelizationCfg;
import org.apache.log4j.Logger;

/**
 * Provide weight correction factor based on shot ID.
 *
 * Weight correction factor is read in CSV file. Two columns, shot ID (integer)
 * and correction factor (float).
 *
 * For multiple scans, intermediate text format with two columns 1. scan name
 * and 2. path to weight correction text file.
 *
 * @author Philippe Verley
 */
public class ShotEchoWeight extends EchoWeight {

    private File file;
    private ShotWeightIterator it;
    private ShotWeight shotWeight;
    private String scanName;
    // logger
    private final static Logger LOGGER = Logger.getLogger(ShotEchoWeight.class);

    public ShotEchoWeight(boolean enabled) {
        super(enabled);
    }

    @Override
    public void init(VoxelizationCfg cfg) throws IOException {

        this.file = cfg.getShotEchoWeightFile();
        this.scanName = cfg.getLidarScan().getFile().getName();

        // checks whether file exists
        if (!file.exists()) {
            throw new FileNotFoundException(file.getAbsolutePath());
        }

        // determine whether the provided file is the weight table or
        // the file that links a scan to a weight table
        try {
            // tryout as weight table file
            it = iterator(file);
            shotWeight = it.next();
        } catch (Exception ex1) {
            // it failed, alternatively tryout as weight file map
            HashMap<String, String> echoWeightMap = readCSV(file);
            String key = findKey(echoWeightMap, scanName);
            if (null != key) {
                it = iterator(new File(echoWeightMap.get(key)));
                shotWeight = it.next();
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

    private ShotWeightIterator iterator(File weightFile) throws IOException {

        ShotWeightIterator swIterator = new ShotWeightIterator(weightFile);
        swIterator.init();
        return swIterator;
    }

    private class ShotWeightIterator implements IteratorWithException<ShotWeight> {

        private final File weightFile;
        private boolean hasNextCalled;
        private ShotWeight currentShot;
        private final String sep = "\t";
        private int l = 1;
        private BufferedReader reader;

        ShotWeightIterator(File weightFile) {
            this.weightFile = weightFile;
        }

        void init() throws IOException {
            try {
                LOGGER.info("Open echoes weight file " + weightFile);
                reader = new BufferedReader(new FileReader(weightFile));
                //skip header
                reader.readLine();
            } catch (Exception ex) {
                throw new IOException("Error reading echo weight file " + weightFile.getName(), ex);
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

        private ShotWeight getNextShot() throws IOException {

            String line;
            try {
                if ((line = reader.readLine()) != null) {
                    l++;
                    String[] shotLine = line.split(sep);
                    return new ShotWeight(Integer.parseInt(shotLine[0]), Double.parseDouble(shotLine[1]));
                } else {
                    reader.close();
                }
            } catch (IOException | NumberFormatException ex) {
                throw new IOException("Error reading echo weight file " + weightFile.getName() + " at line " + l, ex);
            }
            return null;
        }

        @Override
        public ShotWeight next() throws IOException {

            if (hasNextCalled) {
                hasNextCalled = false;
                return currentShot;
            } else {
                return getNextShot();
            }
        }
    }

    @Override
    public void setWeight(Shot shot) {

        int shotID = shot.index;

        // match shot ID
        try {
            while (null != shotWeight && shotWeight.shotID < shotID) {
                shotWeight = it.next();
            }
        } catch (IOException ex) {
            shotWeight = null;
        }
    }

    @Override
    public double getWeight(Echo echo) {

        // shot index
        int shotID = echo.getShot().index;

        // correction factor set to one by default (no correction)
        double weightCorr = 1.d;

        // if shot ID matched, get weight correction factor
        if (null != shotWeight && shotWeight.shotID == shotID) {
            weightCorr = shotWeight.weight;
        }

        // return weight correction factor
        return weightCorr;
    }

    class ShotWeight {

        public final int shotID;
        public final double weight;

        public ShotWeight(int shotID, double weight) {
            this.shotID = shotID;
            this.weight = weight;
        }
    }
}
