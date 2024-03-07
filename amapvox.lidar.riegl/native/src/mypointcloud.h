/*
 * myclass.h
 *
 *  Created on: 15 mars 2013
 *      Author: theveny
 */

#ifndef MYPOINTCLOUD_H_
#define MYPOINTCLOUD_H_

#include <jni.h>
#include <queue>
#include <riegl/pointcloud.hpp>
#include <iostream>

using namespace scanlib;
using namespace std;


namespace mpc
{

    enum ShotType {REFLECTANCE = 2, DEVIATION = 3, AMPLITUDE = 4};

    class mypointcloud: public scanlib::pointcloud
    {

    public:

        mypointcloud(JNIEnv *env);
        virtual ~mypointcloud();
        queue<jobject> *shots;
        void setExportReflectance(bool exportReflectance);
        void setExportDeviation(bool exportDeviation);
        void setExportAmplitude(bool exportAmplitude);
        void setExportTime(bool exportTime);
        void setEnv(JNIEnv *env);

    protected :
        void on_echo_transformed(echo_type echo);
        void on_shot();
        void on_shot_end();

    private :
        JNIEnv *env;
        bool exportReflectance;
        bool exportDeviation;
        bool exportAmplitude;
        bool exportTime;
    };
}



#endif /* MYPINTCLOUD_H_ */
