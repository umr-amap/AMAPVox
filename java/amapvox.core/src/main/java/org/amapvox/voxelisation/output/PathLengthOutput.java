/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.amapvox.voxelisation.output;

import ch.systemsx.cisd.hdf5.HDF5DataSet;
import ch.systemsx.cisd.hdf5.HDF5Factory;
import ch.systemsx.cisd.hdf5.IHDF5Writer;
import org.amapvox.commons.AVoxTask;
import org.amapvox.voxelisation.VoxelSpace;
import org.amapvox.voxelisation.VoxelizationCfg;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.vecmath.Point3i;

/**
 *
 * @author pverley
 */
public class PathLengthOutput extends AbstractOutput {

    // dimensions of the voxel space
    private final int nx, ny, nz;
    // maximum size path length list
    private int arrayMaxSize = 100;
    // HDF5 file name
    private File hdf5File;
    // not intercepted free path length
    private HashMap<Integer, List<Float>> fpl;
    private HashMap<Integer, HDF5DataSet> fplDataSet;
    private int[] nblockNoHit;
    // intercepted free path length
    private HashMap<Integer, List<Float>> ifpl;
    private HashMap<Integer, HDF5DataSet> ifplDataSet;
    private int[] nblockHit;
    // potential path length
    private HashMap<Integer, List<Float>> ppl;
    private HashMap<Integer, HDF5DataSet> pplDataSet;
    // HDF5 writer
    private IHDF5Writer writer;

    public PathLengthOutput(AVoxTask task, VoxelizationCfg cfg, VoxelSpace vxsp, boolean enabled) {
        super(task, cfg, vxsp, enabled);

        nx = cfg.getDimension().x;
        ny = cfg.getDimension().y;
        nz = cfg.getDimension().z;
    }

    public void init() throws IOException {

        // initializes arrays and maps
        fpl = new HashMap<>();
        fplDataSet = new HashMap<>();
        nblockNoHit = new int[nx * ny * nz];
        ifpl = new HashMap<>();
        ifplDataSet = new HashMap<>();
        nblockHit = new int[nx * ny * nz];
        ppl = new HashMap<>();
        pplDataSet = new HashMap<>();

        // create HDF5 file
        File voxFile = cfg.getOutputFile();
        String voxFilename = voxFile.getName();
        String hdf5Filename = voxFilename.substring(0, voxFilename.lastIndexOf(".")) + ".h5";
        hdf5File = new File(voxFile.getParent(), hdf5Filename);
        // delete existing file
        if (hdf5File.exists()) {
            hdf5File.delete();
        }
        writer = HDF5Factory.open(hdf5File.getAbsolutePath());

        // read parameter
        arrayMaxSize = cfg.getPathLengthMaxSize();
    }

    synchronized public void addPathLengthRecord(Point3i voxel, float freePathLength, float potentialPathLength) {
        // voxel coordinates to voxel index
        int index = ijkToIndex(voxel);

        if (Float.isNaN(potentialPathLength)) {
            if (!fpl.containsKey(index)) {
                fpl.put(index, new ArrayList<>(arrayMaxSize));
            }
            fpl.get(index).add(freePathLength);
            // check max size
            if (fpl.get(index).size() >= arrayMaxSize) {
                write(index, fpl.get(index), fplDataSet, "nohit/freePathLength", nblockNoHit[index]);
                fpl.get(index).clear();
                nblockNoHit[index]++;
            }
        } else {
            if (!ifpl.containsKey(index)) {
                ifpl.put(index, new ArrayList<>(arrayMaxSize));
                ppl.put(index, new ArrayList<>(arrayMaxSize));
            }
            ifpl.get(index).add(freePathLength);
            ppl.get(index).add(potentialPathLength);
            // check max size
            if (ifpl.get(index).size() >= arrayMaxSize) {
                write(index, ifpl.get(index), ifplDataSet, "hit/freePathLength", nblockHit[index]);
                ifpl.get(index).clear();
                write(index, ppl.get(index), pplDataSet, "hit/potentialPathLength", nblockHit[index]);
                ppl.get(index).clear();
                nblockHit[index]++;
            }
        }
    }

    private void write(int voxel, List<Float> pathLength, HashMap<Integer, HDF5DataSet> dataSets, String subgroup, int nblock) {

        if (!dataSets.containsKey(voxel)) {
            // create dataset
            Point3i ijk = indexToijk(voxel);
            String group = new StringBuilder()
                    .append("i").append(ijk.x)
                    .append("j").append(ijk.y)
                    .append("k").append(ijk.z)
                    .append("/").append(subgroup)
                    .toString();
            dataSets.put(voxel, writer.float32().createArrayAndOpen(group, arrayMaxSize, arrayMaxSize));
        }
        // write array to HDF5 file
        if (pathLength.size() < arrayMaxSize) {
            writer.float32().writeArrayBlockWithOffset(dataSets.get(voxel), toFloatArray(pathLength), pathLength.size(), nblock * arrayMaxSize);
        } else {
            writer.float32().writeArrayBlock(dataSets.get(voxel), toFloatArray(pathLength), nblock);
        }
    }

    private float[] toFloatArray(List<Float> list) {

        float[] array = new float[list.size()];
        for (int i = 0; i < array.length; i++) {
            array[i] = list.get(i);
        }
        return array;
    }

    private int ijkToIndex(Point3i voxel) {
        return voxel.z * nx * ny + voxel.y * nx + voxel.x;
    }

    private Point3i indexToijk(int index) {
        int k = index / (nx * ny);
        int j = index / nx - k * ny; //(index - k * nx * ny) / nx;
        int i = index - k * nx * ny - j * nx;
        return new Point3i(i, j, k);
    }

    @Override
    public File[] write() throws IOException {

        // flush last data from arraylist to hdf5 file
        long nvoxel = fpl.keySet().size() + ifpl.keySet().size();
        //long ivoxel = 0;
        int ivoxel = 0;
        String hdf5Filename = hdf5File.getName();
        LOGGER.info("Writing file " + hdf5File.getAbsolutePath());
        for (int voxel : fpl.keySet()) {
            if (!fpl.get(voxel).isEmpty()) {
                write(voxel, fpl.get(voxel), fplDataSet, "nohit/freePathLength", nblockNoHit[voxel]);
            }
            ivoxel++;
            progress("Writing " + hdf5Filename + " voxel " + ivoxel + "/" + nvoxel, ivoxel, nvoxel);

        }
        for (int voxel : ifpl.keySet()) {
            if (!ifpl.get(voxel).isEmpty()) {
                write(voxel, ifpl.get(voxel), ifplDataSet, "hit/freePathLength", nblockHit[voxel]);
                write(voxel, ppl.get(voxel), pplDataSet, "hit/potentialPathLength", nblockHit[voxel]);
            }
            ivoxel++;
            progress("Writing " + hdf5Filename + " voxel " + ivoxel + "/" + nvoxel, ivoxel, nvoxel);
        }

        // clear hashmaps
        fpl.clear();
        ifpl.clear();
        ppl.clear();

        // close writer
        writer.close();

        // return HDF5 file
        return new File[]{hdf5File};
    }
}
