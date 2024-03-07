/*--------------------------------------------------------------------------

  Ce programme utilise LASZip "https://github.com/LAStools/LAStools/blob/master/LASzip"
  sous la licence:

  LICENSE AGREEMENT (for LASzip LiDAR compression):

  LASzip is open-source and is licensed with the standard LGPL version 2.1
  (see COPYING.txt).

  This software is distributed WITHOUT ANY WARRANTY and without even the
  implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.

--------------------------------------------------------------------------

  PROGRAMMERS:

  martin@rapidlasso.com

--------------------------------------------------------------------------

  COPYRIGHT:

  (c) 2007-2014, martin isenburg, rapidlasso - fast tools to catch reality

--------------------------------------------------------------------------*/
#ifndef LASZIPLIBRARY_H
#define LASZIPLIBRARY_H

#ifdef WIN64
    #include "win64/jni.h"
    #include "win64/jni_md.h"
#else
    #include "linux64/jni.h"
    #include "linux64/jni_md.h"
#endif

#include <iostream>
#include <fstream>
#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#include "laszip.hpp"
#include "bytestreamout_file.hpp"
#include "bytestreamin_file.hpp"
#include "laswritepoint.hpp"
#include "lasreadpoint.hpp"

typedef int                laszip_BOOL;
typedef unsigned char      laszip_U8;
typedef unsigned short     laszip_U16;
typedef unsigned int       laszip_U32;
typedef unsigned long long laszip_U64;
typedef char               laszip_I8;
typedef short              laszip_I16;
typedef int                laszip_I32;
typedef long               laszip_I64;
typedef char               laszip_CHAR;
typedef float              laszip_F32;
typedef double             laszip_F64;
typedef void*              laszip_POINTER;

typedef struct laszip_vlr
{
    laszip_U16 reserved;
    laszip_CHAR user_id[16];
    laszip_U16 record_id;
    laszip_U16 record_length_after_header;
    laszip_CHAR description[32];
    laszip_U8* data;
} laszip_vlr_struct;

typedef struct laszip_point
{
    laszip_I32 X;
    laszip_I32 Y;
    laszip_I32 Z;
    laszip_U16 intensity;
    laszip_U8 return_number : 3;
    laszip_U8 number_of_returns_of_given_pulse : 3;
    laszip_U8 scan_direction_flag : 1;
    laszip_U8 edge_of_flight_line : 1;
    laszip_U8 classification;
    laszip_I8 scan_angle_rank;
    laszip_U8 user_data;
    laszip_U16 point_source_ID;

    laszip_F64 gps_time;
    laszip_U16 rgb[4];
    laszip_U8 wave_packet[29];

    // LAS 1.4 only
    laszip_U8 extended_point_type : 2;
    laszip_U8 extended_scanner_channel : 2;
    laszip_U8 extended_classification_flags : 4;
    laszip_U8 extended_classification;
    laszip_U8 extended_return_number : 4;
    laszip_U8 extended_number_of_returns_of_given_pulse : 4;
    laszip_I16 extended_scan_angle;

    laszip_I32 num_extra_bytes;
    laszip_U8* extra_bytes;

} laszip_point_struct;

typedef struct laszip_header
{
    laszip_U16 file_source_ID;
    laszip_U16 global_encoding;
    laszip_U32 project_ID_GUID_data_1;
    laszip_U16 project_ID_GUID_data_2;
    laszip_U16 project_ID_GUID_data_3;
    laszip_CHAR project_ID_GUID_data_4[8];
    laszip_U8 version_major;
    laszip_U8 version_minor;
    laszip_CHAR system_identifier[32];
    laszip_CHAR generating_software[32];
    laszip_U16 file_creation_day;
    laszip_U16 file_creation_year;
    laszip_U16 header_size;
    laszip_U32 offset_to_point_data;
    laszip_U32 number_of_variable_length_records;
    laszip_U8 point_data_format;
    laszip_U16 point_data_record_length;
    laszip_U32 number_of_point_records;
    laszip_U32 number_of_points_by_return[5];
    laszip_F64 x_scale_factor;
    laszip_F64 y_scale_factor;
    laszip_F64 z_scale_factor;
    laszip_F64 x_offset;
    laszip_F64 y_offset;
    laszip_F64 z_offset;
    laszip_F64 max_x;
    laszip_F64 min_x;
    laszip_F64 max_y;
    laszip_F64 min_y;
    laszip_F64 max_z;
    laszip_F64 min_z;

    // LAS 1.3 and higher only
    laszip_U64 start_of_waveform_data_packet_record;

    // LAS 1.4 and higher only
    laszip_U64 start_of_first_extended_variable_length_record;
    laszip_U32 number_of_extended_variable_length_records;
    laszip_U64 extended_number_of_point_records;
    laszip_U64 extended_number_of_points_by_return[15];

    // optional
    laszip_U32 user_data_in_header_size;
    laszip_U8* user_data_in_header;

    // optional VLRs
    laszip_vlr_struct* vlrs;

    // optional
    laszip_U32 user_data_after_header_size;
    laszip_U8* user_data_after_header;

} laszip_header_struct;

typedef struct laszip_dll {
    laszip_header_struct header;
    I64 p_count;
    I64 npoints;
    laszip_point_struct point;
    U8** point_items;
    FILE* file;
    ByteStreamIn* streamin;
    LASreadPoint* reader;
    ByteStreamOut* streamout;
    LASwritePoint* writer;
    CHAR error[1024];
    CHAR warning[1024];
} laszip_dll_struct;

#ifndef _Included_fr_amap_amapvox_als_laz_LazExtraction
#define _Included_fr_amap_amapvox_als_laz_LazExtraction
#ifdef __cplusplus
extern "C" {
#endif

    JNIEXPORT void JNICALL Java_fr_amap_amapvox_als_laz_LazExtraction_afficherBonjour
        (JNIEnv *, jobject);

    JNIEXPORT jlong JNICALL Java_fr_amap_amapvox_als_laz_LazExtraction_instantiateLasZip
        (JNIEnv *, jobject);

    JNIEXPORT void JNICALL Java_fr_amap_amapvox_als_laz_LazExtraction_deleteLasZip
        (JNIEnv *, jobject, jlong pointer);

    JNIEXPORT int JNICALL Java_fr_amap_amapvox_als_laz_LazExtraction_open
    (JNIEnv *, jobject, jlong pointer, jstring file_name);

    JNIEXPORT void JNICALL Java_fr_amap_amapvox_als_laz_LazExtraction_readAllPoints
    (JNIEnv *, jobject, jlong pointer);

    JNIEXPORT jobject JNICALL Java_fr_amap_amapvox_als_laz_LazExtraction_getNextPoint
    (JNIEnv *, jobject, jlong pointer);

    JNIEXPORT jobject JNICALL Java_fr_amap_amapvox_als_laz_LazExtraction_getBasicHeader
    (JNIEnv *, jobject, jlong pointer);

#ifdef __cplusplus
}
#endif
#endif

#endif
