/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.amapvox.voxelfile;

import org.amapvox.voxelisation.output.OutputVariable;
import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 *
 * @author calcul
 */
public class VoxelFileReader implements Iterable<VoxelFileVoxel>, Closeable {

    private final static Logger LOGGER = Logger.getLogger(VoxelFileReader.class);

    private final VoxelFileHeader header;
    private final File file;
    private BufferedReader reader;

    public VoxelFileReader(File file) throws IOException {

        this.file = file;
        header = readHeader();
        reader = new BufferedReader(new FileReader(file));
    }

    public static boolean isValid(File file) {

        try (VoxelFileReader reader = new VoxelFileReader(file)) {
            return true;
        } catch (IOException ex) {
            return false;
        }
    }

    public File getFile() {
        return file;
    }

    public VoxelFileHeader getHeader() {
        return header;
    }

    private VoxelFileHeader readHeader() throws IOException {

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {

            String identifier = reader.readLine();

            if (!identifier.equals("VOXEL SPACE")) {
                reader.close();
                throw new IOException("Voxel file is invalid, VOXEL SPACE identifier is missing");
            }

            HashMap<String, String> properties = new HashMap<>();
            int nLine = 0;

            String line;
            while (null != (line = reader.readLine().trim())) {

                // increment line number
                nLine++;

                // ignore empty line
                if (!line.isEmpty()) {
                    // header is over, leave the loop
                    if (!line.startsWith("#")) {
                        break;
                    }

                    // valid header, extract parameters
                    // may be several parameters per line
                    String[] parameters = line.split("#");
                    // loop over the parameters
                    for (String parameter : parameters) {
                        if (parameter.isEmpty()) {
                            continue;
                        }
                        // expected parameter format key:value
                        String[] key_value = parameter.split(":");
                        if (key_value.length != 2) {
                            Logger.getLogger(VoxelFileReader.class).log(Level.WARN, "Wrong header parameter format. Expected key:value. Found " + parameter);
                            continue;
                        }
                        String key = key_value[0].trim();
                        String value = key_value[1].trim();
                        properties.put(key, value);
                    }
                }
            }
            // increment to include column name in header
            nLine++;
            // add header size in properties
            properties.put("nline", String.valueOf(nLine));
            // add column names in properties
            properties.put("colnames", line);

            return new VoxelFileHeader(properties);

        } catch (FileNotFoundException ex) {
            throw new IOException("Cannot find voxel file", ex);
        } catch (IOException ex) {
            throw new IOException("Error reading voxel file header", ex);
        }
    }

    public int findColumn(OutputVariable variable) {

        for (int column = 0; column < header.getColumnNames().length; column++) {
            try {
                OutputVariable v = OutputVariable.find(header.getColumnName(column));
                if (v.equals(variable)) {
                    return column - 3;
                }
            } catch (NullPointerException ex) {
            }
        }

        return -1;
    }

    @Override
    public Iterator<VoxelFileVoxel> iterator() {

        try {
            // reset reader
            if (null != reader) {
                reader.close();
            }
            reader = new BufferedReader(new FileReader(file));
            // skip header
            for (int i = 0; i < header.getNLine(); i++) {
                reader.readLine();
            }
        } catch (IOException ex) {
            LOGGER.error("Cannot read voxel file", ex);
            return null;
        }

        Iterator<VoxelFileVoxel> it = new Iterator<VoxelFileVoxel>() {

            String line;
            int currentVoxelIndex = 0;
            int nvar = header.getColumnNames().length - 3;

            @Override
            public boolean hasNext() {

                try {
                    if (null == line) {
                        line = reader.readLine();
                        currentVoxelIndex++;
                    }
                    return line != null;
                } catch (IOException ex) {
                    return false;
                }

            }

            @Override
            public VoxelFileVoxel next() {

                if (null == line) {
                    try {
                        line = reader.readLine();
                        currentVoxelIndex++;
                    } catch (IOException ex) {
                    }
                }

                if (null != line) {
                    String[] split = line.split(" ");
                    VoxelFileVoxel voxel = new VoxelFileVoxel(
                            Integer.valueOf(split[0]), // i
                            Integer.valueOf(split[1]), // j
                            Integer.valueOf(split[2]), // k
                            Arrays.copyOfRange(split, 3, split.length));
                    // flush current line
                    line = null;
                    return voxel;
                }
                return null;
            }
        };

        return it;
    }

    @Override
    public void close() throws IOException {
        reader.close();
    }

}
