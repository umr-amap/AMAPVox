/*
 * myclass.cpp
 *
 *  Created on: 15 mars 2013
 *      Author: theveny
 */

#include "mypointcloud.h"

using namespace mpc;

mypointcloud::mypointcloud(JNIEnv *env) : pointcloud(false) {

    this->env = env;
    this->shots = new queue<jobject>();
    exportReflectance = false;
    exportDeviation = false;
    exportAmplitude = false;
}

void mypointcloud::setEnv(JNIEnv *env){
    this->env = env;
}

mypointcloud::~mypointcloud() {
    delete shots;
}

void mypointcloud::setExportReflectance(bool exportReflectance){
    this->exportReflectance = exportReflectance;
}

void mypointcloud::setExportDeviation(bool exportDeviation){
    this->exportDeviation = exportDeviation;
}

void mypointcloud::setExportAmplitude(bool exportAmplitude){
    this->exportAmplitude = exportAmplitude;
}

void mypointcloud::setExportTime(bool exportTime){
    this->exportTime = exportTime;
}

void mypointcloud::on_echo_transformed(echo_type echo){
	pointcloud::on_echo_transformed(echo);
}

void mypointcloud::on_shot() {
	pointcloud::on_shot();
}

void mypointcloud::on_shot_end() {

	pointcloud::on_shot_end();

    int nbEchos = 0;

    jdouble distancesArray[7];
    jfloat reflectanceArray[7];
    jfloat deviationArray[7];
    jfloat amplitudeArray[7];
    jdouble timeArray[7];

    for(pointcloud::target_count_type i = 0; i < target_count; ++i) {

            target t = targets[i];

            if(i < 7){ //handle more than 7 echoes case

                distancesArray[i] = t.echo_range;

                if(exportReflectance){
                    reflectanceArray[i] = t.reflectance;
                }

                if(exportDeviation){
                    deviationArray[i] = t.deviation;
                }

                if(exportAmplitude){
                    amplitudeArray[i] = t.amplitude;
                }

                if(exportTime){
                    timeArray[i] = t.time;
                }

                nbEchos++;
            }
    }

    jdoubleArray echos = env->NewDoubleArray(nbEchos);
    env->SetDoubleArrayRegion(echos, 0, nbEchos, distancesArray);

    jobject shotTemp/* = new jobject()*/;
    jclass shotClass = env->FindClass("org/amapvox/lidar/riegl/RxpShot");
    if(shotClass){

        jmethodID shotConstructor = env->GetMethodID(shotClass, "<init>", "(IDDDDDDD[D)V");
        shotTemp = env->NewObject(shotClass, shotConstructor, (jint)nbEchos, (jdouble)time,
                                   (jdouble)beam_origin[0], (jdouble)beam_origin[1], (jdouble)beam_origin[2],
                                    (jdouble)beam_direction[0], (jdouble)beam_direction[1], (jdouble)beam_direction[2], echos);



        env->DeleteLocalRef(echos);

        /**Handle echoes attributes**/

        jfloatArray reflectances = NULL;
        jfloatArray deviations = NULL;
        jfloatArray amplitudes = NULL;
        jdoubleArray times = NULL;

        if(exportReflectance){
            reflectances = env->NewFloatArray(nbEchos);
            env->SetFloatArrayRegion(reflectances, 0, nbEchos, reflectanceArray);
            jmethodID setReflectancesMethod = env->GetMethodID(shotClass, "setReflectances", "([F)V");
            env->CallVoidMethod(shotTemp, setReflectancesMethod, reflectances);
            env->DeleteLocalRef(reflectances);
        }

        if(exportDeviation){
            deviations = env->NewFloatArray(nbEchos);
            env->SetFloatArrayRegion(deviations, 0, nbEchos, deviationArray);
            jmethodID setDeviationsMethod = env->GetMethodID(shotClass, "setDeviations", "([F)V");
            env->CallVoidMethod(shotTemp, setDeviationsMethod, deviations);
            env->DeleteLocalRef(deviations);

        }

        if(exportAmplitude){
            amplitudes = env->NewFloatArray(nbEchos);
            env->SetFloatArrayRegion(amplitudes, 0, nbEchos, amplitudeArray);
            jmethodID setAmplitudesMethod = env->GetMethodID(shotClass, "setAmplitudes", "([F)V");
            env->CallVoidMethod(shotTemp, setAmplitudesMethod, amplitudes);
            env->DeleteLocalRef(amplitudes);
        }

        if(exportTime){
            times = env->NewDoubleArray(nbEchos);
            env->SetDoubleArrayRegion(times, 0, nbEchos, timeArray);
            jmethodID setTimesMethod = env->GetMethodID(shotClass, "setTimes", "([D)V");
            env->CallVoidMethod(shotTemp, setTimesMethod, times);
            env->DeleteLocalRef(times);
        }

        shots->push(env->NewGlobalRef(shotTemp));
    }else{
        std::cout << "Finding Shot class failed" << std::endl;
    }
}
