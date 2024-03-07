#include "lasziplibrary.h"

jclass lasPointFormat0Class;
jmethodID lasPointFormat0Constructor;

JNIEXPORT void JNICALL Java_fr_amap_amapvox_als_laz_LazExtraction_afficherBonjour(JNIEnv *env, jobject obj)
{
    printf(" Bonjour\n ");
    return;
}



JNIEXPORT jlong JNICALL Java_fr_amap_amapvox_als_laz_LazExtraction_instantiateLasZip(JNIEnv *env, jobject obj)
{
    laszip_dll_struct* laszip_dll = new laszip_dll_struct;
    memset(laszip_dll, 0, sizeof(laszip_dll_struct));

    /*jclass c = env->FindClass("fr/amap/amapvox/als/LasPoint");
    if (c == NULL){
        return JNI_ERR;
    }

    lasPointFormat0Class = (jclass)env->NewGlobalRef(c);
    if (lasPointFormat0Class == NULL){
        return JNI_ERR;
    }*/

    /*lasPointFormat0Constructor = env->GetMethodID(lasPointFormat0Class, "<init>", "(IIIBBIBD)V");
    if (lasPointFormat0Constructor == NULL){
        return JNI_ERR;
    }*/

    return (jlong)laszip_dll;
}

JNIEXPORT void JNICALL Java_fr_amap_amapvox_als_laz_LazExtraction_deleteLasZip(JNIEnv *env, jobject obj, jlong pointer)
{
    long pointerAddress = pointer;
    laszip_dll_struct* laszip_dll = (laszip_dll_struct*)pointerAddress;

    delete laszip_dll;

    /*if (lasPointFormat0Class != NULL) {
        env->DeleteGlobalRef(lasPointFormat0Class);
    }*/
}

jint JNI_OnLoad(JavaVM* vm, void* reserved) {
    JNIEnv* env;
    if ((vm)->GetEnv((void **) &env, JNI_VERSION_1_8) != JNI_OK) {
        return JNI_ERR;
    } else {
        jclass c = env->FindClass("fr/amap/amapvox/als/LasPoint");
        if (c == NULL){
            return JNI_ERR;
        }
        lasPointFormat0Class = (jclass)env->NewGlobalRef(c);
        lasPointFormat0Constructor = env->GetMethodID(lasPointFormat0Class, "<init>", "(IIIBBIBD)V");
        if (lasPointFormat0Constructor == NULL){
            return JNI_ERR;
        }
    }
    return JNI_VERSION_1_8;
}

void JNI_OnUnload(JavaVM *vm, void *reserved) {
    JNIEnv* env;
    if ((vm)->GetEnv((void **) &env, JNI_VERSION_1_8) != JNI_OK) {
        // Something is wrong but nothing we can do about this :(
        return;
    } else {
        if (0 != NULL){
            (env)->DeleteGlobalRef(lasPointFormat0Class);
        }
    }
}

//morceau de code trouvé sur Internet qui permet de convertir les jstring contenant des accents
char* JNU_GetStringNativeChars(JNIEnv* env, jstring jstr) {
    //!!!!! C'est ces définitions qu'il me manquait...
    jclass Class_java_lang_String = env->FindClass("java/lang/String");
    jmethodID MID_String_getBytes = env->GetMethodID(Class_java_lang_String, "getBytes", "()[B");


    jbyteArray bytes = 0;
    //on utilise ExceptionCheck au lieu de ExceptionOccured => plus besoin de exc
//	jthrowable exc;
    char* result = 0;

    if (env->EnsureLocalCapacity(2) < 0) {
        return 0;  // out of memory error
    }

    //casté pour que ça marche...
    bytes = (jbyteArray) env->CallObjectMethod(jstr, MID_String_getBytes);

    //on utilise ExceptionCheck au lieu de ExceptionOccured...
//	exc = env->ExceptionOccured();
    jboolean exc = env->ExceptionCheck();

    if (!exc) {
        jint len = env->GetArrayLength(bytes);
        result = (char*)malloc(len+1);
        if (result == 0) {
            env->DeleteLocalRef(bytes);

            return 0;
        }
        env->GetByteArrayRegion(bytes, 0, len, (jbyte*)result);
        result[len] = 0; // NULL-terminate
    }
    else {
        printf("Exception occured...\n");
    }
    env->DeleteLocalRef(bytes);

    return result;
}


JNIEXPORT int JNICALL Java_fr_amap_amapvox_als_laz_LazExtraction_open(JNIEnv *env, jobject obj, jlong pointer, jstring file_name)
{
    long pointerAddress = pointer;

    laszip_dll_struct* laszip_dll = (laszip_dll_struct*)pointerAddress;

    const char *fileName = JNU_GetStringNativeChars(env, file_name);

    std::cout << fileName << std::endl;

    laszip_dll->file = fopen(fileName, "rb");

    if (laszip_dll->file == 0)
    {
        sprintf(laszip_dll->error, "cannot open file '%s'", fileName);
        return -1;
    }

    //reader


    int compressed = 1;
    laszip_BOOL* is_compressed = &compressed;

    if (IS_LITTLE_ENDIAN())
        laszip_dll->streamin = new ByteStreamInFileLE(laszip_dll->file);
    else
        laszip_dll->streamin = new ByteStreamInFileBE(laszip_dll->file);


    U32 i;

    CHAR file_signature[5];
    try { laszip_dll->streamin->getBytes((U8*)file_signature, 4); } catch(...)
    {
        sprintf(laszip_dll->error, "reading header.file_signature");
        return 1;
    }
    if (strncmp(file_signature, "LASF", 4) != 0)
    {
        sprintf(laszip_dll->error, "wrong file_signature. not a LAS/LAZ file.");
        return 1;
    }
    try { laszip_dll->streamin->get16bitsLE((U8*)&(laszip_dll->header.file_source_ID)); } catch(...)
    {
        sprintf(laszip_dll->error, "reading header.file_source_ID");
        return 1;
    }
    try { laszip_dll->streamin->get16bitsLE((U8*)&(laszip_dll->header.global_encoding)); } catch(...)
    {
        sprintf(laszip_dll->error, "reading header.global_encoding");
        return 1;
    }
    try { laszip_dll->streamin->get32bitsLE((U8*)&(laszip_dll->header.project_ID_GUID_data_1)); } catch(...)
    {
        sprintf(laszip_dll->error, "reading header.project_ID_GUID_data_1");
        return 1;
    }
    try { laszip_dll->streamin->get16bitsLE((U8*)&(laszip_dll->header.project_ID_GUID_data_2)); } catch(...)
    {
        sprintf(laszip_dll->error, "reading header.project_ID_GUID_data_2");
        return 1;
    }
    try { laszip_dll->streamin->get16bitsLE((U8*)&(laszip_dll->header.project_ID_GUID_data_3)); } catch(...)
    {
        sprintf(laszip_dll->error, "reading header.project_ID_GUID_data_3");
        return 1;
    }
    try { laszip_dll->streamin->getBytes((U8*)laszip_dll->header.project_ID_GUID_data_4, 8); } catch(...)
    {
        sprintf(laszip_dll->error, "reading header.project_ID_GUID_data_4");
        return 1;
    }
    try { laszip_dll->streamin->getBytes((U8*)&(laszip_dll->header.version_major), 1); } catch(...)
    {
        sprintf(laszip_dll->error, "reading header.version_major");
        return 1;
    }
    try { laszip_dll->streamin->getBytes((U8*)&(laszip_dll->header.version_minor), 1); } catch(...)
    {
        sprintf(laszip_dll->error, "reading header.version_minor");
        return 1;
    }
    try { laszip_dll->streamin->getBytes((U8*)laszip_dll->header.system_identifier, 32); } catch(...)
    {
        sprintf(laszip_dll->error, "reading header.system_identifier");
        return 1;
    }
    try { laszip_dll->streamin->getBytes((U8*)laszip_dll->header.generating_software, 32); } catch(...)
    {
        sprintf(laszip_dll->error, "reading header.generating_software");
        return 1;
    }
    try { laszip_dll->streamin->get16bitsLE((U8*)&(laszip_dll->header.file_creation_day)); } catch(...)
    {
        sprintf(laszip_dll->error, "reading header.file_creation_day");
        return 1;
    }
    try { laszip_dll->streamin->get16bitsLE((U8*)&(laszip_dll->header.file_creation_year)); } catch(...)
    {
        sprintf(laszip_dll->error, "reading header.file_creation_year");
        return 1;
    }
    try { laszip_dll->streamin->get16bitsLE((U8*)&(laszip_dll->header.header_size)); } catch(...)
    {
        sprintf(laszip_dll->error, "reading header.header_size");
        return 1;
    }
    try { laszip_dll->streamin->get32bitsLE((U8*)&(laszip_dll->header.offset_to_point_data)); } catch(...)
    {
        sprintf(laszip_dll->error, "reading header.offset_to_point_data");
        return 1;
    }
    try { laszip_dll->streamin->get32bitsLE((U8*)&(laszip_dll->header.number_of_variable_length_records)); } catch(...)
    {
        sprintf(laszip_dll->error, "reading header.number_of_variable_length_records");
        return 1;
    }
    try { laszip_dll->streamin->getBytes((U8*)&(laszip_dll->header.point_data_format), 1); } catch(...)
    {
        sprintf(laszip_dll->error, "reading header.point_data_format");
        return 1;
    }
    try { laszip_dll->streamin->get16bitsLE((U8*)&(laszip_dll->header.point_data_record_length)); } catch(...)
    {
        sprintf(laszip_dll->error, "reading header.point_data_record_length");
        return 1;
    }
    try { laszip_dll->streamin->get32bitsLE((U8*)&(laszip_dll->header.number_of_point_records)); } catch(...)
    {
        sprintf(laszip_dll->error, "reading header.number_of_point_records");
        return 1;
    }
    for (i = 0; i < 5; i++)
    {
        try { laszip_dll->streamin->get32bitsLE((U8*)&(laszip_dll->header.number_of_points_by_return[i])); } catch(...)
        {
            sprintf(laszip_dll->error, "reading header.number_of_points_by_return %d", i);
            return 1;
        }
    }
    try { laszip_dll->streamin->get64bitsLE((U8*)&(laszip_dll->header.x_scale_factor)); } catch(...)
    {
        sprintf(laszip_dll->error, "reading header.x_scale_factor");
        return 1;
    }
    try { laszip_dll->streamin->get64bitsLE((U8*)&(laszip_dll->header.y_scale_factor)); } catch(...)
    {
        sprintf(laszip_dll->error, "reading header.y_scale_factor");
        return 1;
    }
    try { laszip_dll->streamin->get64bitsLE((U8*)&(laszip_dll->header.z_scale_factor)); } catch(...)
    {
        sprintf(laszip_dll->error, "reading header.z_scale_factor");
        return 1;
    }
    try { laszip_dll->streamin->get64bitsLE((U8*)&(laszip_dll->header.x_offset)); } catch(...)
    {
        sprintf(laszip_dll->error, "reading header.x_offset");
        return 1;
    }
    try { laszip_dll->streamin->get64bitsLE((U8*)&(laszip_dll->header.y_offset)); } catch(...)
    {
        sprintf(laszip_dll->error, "reading header.y_offset");
        return 1;
    }
    try { laszip_dll->streamin->get64bitsLE((U8*)&(laszip_dll->header.z_offset)); } catch(...)
    {
        sprintf(laszip_dll->error, "reading header.z_offset");
        return 1;
    }
    try { laszip_dll->streamin->get64bitsLE((U8*)&(laszip_dll->header.max_x)); } catch(...)
    {
        sprintf(laszip_dll->error, "reading header.max_x");
        return 1;
    }
    try { laszip_dll->streamin->get64bitsLE((U8*)&(laszip_dll->header.min_x)); } catch(...)
    {
        sprintf(laszip_dll->error, "reading header.min_x");
        return 1;
    }
    try { laszip_dll->streamin->get64bitsLE((U8*)&(laszip_dll->header.max_y)); } catch(...)
    {
        sprintf(laszip_dll->error, "reading header.max_y");
        return 1;
    }
    try { laszip_dll->streamin->get64bitsLE((U8*)&(laszip_dll->header.min_y)); } catch(...)
    {
        sprintf(laszip_dll->error, "reading header.min_y");
        return 1;
    }
    try { laszip_dll->streamin->get64bitsLE((U8*)&(laszip_dll->header.max_z)); } catch(...)
    {
        sprintf(laszip_dll->error, "reading header.max_z");
        return 1;
    }
    try { laszip_dll->streamin->get64bitsLE((U8*)&(laszip_dll->header.min_z)); } catch(...)
    {
        sprintf(laszip_dll->error, "reading header.min_z");
        return 1;
    }

    // special handling for LAS 1.3
    if ((laszip_dll->header.version_major == 1) && (laszip_dll->header.version_minor >= 3))
    {
        if (laszip_dll->header.header_size < 235)
        {
            sprintf(laszip_dll->error, "for LAS 1.%d header_size should at least be 235 but it is only %d", laszip_dll->header.version_minor, laszip_dll->header.header_size);
            return 1;
        }
        else
        {
            try { laszip_dll->streamin->get64bitsLE((U8*)&(laszip_dll->header.start_of_waveform_data_packet_record)); } catch(...)
            {
                sprintf(laszip_dll->error, "reading header.start_of_waveform_data_packet_record");
                return 1;
            }
            laszip_dll->header.user_data_in_header_size = laszip_dll->header.header_size - 235;
        }
    }
    else
    {
        laszip_dll->header.user_data_in_header_size = laszip_dll->header.header_size - 227;
    }

    // special handling for LAS 1.4
    if ((laszip_dll->header.version_major == 1) && (laszip_dll->header.version_minor >= 4))
    {
        if (laszip_dll->header.header_size < 375)
        {
            sprintf(laszip_dll->error, "for LAS 1.%d header_size should at least be 375 but it is only %d", laszip_dll->header.version_minor, laszip_dll->header.header_size);
            return 1;
        }
        else
        {
            try { laszip_dll->streamin->get64bitsLE((U8*)&(laszip_dll->header.start_of_first_extended_variable_length_record)); } catch(...)
            {
                sprintf(laszip_dll->error, "reading header.start_of_first_extended_variable_length_record");
                return 1;
            }
            try { laszip_dll->streamin->get32bitsLE((U8*)&(laszip_dll->header.number_of_extended_variable_length_records)); } catch(...)
            {
                sprintf(laszip_dll->error, "reading header.number_of_extended_variable_length_records");
                return 1;
            }
            try { laszip_dll->streamin->get64bitsLE((U8*)&(laszip_dll->header.extended_number_of_point_records)); } catch(...)
            {
                sprintf(laszip_dll->error, "reading header.extended_number_of_point_records");
                return 1;
            }
            for (i = 0; i < 15; i++)
            {
                try { laszip_dll->streamin->get64bitsLE((U8*)&(laszip_dll->header.extended_number_of_points_by_return[i])); } catch(...)
                {
                    sprintf(laszip_dll->error, "reading header.extended_number_of_points_by_return[%d]", i);
                    return 1;
                }
            }
            laszip_dll->header.user_data_in_header_size = laszip_dll->header.header_size - 375;
        }
    }

    // load any number of user-defined bytes that might have been added to the header
    if (laszip_dll->header.user_data_in_header_size)
    {
        if (laszip_dll->header.user_data_in_header)
        {
            delete [] laszip_dll->header.user_data_in_header;
        }
        laszip_dll->header.user_data_in_header = new U8[laszip_dll->header.user_data_in_header_size];

        try { laszip_dll->streamin->getBytes((U8*)laszip_dll->header.user_data_in_header, laszip_dll->header.user_data_in_header_size); } catch(...)
        {
            sprintf(laszip_dll->error, "reading %u bytes of data into header.user_data_in_header", laszip_dll->header.user_data_in_header_size);
            return 1;
        }
    }

    // read variable length records into the header

    U32 vlrs_size = 0;
    LASzip* laszip = 0;

    if (laszip_dll->header.number_of_variable_length_records)
    {
        U32 i;

        laszip_dll->header.vlrs = (laszip_vlr*)malloc(sizeof(laszip_vlr)*laszip_dll->header.number_of_variable_length_records);

        for (i = 0; i < laszip_dll->header.number_of_variable_length_records; i++)
        {
            // make sure there are enough bytes left to read a variable length record before the point block starts

            if (((int)laszip_dll->header.offset_to_point_data - vlrs_size - laszip_dll->header.header_size) < 54)
            {
                sprintf(laszip_dll->warning, "only %d bytes until point block after reading %d of %d vlrs. skipping remaining vlrs ...", (int)laszip_dll->header.offset_to_point_data - vlrs_size - laszip_dll->header.header_size, i, laszip_dll->header.number_of_variable_length_records);
                laszip_dll->header.number_of_variable_length_records = i;
                break;
            }

            // read variable length records variable after variable (to avoid alignment issues)

            try { laszip_dll->streamin->get16bitsLE((U8*)&(laszip_dll->header.vlrs[i].reserved)); } catch(...)
            {
                sprintf(laszip_dll->error, "reading header.vlrs[%u].reserved", i);
                return 1;
            }

            try { laszip_dll->streamin->getBytes((U8*)laszip_dll->header.vlrs[i].user_id, 16); } catch(...)
            {
                sprintf(laszip_dll->error, "reading header.vlrs[%u].user_id", i);
                return 1;
            }
            try { laszip_dll->streamin->get16bitsLE((U8*)&(laszip_dll->header.vlrs[i].record_id)); } catch(...)
            {
                sprintf(laszip_dll->error, "reading header.vlrs[%u].record_id", i);
                return 1;
            }
            try { laszip_dll->streamin->get16bitsLE((U8*)&(laszip_dll->header.vlrs[i].record_length_after_header)); } catch(...)
            {
                sprintf(laszip_dll->error, "reading header.vlrs[%u].record_length_after_header", i);
                return 1;
            }
            try { laszip_dll->streamin->getBytes((U8*)laszip_dll->header.vlrs[i].description, 32); } catch(...)
            {
                sprintf(laszip_dll->error, "reading header.vlrs[%u].description", i);
                return 1;
            }

            // keep track on the number of bytes we have read so far

            vlrs_size += 54;

            // check variable length record contents

            if (laszip_dll->header.vlrs[i].reserved != 0xAABB)
            {
                sprintf(laszip_dll->warning,"wrong header.vlrs[%d].reserved: %d != 0xAABB", i, laszip_dll->header.vlrs[i].reserved);
            }

            // make sure there are enough bytes left to read the data of the variable length record before the point block starts

            if (((int)laszip_dll->header.offset_to_point_data - vlrs_size - laszip_dll->header.header_size) < laszip_dll->header.vlrs[i].record_length_after_header)
            {
                sprintf(laszip_dll->warning, "only %d bytes until point block when trying to read %d bytes into header.vlrs[%d].data", (int)laszip_dll->header.offset_to_point_data - vlrs_size - laszip_dll->header.header_size, laszip_dll->header.vlrs[i].record_length_after_header, i);
                laszip_dll->header.vlrs[i].record_length_after_header = (int)laszip_dll->header.offset_to_point_data - vlrs_size - laszip_dll->header.header_size;
            }

            // load data following the header of the variable length record

            if (laszip_dll->header.vlrs[i].record_length_after_header)
            {
                if (strcmp(laszip_dll->header.vlrs[i].user_id, "laszip encoded") == 0)
                {
                    if (laszip)
                    {
                        delete laszip;
                    }

                    laszip = new LASzip();

                    if (laszip == 0)
                    {
                        sprintf(laszip_dll->error, "could not alloc LASzip");
                        return 1;
                    }

                    // read the LASzip VLR payload

                    //     U16  compressor                2 bytes
                    //     U32  coder                     2 bytes
                    //     U8   version_major             1 byte
                    //     U8   version_minor             1 byte
                    //     U16  version_revision          2 bytes
                    //     U32  options                   4 bytes
                    //     I32  chunk_size                4 bytes
                    //     I64  number_of_special_evlrs   8 bytes
                    //     I64  offset_to_special_evlrs   8 bytes
                    //     U16  num_items                 2 bytes
                    //        U16 type                2 bytes * num_items
                    //        U16 size                2 bytes * num_items
                    //        U16 version             2 bytes * num_items
                    // which totals 34+6*num_items

                    try { laszip_dll->streamin->get16bitsLE((U8*)&(laszip->compressor)); } catch(...)
                    {
                        sprintf(laszip_dll->error, "reading compressor %d", (I32)laszip->compressor);
                        return 1;
                    }
                    try { laszip_dll->streamin->get16bitsLE((U8*)&(laszip->coder)); } catch(...)
                    {
                        sprintf(laszip_dll->error, "reading coder %d", (I32)laszip->coder);
                        return 1;
                    }
                    try { laszip_dll->streamin->getBytes((U8*)&(laszip->version_major), 1); } catch(...)
                    {
                        sprintf(laszip_dll->error, "reading version_major %d", (I32)laszip->version_major);
                        return 1;
                    }
                    try { laszip_dll->streamin->getBytes((U8*)&(laszip->version_minor), 1); } catch(...)
                    {
                        sprintf(laszip_dll->error, "reading version_minor %d", (I32)laszip->version_minor);
                        return 1;
                    }
                    try { laszip_dll->streamin->get16bitsLE((U8*)&(laszip->version_revision)); } catch(...)
                    {
                        sprintf(laszip_dll->error, "reading version_revision %d", (I32)laszip->version_revision);
                        return 1;
                    }
                    try { laszip_dll->streamin->get32bitsLE((U8*)&(laszip->options)); } catch(...)
                    {
                        sprintf(laszip_dll->error, "reading options %u", laszip->options);
                        return 1;
                    }
                    try { laszip_dll->streamin->get32bitsLE((U8*)&(laszip->chunk_size)); } catch(...)
                    {
                        sprintf(laszip_dll->error, "reading chunk_size %u", laszip->chunk_size);
                        return 1;
                    }
                    try { laszip_dll->streamin->get64bitsLE((U8*)&(laszip->number_of_special_evlrs)); } catch(...)
                    {
                        sprintf(laszip_dll->error, "reading number_of_special_evlrs %d", (I32)laszip->number_of_special_evlrs);
                        return 1;
                    }
                    try { laszip_dll->streamin->get64bitsLE((U8*)&(laszip->offset_to_special_evlrs)); } catch(...)
                    {
                        sprintf(laszip_dll->error, "reading offset_to_special_evlrs %d", (I32)laszip->offset_to_special_evlrs);
                        return 1;
                    }
                    try { laszip_dll->streamin->get16bitsLE((U8*)&(laszip->num_items)); } catch(...)
                    {
                        sprintf(laszip_dll->error, "reading num_items %d", (I32)laszip->num_items);
                        return 1;
                    }
                    laszip->items = new LASitem[laszip->num_items];
                    U32 j;
                    for (j = 0; j < laszip->num_items; j++)
                    {
                        U16 type;
                        try { laszip_dll->streamin->get16bitsLE((U8*)&type); } catch(...)
                        {
                            sprintf(laszip_dll->error, "reading type of item %u", j);
                            return 1;
                        }
                        laszip->items[j].type = (LASitem::Type)type;
                        try { laszip_dll->streamin->get16bitsLE((U8*)&(laszip->items[j].size)); } catch(...)
                        {
                            sprintf(laszip_dll->error, "reading size of item %u", j);
                            return 1;
                        }
                        try { laszip_dll->streamin->get16bitsLE((U8*)&(laszip->items[j].version)); } catch(...)
                        {
                            sprintf(laszip_dll->error, "reading version of item %u", j);
                            return 1;
                        }
                    }
                }
                else
                {
                    laszip_dll->header.vlrs[i].data = new U8[laszip_dll->header.vlrs[i].record_length_after_header];

                    try { laszip_dll->streamin->getBytes(laszip_dll->header.vlrs[i].data, laszip_dll->header.vlrs[i].record_length_after_header); } catch(...)
                    {
                        sprintf(laszip_dll->error, "reading %d bytes of data into header.vlrs[%u].data", (I32)laszip_dll->header.vlrs[i].record_length_after_header, i);
                        return 1;
                    }
                }
            }
            else
            {
                laszip_dll->header.vlrs[i].data = 0;
            }

            // keep track on the number of bytes we have read so far

            vlrs_size += laszip_dll->header.vlrs[i].record_length_after_header;

            // special handling for LASzip VLR

            if (strcmp(laszip_dll->header.vlrs[i].user_id, "laszip encoded") == 0)
            {
                // we take our the VLR for LASzip away
                laszip_dll->header.offset_to_point_data -= (54+laszip_dll->header.vlrs[i].record_length_after_header);
                vlrs_size -= (54+laszip_dll->header.vlrs[i].record_length_after_header);
                i--;
                laszip_dll->header.number_of_variable_length_records--;
            }
        }
    }

    // load any number of user-defined bytes that might have been added after the header

    laszip_dll->header.user_data_after_header_size = (I32)laszip_dll->header.offset_to_point_data - vlrs_size - laszip_dll->header.header_size;
    if (laszip_dll->header.user_data_after_header_size)
    {
        if (laszip_dll->header.user_data_after_header)
        {
            delete [] laszip_dll->header.user_data_after_header;
        }

        laszip_dll->header.user_data_after_header = new U8[laszip_dll->header.user_data_after_header_size];

        try { laszip_dll->streamin->getBytes((U8*)laszip_dll->header.user_data_after_header, laszip_dll->header.user_data_after_header_size); } catch(...)
        {
            sprintf(laszip_dll->error, "reading %u bytes of data into header.user_data_after_header", laszip_dll->header.user_data_after_header_size);
            return 1;
        }
    }

    // remove extra bits in point data type

    if ((laszip_dll->header.point_data_format & 128) || (laszip_dll->header.point_data_format & 64))
    {
        if (!laszip)
        {
            sprintf(laszip_dll->error, "this file was compressed with an experimental version of LASzip. contact 'martin.isenburg@rapidlasso.com' for assistance");
            return 1;
        }
        laszip_dll->header.point_data_format &= 127;
    }

    // check if file is compressed

    if (laszip)
    {
        // yes. check the compressor state
        *is_compressed = 1;
        if (!laszip->check())
        {
            sprintf(laszip_dll->error, "%s upgrade to the latest release of LAStools (with LASzip) or contact 'martin.isenburg@rapidlasso.com' for assistance", laszip->get_error());
            return 1;
        }
    }
    else
    {
        // no. setup an un-compressed read
        *is_compressed = 0;
        laszip = new LASzip;
        if (laszip == 0)
        {
            sprintf(laszip_dll->error, "could not alloc LASzip");
            return 1;
        }
        if (!laszip->setup(laszip_dll->header.point_data_format, laszip_dll->header.point_data_record_length, LASZIP_COMPRESSOR_NONE))
        {
            sprintf(laszip_dll->error, "invalid combination of point_data_format %d and point_data_record_length %d", (I32)laszip_dll->header.point_data_format, (I32)laszip_dll->header.point_data_record_length);
            return 1;
        }
    }

    // create point's item pointers

    laszip_dll->point_items = new U8*[laszip->num_items];

    if (laszip_dll->point_items == 0)
    {
        sprintf(laszip_dll->error, "could not alloc point_items");
        return 1;
    }

    for (i = 0; i < laszip->num_items; i++)
    {
        switch (laszip->items[i].type)
        {
        case LASitem::POINT14:
        case LASitem::POINT10:
            laszip_dll->point_items[i] = (U8*)&(laszip_dll->point.X);
            break;
        case LASitem::GPSTIME11:
            laszip_dll->point_items[i] = (U8*)&(laszip_dll->point.gps_time);
            break;
        case LASitem::RGBNIR14:
        case LASitem::RGB12:
            laszip_dll->point_items[i] = (U8*)laszip_dll->point.rgb;
            break;
        case LASitem::WAVEPACKET13:
            laszip_dll->point_items[i] = (U8*)&(laszip_dll->point.wave_packet);
            break;
        case LASitem::BYTE:
            laszip_dll->point.num_extra_bytes = laszip->items[i].size;
            if (laszip_dll->point.extra_bytes) delete [] laszip_dll->point.extra_bytes;
            laszip_dll->point.extra_bytes = new U8[laszip_dll->point.num_extra_bytes];
            laszip_dll->point_items[i] = laszip_dll->point.extra_bytes;
            break;
        default:
            sprintf(laszip_dll->error, "unknown LASitem type %d", (I32)laszip->items[i].type);
            return 1;
        }
    }

    // create the point reader

    laszip_dll->reader = new LASreadPoint();
    if (laszip_dll->reader == 0)
    {
        sprintf(laszip_dll->error, "could not alloc LASreadPoint");
        return 1;
    }

    if (!laszip_dll->reader->setup(laszip->num_items, laszip->items, laszip))
    {
        sprintf(laszip_dll->error, "setup of LASreadPoint failed");
        return 1;
    }

    if (!laszip_dll->reader->init(laszip_dll->streamin))
    {
        sprintf(laszip_dll->error, "init of LASreadPoint failed");
        return 1;
    }

    delete laszip;

    // set the point number and point count

    laszip_dll->npoints = laszip_dll->header.number_of_point_records;
    laszip_dll->p_count = 0;

    return 0;

}

JNIEXPORT void JNICALL Java_fr_amap_amapvox_als_laz_LazExtraction_readAllPoints(JNIEnv *, jobject, jlong pointer)
{
    long pointerAddress = pointer;
    laszip_dll_struct* laszip_dll = (laszip_dll_struct*)pointerAddress;

    laszip_U32 count = 0;

    while (count < laszip_dll->header.number_of_point_records)
    {
        // read a point
        laszip_dll->reader->read(laszip_dll->point_items);
        laszip_dll->p_count++;
        std::cout << ((laszip_dll->point.X * laszip_dll->header.x_scale_factor) + laszip_dll->header.x_offset) << " "
             << ((laszip_dll->point.Y * laszip_dll->header.y_scale_factor) + laszip_dll->header.y_offset) << " "
             << ((laszip_dll->point.Z * laszip_dll->header.z_scale_factor) + laszip_dll->header.z_offset) << " "
             << "\n";
        count++;
    }

}

JNIEXPORT jobject JNICALL Java_fr_amap_amapvox_als_laz_LazExtraction_getNextPoint(JNIEnv *env, jobject obj, jlong pointer)
{
    long pointerAddress = pointer;
    laszip_dll_struct* laszip_dll = (laszip_dll_struct*)pointerAddress;

    if(laszip_dll->p_count < laszip_dll->header.number_of_point_records){

        laszip_dll->reader->read(laszip_dll->point_items);
        laszip_dll->p_count++;

        jobject lasPoint = env->NewObject(lasPointFormat0Class, lasPointFormat0Constructor);

        env->CallVoidMethod(lasPoint, lasPointFormat0Constructor,
                            laszip_dll->point.X,
                            laszip_dll->point.Y,
                            laszip_dll->point.Z,
                            laszip_dll->point.return_number, laszip_dll->point.number_of_returns_of_given_pulse,
                            laszip_dll->point.intensity, laszip_dll->point.classification, laszip_dll->point.gps_time);

        return lasPoint;
    }

    return NULL;
}

JNIEXPORT jobject JNICALL Java_fr_amap_amapvox_als_laz_LazExtraction_getBasicHeader(JNIEnv *env, jobject obj, jlong pointer)
{
    long pointerAddress = pointer;
    laszip_dll_struct* laszip_dll = (laszip_dll_struct*)pointerAddress;


    jclass lasHeaderClass = env->FindClass("fr/amap/amapvox/als/LasHeader");
    if (lasHeaderClass == NULL){
        return NULL;
    }

    jmethodID lasHeaderConstructor = env->GetMethodID(lasHeaderClass, "<init>", "(BBIDDDDDDDDDDDD)V");
    if (lasHeaderConstructor == NULL){
        return NULL;
    }

    jobject header = env->NewObject(lasHeaderClass, lasHeaderConstructor);
    if (header == NULL){
        return NULL;
    }

    env->CallVoidMethod(header,lasHeaderConstructor,
                        laszip_dll->header.version_major, laszip_dll->header.version_minor, laszip_dll->header.number_of_point_records,
                        laszip_dll->header.x_scale_factor, laszip_dll->header.y_scale_factor, laszip_dll->header.z_scale_factor,
                        laszip_dll->header.x_offset, laszip_dll->header.y_offset, laszip_dll->header.z_offset,
                        laszip_dll->header.min_x, laszip_dll->header.min_y, laszip_dll->header.min_z,
                        laszip_dll->header.max_x, laszip_dll->header.max_y, laszip_dll->header.max_z);

    return header;

}
