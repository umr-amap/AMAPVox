package org.amapvox.voxelfile;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import org.amapvox.commons.util.ArrayUtils;
import org.amapvox.commons.Util;
import org.amapvox.voxelisation.VoxelizationCfg;
import org.amapvox.voxelisation.output.OutputVariable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import javax.vecmath.Point3d;
import javax.vecmath.Point3f;
import javax.vecmath.Point3i;
import org.apache.log4j.Logger;

/**
 *
 * @author calcul
 */
public class VoxelFileHeader {

    private final static Logger LOGGER = Logger.getLogger(VoxelFileHeader.class);

    private final HashMap<String, String> properties;

    private Point3d minCorner;
    private Point3d maxCorner;
    private Point3i dimension;
    private Point3d voxelSize;
    private String[] columnNames;
    private String buildVersion;
    // default decimal format (temporary, should be read from .vox metadata)
    private int nFractionDigits;
    private int nLine;

    public VoxelFileHeader() {
        this.properties = new HashMap<>();
    }
    
    public VoxelFileHeader(HashMap<String, String> properties) throws IOException {
        this.properties = properties;
        presetProperties();
    } 

    public VoxelFileHeader(VoxelizationCfg cfg) {

        this.properties = new HashMap<>();

        setMinCorner(cfg.getMinCorner());
        setMaxCorner(cfg.getMaxCorner());
        setDimension(cfg.getDimension());
        updateVoxelSize();
        addProperty("nrecordmax", String.valueOf(cfg.getNTrRecordMax()));
        addProperty("nsubvoxel", String.valueOf(cfg.getSubVoxelSplit() ^ 3));
        setFractionDigits(cfg.getDecimalFormat().getMaximumFractionDigits());
        // column names
        columnNames = Arrays.asList(OutputVariable.values()).stream()
                .filter(v -> cfg.isOutputVariableEnabled(v))
                .map(v -> v.getShortName())
                .toArray(String[]::new);
    }

    public VoxelFileHeader crop(Point3d min, Point3d max, Point3i dim) {

        VoxelFileHeader header = new VoxelFileHeader();

        properties.keySet().forEach(key -> {
            header.addProperty(key, properties.get(key));
        });
        try {
            header.presetProperties();
        } catch (IOException ex) {
            java.util.logging.Logger.getLogger(VoxelFileHeader.class.getName()).log(Level.SEVERE, null, ex);
        }
        header.columnNames = this.columnNames;
        header.setMinCorner(min);
        header.setMaxCorner(max);
        header.setDimension(dim);

        return header;
    }

    public String getProperty(String key) {
        return properties.get(key.toLowerCase());
    }

    private void addProperty(String key, String value) {
        properties.put(key.toLowerCase(), value);
    }

    private void presetProperties() throws IOException {

        try {

            Point3d resolution = new Point3d();

            for (String key : properties.keySet()) {
                String value = properties.get(key);
                switch (key) {
                    case "min_corner":
                        minCorner = new Point3d(ArrayUtils.parseDoubleArray(value));
                        break;
                    case "max_corner":
                        maxCorner = new Point3d(ArrayUtils.parseDoubleArray(value));
                        break;
                    case "split":
                        dimension = new Point3i(ArrayUtils.parseIntArray(value));
                        break;
                    case "res":
                        try {
                            // voxel as cube (version <= 1.7.4)
                            double res = Double.valueOf(value);
                            resolution = new Point3d(res, res, res);
                        } catch (NumberFormatException ex) {
                            // voxel as cuboid (version > 1.7.4)
                            resolution = new Point3d(ArrayUtils.parseDoubleArray(value));
                        }
                        break;
                    case "build-version":
                        buildVersion = value;
                        break;
                    case "fraction-digits":
                        nFractionDigits = Integer.valueOf(value);
                        break;
                    case "colnames":
                        columnNames = value.split(" ");
                        break;
                    case "nline":
                        nLine = Integer.valueOf(value);
                        break;
                }
            }

            // check resolution / voxelSize
            updateVoxelSize();
            double EPSILON = 1e-5;
            Point3d diff = new Point3d(resolution);
            diff.sub(voxelSize);
            diff.absolute();
            if (diff.x > EPSILON || diff.y > EPSILON || diff.z > EPSILON) {
                StringBuilder sb = new StringBuilder();
                sb.append("Voxel file warning:").append('\n');
                sb.append("Recorded voxel size ");
                sb.append(new Point3f(resolution)).append('\n');
                sb.append("Effective voxel size ");
                sb.append(new Point3f(voxelSize));
                sb.append(" (computed from min, max coordinates and voxel space dimension)").append('\n');
                sb.append("AMAPVox will use effective voxel size ");
                sb.append(new Point3f(voxelSize));
                LOGGER.warn(sb.toString());
            }

        } catch (NumberFormatException ex) {
            throw new IOException("Header is invalid", ex);
        }
    }

    public int findColumnIndex(String name) {

        for (int i = 0; i < columnNames.length; i++) {
            if (name.equalsIgnoreCase(columnNames[i])) {
                return i;
            }
        }
        return -1;
    }

    public int findColumnIndex(OutputVariable variable) {
        int i = 0;
        for (String columnName : columnNames) {
            if (variable.isValidShortName(columnName)) {
                return i;
            }
            i++;
        }
        return -1;
    }

    private void updateVoxelSize() {

        voxelSize = new Point3d(
                (maxCorner.x - minCorner.x) / dimension.x,
                (maxCorner.y - minCorner.y) / dimension.y,
                (maxCorner.z - minCorner.z) / dimension.z
        );
        Point3f res = new Point3f(voxelSize);
        addProperty("res", String.valueOf(res));
    }

    public int getNLine() {
        return nLine;
    }

    public Point3d getMinCorner() {
        return minCorner;
    }

    public Point3d getMaxCorner() {
        return maxCorner;
    }

    public Point3i getDimension() {
        return dimension;
    }

    public Point3d getVoxelSize() {

        return voxelSize;
    }
    
    public String getColumnName(int i) {
        
        return (i >= 0 && i < columnNames.length)
                ? columnNames[i]
                : null;
    }

    public String[] getColumnNames() {
        return columnNames;
    }

    private void setMinCorner(Point3d minCorner) {
        this.minCorner = minCorner;
        properties.put("min_corner", String.valueOf(minCorner));
    }

    private void setMaxCorner(Point3d maxCorner) {
        this.maxCorner = maxCorner;
        properties.put("max_corner", String.valueOf(maxCorner));
    }

    private void setDimension(Point3i dimension) {
        this.dimension = dimension;
        properties.put("split", String.valueOf(dimension));
    }

    @Override
    public String toString() {

        // update version
        properties.put("build-version", getVersion());

        StringBuilder header = new StringBuilder();
        header.append("VOXEL SPACE");
        List<String> keys = new ArrayList<>(properties.keySet());
        Collections.sort(keys);
        keys.stream().forEachOrdered(key -> {
            String value = properties.get(key);
            header.append("\n").append("#").append(key).append(":").append(value);
        });

        StringBuilder columns = new StringBuilder();
        for (String column : this.getColumnNames()) {
            columns.append(column).append(" ");
        }
        return header.toString() + "\n" + columns.toString().trim();

    }

    public String getVersion() {

        if (null == buildVersion) {
            buildVersion = Util.getVersion();
        }
        return buildVersion;
    }

    private void setFractionDigits(int nFractionDigits) {
        this.nFractionDigits = nFractionDigits;
        properties.put("fraction-digits", String.valueOf(nFractionDigits));
    }

    public int getFractionDigits() {
        return nFractionDigits;
    }
}
