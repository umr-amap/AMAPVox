#include "rivliblibrary.h"



//morceau de code trouvé sur Internet qui permet de convertir les jstring contenant des accents
char* JNU_GetStringNativeChars(JNIEnv* env, jstring jstr) {

    jclass Class_java_lang_String = env->FindClass("java/lang/String");
    jmethodID MID_String_getBytes = env->GetMethodID(Class_java_lang_String, "getBytes", "()[B");

    jbyteArray bytes = 0;

    char* result = 0;

    if (env->EnsureLocalCapacity(2) < 0) {
        return 0;  // out of memory error
    }

    bytes = (jbyteArray) env->CallObjectMethod(jstr, MID_String_getBytes);

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

JNIEXPORT void JNICALL Java_org_amapvox_lidar_riegl_RxpExtraction_sayHello(JNIEnv *, jclass){

    std::cout << "Hello from C++ !!" << std::endl;
}

JNIEXPORT jlong JNICALL Java_org_amapvox_lidar_riegl_RxpExtraction_createConnection(JNIEnv *, jclass){

    rxp_extraction* extraction_dll = new rxp_extraction;

    extraction_dll->connection  = std::shared_ptr<basic_rconnection>();

    return (jlong)extraction_dll;
}

JNIEXPORT void JNICALL Java_org_amapvox_lidar_riegl_RxpExtraction_deleteConnection(JNIEnv *, jclass, jlong pointer){

    rxp_extraction *extraction_dll  = (rxp_extraction*)pointer;

    extraction_dll->connection.reset();

    delete extraction_dll->decoder;

    delete extraction_dll->pointcloud;

    delete extraction_dll;
}

JNIEXPORT int JNICALL Java_org_amapvox_lidar_riegl_RxpExtraction_openConnection(JNIEnv *env, jclass, jlong pointer, jstring file_name, jintArray shotTypes){

    try
    {
        rxp_extraction *extraction_dll  = (rxp_extraction*)pointer;

        const char* str1 = JNU_GetStringNativeChars(env, file_name);
        //std::cout << "Parsed file_name: " << str1 << std::endl;


        extraction_dll->connection = basic_rconnection::create(str1);

        extraction_dll->connection->open();

        extraction_dll->decoder = new decoder_rxpmarker(*extraction_dll->connection);

        const jsize typesArrayLength = env->GetArrayLength(shotTypes);

        extraction_dll->pointcloud = new mypointcloud(env);

        jint *body = env->GetIntArrayElements(shotTypes,0);

        for(jint i=0;i<typesArrayLength;i++){

            switch(body[i]){
                case 2:
                    extraction_dll->pointcloud->setExportReflectance(true);
                    break;
                case 3:
                    extraction_dll->pointcloud->setExportDeviation(true);
                    break;
                case 4:
                    extraction_dll->pointcloud->setExportAmplitude(true);
                    break;
                case 5:
                    extraction_dll->pointcloud->setExportTime(true);
                    break;
            }
        }


    }catch ( const std::exception &  ){
        return -1;
    }


    return 0;
}

JNIEXPORT void JNICALL Java_org_amapvox_lidar_riegl_RxpExtraction_closeConnection(JNIEnv *, jclass, jlong pointer){

    try
    {
        rxp_extraction *extraction_dll  = (rxp_extraction*)pointer;

        extraction_dll->connection->close();

    }catch ( const std::exception & e ){
        std::cout << "Cannot close rxp file: " << e.what() << std::endl;
    }

}

JNIEXPORT jboolean JNICALL Java_org_amapvox_lidar_riegl_RxpExtraction_hasShot(JNIEnv *, jclass, jlong pointer){

    rxp_extraction *extraction_dll  = (rxp_extraction*)pointer;

    return (jboolean)!extraction_dll->decoder->eoi();
}

JNIEXPORT jobject JNICALL Java_org_amapvox_lidar_riegl_RxpExtraction_getNextShot(JNIEnv *env, jclass, jlong pointer){

    rxp_extraction *extraction_dll  = (rxp_extraction*)pointer;

    if(extraction_dll->pointcloud->shots->empty()) {
        buffer buf;
        while(!extraction_dll->decoder->eoi() && extraction_dll->pointcloud->shots->empty()){
            extraction_dll->decoder->get(buf);
            extraction_dll->pointcloud->dispatch(buf.begin(), buf.end());
        }

    }

    if(!extraction_dll->pointcloud->shots->empty()){

        jobject shotPtr = extraction_dll->pointcloud->shots->front();
        //shotTemp = *shotPtr;
        extraction_dll->pointcloud->shots->pop();
        //delete shotPtr;
        //env->DeleteGlobalRef(shotPtr);
        jobject tmp = env->NewLocalRef(shotPtr);
        env->DeleteGlobalRef(shotPtr);
        return tmp;
    }


    return NULL;
}

JNIEXPORT jlong JNICALL Java_org_amapvox_lidar_riegl_RxpExtraction_tellg(JNIEnv *, jclass, jlong pointer) {

    rxp_extraction *extraction_dll  = (rxp_extraction*)pointer;
    return extraction_dll->connection->tellg();
}
