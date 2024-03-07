/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.amapvox.voxelisation.output;

import org.amapvox.commons.Voxel;
import org.amapvox.commons.AVoxTask;
import org.amapvox.commons.Util;
import org.amapvox.voxelisation.VoxelSpace;
import org.amapvox.voxelisation.VoxelizationCfg;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import ucar.ma2.Array;
import ucar.ma2.ArrayFloat;
import ucar.ma2.ArrayInt;
import ucar.ma2.DataType;
import ucar.ma2.Index;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Attribute;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFileWriter;
import ucar.nc2.Variable;

/**
 *
 * @author pverley
 */
public class NetCDFVoxelFileOutput extends AbstractOutput {

    public NetCDFVoxelFileOutput(AVoxTask task, VoxelizationCfg cfg, VoxelSpace vxsp, boolean enabled) {
        super(task, cfg, vxsp, enabled);
    }

    @Override
    public File[] write() throws IOException {

        String fileName = cfg.getOutputFile().getAbsolutePath();
        if (fileName.endsWith(".vox")) {
            fileName = fileName.substring(0, fileName.length() - 3) + "nc";
        }
        File outputFile = new File(fileName);
        // add dimensions
        try (NetcdfFileWriter writer = NetcdfFileWriter.createNew(NetcdfFileWriter.Version.netcdf3, outputFile.getAbsolutePath(), null)) {
            // add dimensions
            int nx = cfg.getDimension().x;
            int ny = cfg.getDimension().y;
            int nz = cfg.getDimension().z;
            Dimension xDim = writer.addDimension(null, "x", nx);
            Dimension yDim = writer.addDimension(null, "y", ny);
            Dimension zDim = writer.addDimension(null, "z", nz);

            List<Dimension> dims = new ArrayList<>();
            dims.add(xDim);
            dims.add(yDim);
            dims.add(zDim);

            // add variables
            for (OutputVariable variable : OutputVariable.values()) {
                if ((!variable.isCoordinateVariable()) && cfg.isOutputVariableEnabled(variable)) {
                    Variable ncvar = writer.addVariable(null, variable.getShortName(), variable.getType(), dims);
                    ncvar.addAttribute(new Attribute("units", variable.getUnits()));
                    ncvar.addAttribute(new Attribute("long_name", variable.getLongName()));
                    ncvar.addAttribute(new Attribute("missing_value", Float.NaN));
                }
            }

            // add global attributes (same as vox file)
            writer.addGroupAttribute(null, new Attribute("min_corner", cfg.getMinCorner().toString()));
            writer.addGroupAttribute(null, new Attribute("max_corner", cfg.getMaxCorner().toString()));
            writer.addGroupAttribute(null, new Attribute("split", cfg.getDimension().toString()));
            writer.addGroupAttribute(null, new Attribute("res", cfg.getVoxelSize().toString()));
            writer.addGroupAttribute(null, new Attribute("build-version", Util.getVersion()));

            // create the NetCDF file
            writer.create();

            // write data
            for (OutputVariable variable : OutputVariable.values()) {
                if ((!variable.isCoordinateVariable()) && cfg.isOutputVariableEnabled(variable)) {
                    Variable ncvar = writer.findVariable(variable.getShortName());
                    Array array = (DataType.INT == variable.getType())
                            ? new ArrayInt.D3(nx, ny, nz, true)
                            : new ArrayFloat.D3(nx, ny, nz);
                    Index ima = array.getIndex();
                    for (int i = 0; i < nx; i++) {
                        for (int j = 0; j < ny; j++) {
                            for (int k = 0; k < nz; k++) {
                                try {
                                    ima.set(i, j, k);
                                    Voxel voxel = vxsp.getVoxel(i, j, k);
                                    float value = (float) voxel.getFieldValue(Voxel.class, variable.getVariableName(), voxel);
                                    array.setFloat(ima, value);
                                } catch (SecurityException | NoSuchFieldException | IllegalAccessException ex) {
                                    LOGGER.error(null, ex);
                                }
                            }
                        }
                    }
                    try {
                        writer.write(ncvar, array);
                    } catch (InvalidRangeException ex) {
                        LOGGER.error(null, ex);
                    }
                }
            }
        }

        return new File[]{outputFile};
    }
}
