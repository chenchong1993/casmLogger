#include <jni.h>
//#include <string>
//#include <iostream>
#include "rtdlib.h"
//导入日志头文件
#include <android/log.h>
//修改日志tag中的值
#define LOG_TAG "logfromc"
//日志显示的等级
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)

char* jstringToChar(JNIEnv* env, jstring jstr);
jstring charTojstring(JNIEnv* env, const char* pat);

jstring charTojstring(JNIEnv* env, const char* pat) {
    //定义java String类 strClass
    jclass strClass = (env)->FindClass("Ljava/lang/String;");
    //获取String(byte[],String)的构造器,用于将本地byte[]数组转换为一个新String
    jmethodID ctorID = (env)->GetMethodID(strClass, "<init>", "([BLjava/lang/String;)V");
    //建立byte数组
    jbyteArray bytes = (env)->NewByteArray(strlen(pat));
    //将char* 转换为byte数组
    (env)->SetByteArrayRegion(bytes, 0, strlen(pat), (jbyte*) pat);
    // 设置String, 保存语言类型,用于byte数组转换至String时的参数
    jstring encoding = (env)->NewStringUTF("GB2312");
    //将byte数组转换为java String,并输出
    return (jstring) (env)->NewObject(strClass, ctorID, bytes, encoding);
}

char* jstringToChar(JNIEnv* env, jstring jstr) {
    char* rtn = NULL;
    jclass clsstring = env->FindClass("java/lang/String");
    jstring strencode = env->NewStringUTF("GB2312");
    jmethodID mid = env->GetMethodID(clsstring, "getBytes", "(Ljava/lang/String;)[B");
    jbyteArray barr = (jbyteArray) env->CallObjectMethod(jstr, mid, strencode);
    jsize alen = env->GetArrayLength(barr);
    jbyte* ba = env->GetByteArrayElements(barr, JNI_FALSE);
    if (alen > 0) {
        rtn = (char*) malloc(alen + 1);
        memcpy(rtn, ba, alen);
        rtn[alen] = 0;
    }
    env->ReleaseByteArrayElements(barr, ba, 0);
    return rtn;
}

//extern "C"
//JNIEXPORT jstring JNICALL
//Java_com_lass_liuyi_rtklibandroid_MainActivity_passingPathJNI(JNIEnv *env, jobject instance,
//                                                              jobjectArray ip, jobjectArray port) {
//    // TODO
//    std::string IP[8],PORT[8];
//    int PathNum = 0;
//    memset(&IP,0x00,sizeof(IP));memset(&PORT,0x00,sizeof(PORT));
//    //数组的长度
//    jsize iplen = (*env).GetArrayLength(ip);
//    jsize portlen = (*env).GetArrayLength(port);
//    //先获取对象，再根据长度将对象分割
//    for (int i = 0,j = 0; (i < iplen)&&(j <portlen); i++,j++) {
//        jstring jstrsip = (jstring)(*env).GetObjectArrayElement(ip,i);
//        jstring jstrsport = (jstring)(*env).GetObjectArrayElement(port,j);
//        if(jstrsip!=NULL&&jstrsport!=NULL){
//            char* chardataIP = jstringToChar(env, jstrsip);
//            char* chardataPORT = jstringToChar(env, jstrsport);
//            IP[i] = chardataIP;
//            PORT[j] = chardataPORT;
//            PathNum++;
//        }
//    }
//
//    int success = 0;
//    std::string type = "Null";
//    if(PathNum > 0) {
//        success = rtdrun(IP,PORT);                          //rtd接口
//    }
//    if(success) {type = "Connecting";}
//    else {type = "Faild";}
//
//    return env->NewStringUTF(type.c_str());
//}

extern result_t sol_result;
double StationPos[3] = {0};
extern "C"
JNIEXPORT jint JNICALL
Java_com_kubolab_gnss_casmLogger_Logger2Fragment_resultFromJNI(JNIEnv *env, jobject instance,
                                                             jdoubleArray result_, jdoubleArray ep_,
                                                             jdoubleArray gpst_,
                                                             jdoubleArray velocity_,
                                                             jdoubleArray clk_,
                                                             jintArray satprn_,
                                                             jdoubleArray dop_   ) {
    jdouble *result = env->GetDoubleArrayElements(result_, NULL);
    jdouble *ep = env->GetDoubleArrayElements(ep_, NULL);
    jdouble *gpst = env->GetDoubleArrayElements(gpst_, NULL);
    jdouble *velocity = env->GetDoubleArrayElements(velocity_, NULL);
    jdouble *clk = env->GetDoubleArrayElements(clk_, NULL);
    jint    *satprn = env->GetIntArrayElements(satprn_, NULL);
    jdouble *dop = env->GetDoubleArrayElements(dop_, NULL);
    // TODO

    for (int i = 0; i < 3; i++) {
        result[i] = sol_result.result_rr[i];
    }
    for (int i = 0; i < 4; i++) {
        dop[i] = sol_result.dop[i];
    }
    for (int i = 0; i < 6; i++) {
        satprn[i] = sol_result.satnum[i];
    }
    ep[0] = sol_result.gpsWeek;
    ep[1] = sol_result.gpsSec;
//    double StationPos[3] = {0};
    memset(StationPos,0x00, sizeof(StationPos));
    ecef2pos(result,StationPos);
    result[3] = StationPos[0]*R2D;
    result[4] = StationPos[1]*R2D;
    result[5] = StationPos[2];


    env->ReleaseDoubleArrayElements(result_, result, 0);
    env->ReleaseDoubleArrayElements(ep_, ep, 0);
    env->ReleaseDoubleArrayElements(gpst_, gpst, 0);
    env->ReleaseDoubleArrayElements(velocity_, velocity, 0);
    env->ReleaseDoubleArrayElements(clk_, clk, 0);
    env->ReleaseIntArrayElements(satprn_, satprn, 0);
    env->ReleaseDoubleArrayElements(dop_, dop, 0);
    return sol_result.result_stat;
}


extern "C"
JNIEXPORT jstring JNICALL
Java_com_kubolab_gnss_casmLogger_MainActivity_passingPathJNI(JNIEnv *env, jobject instance,
                                                              jobjectArray ip, jobjectArray port,
                                                              jstring resultfilename_) {
    const char *resultfilename = env->GetStringUTFChars(resultfilename_, 0);

    // TODO
    std::string IP[8],PORT[8],RESULTFILENAME;
    int PathNum = 0;
    memset(&IP,0x00,sizeof(IP));memset(&PORT,0x00,sizeof(PORT));
    //数组的长度
    jsize iplen = (*env).GetArrayLength(ip);
    jsize portlen = (*env).GetArrayLength(port);
    //先获取对象，再根据长度将对象分割
    for (int i = 0,j = 0; (i < iplen)&&(j <portlen); i++,j++) {
        jstring jstrsip = (jstring)(*env).GetObjectArrayElement(ip,i);
        jstring jstrsport = (jstring)(*env).GetObjectArrayElement(port,j);
        if(jstrsip!=NULL&&jstrsport!=NULL){
            char* chardataIP = jstringToChar(env, jstrsip);
            char* chardataPORT = jstringToChar(env, jstrsport);
            IP[i] = chardataIP;
            PORT[j] = chardataPORT;
            PathNum++;
        }
    }
    RESULTFILENAME = resultfilename;
    int success = 0;
    std::string type = "Null";
    if(PathNum > 0) {
        success = rtdrun(IP,PORT,RESULTFILENAME);                          //rtd接口
    }
    if(success) {type = "Connecting";}
    else {type = "Faild";}

    env->ReleaseStringUTFChars(resultfilename_, resultfilename);

    return env->NewStringUTF(type.c_str());
    //return env->NewStringUTF(returnValue);
}


obsdata_t ObsData;
extern "C"
JNIEXPORT jint JNICALL
Java_com_kubolab_gnss_casmLogger_MainActivity_passingobsDataJNI(JNIEnv *env, jobject instance,
                                                                 jintArray gpsweek_,
                                                                 jdoubleArray gpssecond_,
                                                                 jintArray bdsweek_,
                                                                 jdoubleArray bdssecond_,
                                                                 jintArray gloweek_,
                                                                 jdoubleArray glosecond_,
                                                                 jintArray galweek_,
                                                                 jdoubleArray galsecond_,
                                                                 jint gpssvnum, jint bdssvnum,
                                                                 jint glosvnum, jint galsvnum,
                                                                 jintArray gpsprn_,
                                                                 jintArray bdsprn_,
                                                                 jintArray gloprn_,
                                                                 jintArray galprn_,
                                                                 jdoubleArray gpsC1_,
                                                                 jdoubleArray gpsL1_,
                                                                 jdoubleArray gpsD1_,
                                                                 jdoubleArray gpsC2_,
                                                                 jdoubleArray gpsL2_,
                                                                 jdoubleArray gpsD2_,
                                                                 jdoubleArray gpsC3_,
                                                                 jdoubleArray gpsL3_,
                                                                 jdoubleArray gpsD3_,
                                                                 jdoubleArray bdsC1_,
                                                                 jdoubleArray bdsL1_,
                                                                 jdoubleArray bdsD1_,
                                                                 jdoubleArray bdsC2_,
                                                                 jdoubleArray bdsL2_,
                                                                 jdoubleArray bdsD2_,
                                                                 jdoubleArray bdsC3_,
                                                                 jdoubleArray bdsL3_,
                                                                 jdoubleArray bdsD3_,
                                                                 jdoubleArray gloC1_,
                                                                 jdoubleArray gloL1_,
                                                                 jdoubleArray gloD1_,
                                                                 jdoubleArray gloC2_,
                                                                 jdoubleArray gloL2_,
                                                                 jdoubleArray gloD2_,
                                                                 jdoubleArray gloC3_,
                                                                 jdoubleArray gloL3_,
                                                                 jdoubleArray gloD3_,
                                                                 jdoubleArray galC1_,
                                                                 jdoubleArray galL1_,
                                                                 jdoubleArray galD1_,
                                                                 jdoubleArray galC2_,
                                                                 jdoubleArray galL2_,
                                                                 jdoubleArray galD2_,
                                                                 jdoubleArray galC3_,
                                                                 jdoubleArray galL3_,
                                                                 jdoubleArray galD3_) {
    jint *gpsweek = env->GetIntArrayElements(gpsweek_, NULL);
    jdouble *gpssecond = env->GetDoubleArrayElements(gpssecond_, NULL);
    jint *bdsweek = env->GetIntArrayElements(bdsweek_, NULL);
    jdouble *bdssecond = env->GetDoubleArrayElements(bdssecond_, NULL);
    jint *gloweek = env->GetIntArrayElements(gloweek_, NULL);
    jdouble *glosecond = env->GetDoubleArrayElements(glosecond_, NULL);
    jint *galweek = env->GetIntArrayElements(galweek_, NULL);
    jdouble *galsecond = env->GetDoubleArrayElements(galsecond_, NULL);
    jint *gpsprn = env->GetIntArrayElements(gpsprn_, NULL);
    jint *bdsprn = env->GetIntArrayElements(bdsprn_, NULL);
    jint *gloprn = env->GetIntArrayElements(gloprn_, NULL);
    jint *galprn = env->GetIntArrayElements(galprn_, NULL);
    jdouble *gpsC1 = env->GetDoubleArrayElements(gpsC1_, NULL);
    jdouble *gpsL1 = env->GetDoubleArrayElements(gpsL1_, NULL);
    jdouble *gpsD1 = env->GetDoubleArrayElements(gpsD1_, NULL);
    jdouble *gpsC2 = env->GetDoubleArrayElements(gpsC2_, NULL);
    jdouble *gpsL2 = env->GetDoubleArrayElements(gpsL2_, NULL);
    jdouble *gpsD2 = env->GetDoubleArrayElements(gpsD2_, NULL);
    jdouble *gpsC3 = env->GetDoubleArrayElements(gpsC3_, NULL);
    jdouble *gpsL3 = env->GetDoubleArrayElements(gpsL3_, NULL);
    jdouble *gpsD3 = env->GetDoubleArrayElements(gpsD3_, NULL);
    jdouble *bdsC1 = env->GetDoubleArrayElements(bdsC1_, NULL);
    jdouble *bdsL1 = env->GetDoubleArrayElements(bdsL1_, NULL);
    jdouble *bdsD1 = env->GetDoubleArrayElements(bdsD1_, NULL);
    jdouble *bdsC2 = env->GetDoubleArrayElements(bdsC2_, NULL);
    jdouble *bdsL2 = env->GetDoubleArrayElements(bdsL2_, NULL);
    jdouble *bdsD2 = env->GetDoubleArrayElements(bdsD2_, NULL);
    jdouble *bdsC3 = env->GetDoubleArrayElements(bdsC3_, NULL);
    jdouble *bdsL3 = env->GetDoubleArrayElements(bdsL3_, NULL);
    jdouble *bdsD3 = env->GetDoubleArrayElements(bdsD3_, NULL);
    jdouble *gloC1 = env->GetDoubleArrayElements(gloC1_, NULL);
    jdouble *gloL1 = env->GetDoubleArrayElements(gloL1_, NULL);
    jdouble *gloD1 = env->GetDoubleArrayElements(gloD1_, NULL);
    jdouble *gloC2 = env->GetDoubleArrayElements(gloC2_, NULL);
    jdouble *gloL2 = env->GetDoubleArrayElements(gloL2_, NULL);
    jdouble *gloD2 = env->GetDoubleArrayElements(gloD2_, NULL);
    jdouble *gloC3 = env->GetDoubleArrayElements(gloC3_, NULL);
    jdouble *gloL3 = env->GetDoubleArrayElements(gloL3_, NULL);
    jdouble *gloD3 = env->GetDoubleArrayElements(gloD3_, NULL);
    jdouble *galC1 = env->GetDoubleArrayElements(galC1_, NULL);
    jdouble *galL1 = env->GetDoubleArrayElements(galL1_, NULL);
    jdouble *galD1 = env->GetDoubleArrayElements(galD1_, NULL);
    jdouble *galC2 = env->GetDoubleArrayElements(galC2_, NULL);
    jdouble *galL2 = env->GetDoubleArrayElements(galL2_, NULL);
    jdouble *galD2 = env->GetDoubleArrayElements(galD2_, NULL);
    jdouble *galC3 = env->GetDoubleArrayElements(galC3_, NULL);
    jdouble *galL3 = env->GetDoubleArrayElements(galL3_, NULL);
    jdouble *galD3 = env->GetDoubleArrayElements(galD3_, NULL);

    // TODO

    ObsData.gpsSvNum = gpssvnum;
    ObsData.bdsSvNum = bdssvnum;
    ObsData.gloSvNum = glosvnum;
    ObsData.galSvNum = galsvnum;
    for(int i = 0;i<ObsData.gpsSvNum;i++)
    {
        ObsData.GPSTweek[i] = gpsweek[i];
        ObsData.GPSTsecond[i] = gpssecond[i];
        ObsData.Gpsprn_t[i] = gpsprn[i];
        ObsData.GpsC1_t[i] = gpsC1[i];
        ObsData.GpsC2_t[i] = gpsC2[i];
        ObsData.GpsC3_t[i] = gpsC3[i];
        ObsData.GpsL1_t[i] = gpsL1[i];
        ObsData.GpsL2_t[i] = gpsL2[i];
        ObsData.GpsL3_t[i] = gpsL3[i];
        ObsData.GpsD1_t[i] = gpsD1[i];
        ObsData.GpsD3_t[i] = gpsD3[i];
    }
    for(int i = 0;i<ObsData.bdsSvNum;i++)
    {
        ObsData.BDSTweek[i] = bdsweek[i];
        ObsData.BDSTsecond[i] = bdssecond[i];
        ObsData.Bdsprn_t[i] = bdsprn[i];
        ObsData.BdsC1_t[i] = bdsC1[i];
        ObsData.BdsC2_t[i] = bdsC2[i];
        ObsData.BdsC3_t[i] = bdsC3[i];
        ObsData.BdsL1_t[i] = bdsL1[i];
        ObsData.BdsL2_t[i] = bdsL2[i];
        ObsData.BdsL3_t[i] = bdsL3[i];
        ObsData.BdsD1_t[i] = bdsD1[i];
        ObsData.BdsD2_t[i] = bdsD2[i];
        ObsData.BdsD3_t[i] = bdsD3[i];

    }
    for(int i = 0;i<ObsData.gloSvNum;i++) {

        ObsData.Gloprn_t[i] = gloprn[i];
        ObsData.GLOTweek[i] = gloweek[i];
        ObsData.GLOTsecond[i] = glosecond[i];
        ObsData.GloC1_t[i] = gloC1[i];
        ObsData.GloC2_t[i] = gloC2[i];
        ObsData.GloC3_t[i] = gloC3[i];
        ObsData.GloL1_t[i] = gloL1[i];
        ObsData.GloL2_t[i] = gloL2[i];
        ObsData.GloL3_t[i] = gloL3[i];
        ObsData.GloD1_t[i] = gloD1[i];
        ObsData.GloD2_t[i] = gloD2[i];
        ObsData.GloD3_t[i] = gloD3[i];
    }
    for(int i = 0;i<ObsData.galSvNum;i++) {
        ObsData.Galprn_t[i] = galprn[i];
        ObsData.GALTweek[i] = galweek[i];
        ObsData.GALTsecond[i] = galsecond[i];
        ObsData.GalC1_t[i] = galC1[i];
        ObsData.GalC2_t[i] = galC2[i];
        ObsData.GalC3_t[i] = galC3[i];
        ObsData.GalL1_t[i] = galL1[i];
        ObsData.GalL2_t[i] = galL2[i];
        ObsData.GalL3_t[i] = galL3[i];
        ObsData.GalD1_t[i] = galD1[i];
        ObsData.GalD2_t[i] = galD2[i];
        ObsData.GalD3_t[i] = galD3[i];

    }

    env->ReleaseIntArrayElements(gpsweek_, gpsweek, 0);
    env->ReleaseDoubleArrayElements(gpssecond_, gpssecond, 0);
    env->ReleaseIntArrayElements(bdsweek_, bdsweek, 0);
    env->ReleaseDoubleArrayElements(bdssecond_, bdssecond, 0);
    env->ReleaseIntArrayElements(gloweek_, gloweek, 0);
    env->ReleaseDoubleArrayElements(glosecond_, glosecond, 0);
    env->ReleaseIntArrayElements(galweek_, galweek, 0);
    env->ReleaseDoubleArrayElements(galsecond_, galsecond, 0);
    env->ReleaseIntArrayElements(gpsprn_, gpsprn, 0);
    env->ReleaseIntArrayElements(bdsprn_, bdsprn, 0);
    env->ReleaseIntArrayElements(gloprn_, gloprn, 0);
    env->ReleaseIntArrayElements(galprn_, galprn, 0);
    env->ReleaseDoubleArrayElements(gpsC1_, gpsC1, 0);
    env->ReleaseDoubleArrayElements(gpsL1_, gpsL1, 0);
    env->ReleaseDoubleArrayElements(gpsD1_, gpsD1, 0);
    env->ReleaseDoubleArrayElements(gpsC2_, gpsC2, 0);
    env->ReleaseDoubleArrayElements(gpsL2_, gpsL2, 0);
    env->ReleaseDoubleArrayElements(gpsD2_, gpsD2, 0);
    env->ReleaseDoubleArrayElements(gpsC3_, gpsC3, 0);
    env->ReleaseDoubleArrayElements(gpsL3_, gpsL3, 0);
    env->ReleaseDoubleArrayElements(gpsD3_, gpsD3, 0);
    env->ReleaseDoubleArrayElements(bdsC1_, bdsC1, 0);
    env->ReleaseDoubleArrayElements(bdsL1_, bdsL1, 0);
    env->ReleaseDoubleArrayElements(bdsD1_, bdsD1, 0);
    env->ReleaseDoubleArrayElements(bdsC2_, bdsC2, 0);
    env->ReleaseDoubleArrayElements(bdsL2_, bdsL2, 0);
    env->ReleaseDoubleArrayElements(bdsD2_, bdsD2, 0);
    env->ReleaseDoubleArrayElements(bdsC3_, bdsC3, 0);
    env->ReleaseDoubleArrayElements(bdsL3_, bdsL3, 0);
    env->ReleaseDoubleArrayElements(bdsD3_, bdsD3, 0);
    env->ReleaseDoubleArrayElements(gloC1_, gloC1, 0);
    env->ReleaseDoubleArrayElements(gloL1_, gloL1, 0);
    env->ReleaseDoubleArrayElements(gloD1_, gloD1, 0);
    env->ReleaseDoubleArrayElements(gloC2_, gloC2, 0);
    env->ReleaseDoubleArrayElements(gloL2_, gloL2, 0);
    env->ReleaseDoubleArrayElements(gloD2_, gloD2, 0);
    env->ReleaseDoubleArrayElements(gloC3_, gloC3, 0);
    env->ReleaseDoubleArrayElements(gloL3_, gloL3, 0);
    env->ReleaseDoubleArrayElements(gloD3_, gloD3, 0);
    env->ReleaseDoubleArrayElements(galC1_, galC1, 0);
    env->ReleaseDoubleArrayElements(galL1_, galL1, 0);
    env->ReleaseDoubleArrayElements(galD1_, galD1, 0);
    env->ReleaseDoubleArrayElements(galC2_, galC2, 0);
    env->ReleaseDoubleArrayElements(galL2_, galL2, 0);
    env->ReleaseDoubleArrayElements(galD2_, galD2, 0);
    env->ReleaseDoubleArrayElements(galC3_, galC3, 0);
    env->ReleaseDoubleArrayElements(galL3_, galL3, 0);
    env->ReleaseDoubleArrayElements(galD3_, galD3, 0);
    return 1;
}

extern "C"
JNIEXPORT jint JNICALL
Java_com_kubolab_gnss_casmLogger_MainActivity_passingobsLBSdataJNI(JNIEnv *env, jobject instance,
                                                                   jdouble lat, jdouble lng,
                                                                   jdouble alt, jstring epho_) {
    const char *epho = env->GetStringUTFChars(epho_, 0);

    // TODO

    env->ReleaseStringUTFChars(epho_, epho);
}