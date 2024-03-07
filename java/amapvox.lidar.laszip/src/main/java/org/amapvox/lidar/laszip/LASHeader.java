/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.amapvox.lidar.laszip;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author pverley
 */
public class LASHeader extends Structure {

    public short file_source_ID;
    public short global_encoding;
    public int project_ID_GUID_data_1;
    public short project_ID_GUID_data_2;
    public short project_ID_GUID_data_3;
    public byte[] project_ID_GUID_data_4 = new byte[8];
    public byte version_major;
    public byte version_minor;
    public byte[] system_identifier = new byte[32];
    public byte[] generating_software = new byte[32];
    public short file_creation_day;
    public short file_creation_year;
    public short header_size;
    public int offset_to_point_data;
    public int number_of_variable_length_records;
    public byte point_data_format;
    public short point_data_record_length;
    public int number_of_point_records;
    public int[] number_of_points_by_return = new int[5];
    public double x_scale_factor;
    public double y_scale_factor;
    public double z_scale_factor;
    public double x_offset;
    public double y_offset;
    public double z_offset;
    public double max_x;
    public double min_x;
    public double max_y;
    public double min_y;
    public double max_z;
    public double min_z;

    private LASHeader(Pointer pointer) {
        super(pointer);
    }

    static public LASHeader read(Pointer pointer) {

        LASHeader header = new LASHeader(pointer);
        header.read();
        if (header.version_major == 1) {
            if (header.version_minor == 3) {
                V13 hv13 = new V13(pointer);
                hv13.read();
                // return v1.3 header
                return hv13;
            } else if (header.version_minor == 4) {
                V14 hv14 = new V14(pointer);
                hv14.read();
                // return v1.4 header
                return hv14;
            }
        }
        // return default header
        return header;
    }

    public boolean isV13() {
        return version_major == 1 && version_minor == 3;
    }

    public boolean isV14() {
        return version_major == 1 && version_minor == 4;
    }

    public long getNPoint() {
        return number_of_point_records;
    }

    @Override
    protected List<String> getFieldOrder() {
        return Arrays.asList(new String[]{
            "file_source_ID",
            "global_encoding",
            "project_ID_GUID_data_1",
            "project_ID_GUID_data_2",
            "project_ID_GUID_data_3",
            "project_ID_GUID_data_4",
            "version_major",
            "version_minor",
            "system_identifier",
            "generating_software",
            "file_creation_day",
            "file_creation_year",
            "header_size",
            "offset_to_point_data",
            "number_of_variable_length_records",
            "point_data_format",
            "point_data_record_length",
            "number_of_point_records",
            "number_of_points_by_return",
            "x_scale_factor",
            "y_scale_factor",
            "z_scale_factor",
            "x_offset",
            "y_offset",
            "z_offset",
            "max_x",
            "min_x",
            "max_y",
            "min_y",
            "max_z",
            "min_z"
        });
    }

    @Override
    public String toString() {

        StringBuilder s = new StringBuilder();
        s.append("file source ID").append('\t')
                .append(file_source_ID).append('\n');
        s.append("global encoding").append('\t')
                .append(global_encoding).append('\n');
        s.append("project ID GUID").append('\t')
                .append(project_ID_GUID_data_1).append("-")
                .append(project_ID_GUID_data_2).append("-")
                .append(project_ID_GUID_data_3).append("-")
                .append(Arrays.toString(project_ID_GUID_data_4)).append('\n');
        s.append("version major.minor").append('\t')
                .append(version_major).append(".").append(version_minor).append('\n');
        s.append("system identifier").append('\t')
                .append(new String(system_identifier)).append('\n');
        s.append("generating software").append('\t')
                .append(new String(generating_software)).append('\n');
        s.append("file create day/year").append('\t')
                .append(file_creation_day).append('/').append(file_creation_year).append('\n');
        s.append("header size").append('\t')
                .append(header_size).append('\n');
        s.append("offset to point data").append('\t')
                .append(offset_to_point_data).append('\n');
        s.append("number var. length records").append('\t')
                .append(number_of_variable_length_records).append('\n');
        s.append("point data format").append('\t')
                .append(point_data_format).append('\n');
        s.append("point data record length").append('\t')
                .append(point_data_record_length).append('\n');
        s.append("number of point records").append('\t')
                .append(number_of_point_records).append('\n');
        s.append("number of points by return").append('\t')
                .append(Arrays.toString(number_of_points_by_return)).append('\n');
        s.append("scale factor x y z").append('\t')
                .append(x_scale_factor).append(" ")
                .append(y_scale_factor).append(" ")
                .append(z_scale_factor).append(" ").append('\n');
        s.append("offset x y z").append('\t')
                .append(x_offset).append(" ")
                .append(y_offset).append(" ")
                .append(z_offset).append(" ").append('\n');
        s.append("min x y z").append('\t')
                .append(min_x).append(" ")
                .append(min_y).append(" ")
                .append(min_z).append(" ").append('\n');
        s.append("max x y z").append('\t')
                .append(max_x).append(" ")
                .append(max_y).append(" ")
                .append(max_z).append(" ").append('\n');

        return s.toString();

    }

    static public class V13 extends LASHeader {

        // LAS 1.3 and higher only
        public long start_of_waveform_data_packet_record;

        private V13(Pointer pointer) {
            super(pointer);
        }

        @Override
        protected List<String> getFieldOrder() {

            List<String> fields = new ArrayList();
            fields.addAll(super.getFieldOrder());
            fields.add("start_of_waveform_data_packet_record");
            return fields;
        }

        @Override
        public String toString() {

            StringBuilder s = new StringBuilder();
            s.append(super.toString());
            s.append("start_of_waveform_data_packet_record").append('\t')
                    .append(start_of_waveform_data_packet_record).append('\n');
            return s.toString();

        }

    }

    static public class V14 extends V13 {

        // LAS 1.4 and higher only
        public long start_of_first_extended_variable_length_record;
        public int number_of_extended_variable_length_records;
        public long extended_number_of_point_records;
        public long[] extended_number_of_points_by_return = new long[15];

        public V14(Pointer pointer) {
            super(pointer);
        }
        
        @Override
        public long getNPoint() {
        return extended_number_of_point_records;
    }

        @Override
        protected List<String> getFieldOrder() {

            List<String> fields = new ArrayList();
            fields.addAll(super.getFieldOrder());
            fields.addAll(Arrays.asList(new String[]{
                "start_of_first_extended_variable_length_record",
                "number_of_extended_variable_length_records",
                "extended_number_of_point_records",
                "extended_number_of_points_by_return"
            }));
            return fields;
        }

        @Override
        public String toString() {

            StringBuilder s = new StringBuilder();
            s.append(super.toString());
            s.append("start_of_first_extended_variable_length_record").append('\t')
                    .append(start_of_first_extended_variable_length_record).append('\n');
            s.append("number_of_extended_variable_length_records").append('\t')
                    .append(number_of_extended_variable_length_records).append('\n');
            s.append("extended_number_of_point_records").append('\t')
                    .append(extended_number_of_point_records).append('\n');
            s.append("extended_number_of_points_by_return").append('\t')
                    .append(Arrays.toString(extended_number_of_points_by_return)).append('\n');
            return s.toString();

        }

    }

}
