#ifndef RIVLIBLIBRARY
#define RIVLIBLIBRARY

#include <jni.h>
#include <jni_md.h>

#include "mypointcloud.h"

#include <riegl/scanlib.hpp>
#include <riegl/pointcloud.hpp>

#include <stdlib.h>
#include <stdio.h>
#include <cstring>
#include <iostream>
#include <fstream>

using namespace scanlib;
using namespace mpc;


#ifndef _Included_org_amapvox_lidar_riegl_RxpExtraction
#define _Included_org_amapvox_lidar_riegl_RxpExtraction
#ifdef __cplusplus

struct rxp_extraction {

    std::shared_ptr<scanlib::basic_rconnection> connection;
    decoder_rxpmarker* decoder;
    mypointcloud* pointcloud;

};

extern "C" {
#endif

    JNIEXPORT void JNICALL Java_org_amapvox_lidar_riegl_RxpExtraction_sayHello
        (JNIEnv *, jclass);

    JNIEXPORT jlong JNICALL Java_org_amapvox_lidar_riegl_RxpExtraction_createConnection
        (JNIEnv *, jclass);

    JNIEXPORT void JNICALL Java_org_amapvox_lidar_riegl_RxpExtraction_deleteConnection
        (JNIEnv *, jclass, jlong pointer);

    JNIEXPORT int JNICALL Java_org_amapvox_lidar_riegl_RxpExtraction_openConnection
    (JNIEnv *, jclass, jlong pointer, jstring file_name, jintArray shotTypes);

    JNIEXPORT void JNICALL Java_org_amapvox_lidar_riegl_RxpExtraction_closeConnection
    (JNIEnv *, jclass, jlong pointer);

    JNIEXPORT jobject JNICALL Java_org_amapvox_lidar_riegl_RxpExtraction_getNextShot
    (JNIEnv *, jclass, jlong pointer);

    JNIEXPORT jboolean JNICALL Java_org_amapvox_lidar_riegl_RxpExtraction_hasShot
    (JNIEnv *, jclass, jlong pointer);
    
    JNIEXPORT jlong JNICALL Java_org_amapvox_lidar_riegl_RxpExtraction_tellg
    (JNIEnv *, jclass, jlong pointer);

#ifdef __cplusplus
}
#endif
#endif

#endif // RIVLIBLIBRARY

