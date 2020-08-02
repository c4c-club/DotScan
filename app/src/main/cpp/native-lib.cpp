#include <jni.h>
#include "com_ncsu_dotscan_MainActivity.h"
#include <android/bitmap.h>
#include <opencv2/opencv.hpp>

using namespace cv;

extern "C"
JNIEXPORT jint JNICALL
Java_com_ncsu_dotscan_MainActivity_getDot
        (JNIEnv *env, jobject obj, jobject bitmap) {
    AndroidBitmapInfo info;
    void *pixels;
    jint count;

    CV_Assert(AndroidBitmap_getInfo(env, bitmap, &info) >= 0);
    CV_Assert(info.format == ANDROID_BITMAP_FORMAT_RGBA_8888 ||
              info.format == ANDROID_BITMAP_FORMAT_RGB_565);
    CV_Assert(AndroidBitmap_lockPixels(env, bitmap, &pixels) >= 0);
    CV_Assert(pixels);

    Mat temp(info.height, info.width, CV_8UC4, pixels);//src
    Mat gray;//dst
    //Convert to gray image
    cvtColor(temp, gray, COLOR_RGBA2GRAY);
    //Binary segmentation
    threshold(gray, gray, 14, 255,THRESH_BINARY );//THRESH_BINARY|THRESH_TRIANGLE

    //Morphological operation
    //Rectangular structure
    Mat kernel = getStructuringElement(MORPH_RECT, Size(3,3), Point(-1, -1));
    erode(gray, gray, kernel, Point(-1, -1), 1);
    //Dilate
    dilate(gray, gray, kernel, Point(-1, -1), 1);

    //Count
    std::vector<std::vector<Point>> contours;
    findContours(gray, contours, RETR_EXTERNAL, CHAIN_APPROX_SIMPLE);

    // draw result
    Mat markers = Mat::zeros(temp.size(), CV_8UC3);
    RNG rng(12345);
    for (size_t t = 0; t < contours.size(); ++t)
    {
        drawContours(markers, contours, static_cast<int>(t), Scalar(rng.uniform(0, 255), rng.uniform(0, 255), rng.uniform(0, 255)),
                     -1, 8, Mat());
    }
    cvtColor(gray, temp, COLOR_GRAY2RGBA);

    AndroidBitmap_unlockPixels(env, bitmap);
    count = contours.size();
    return count;
}





//extern "C" JNIEXPORT jstring JNICALL

//Java_com_ncsu_dotscan_MainActivity_stringFromJNI(
//        JNIEnv* env,
//        jobject /* this */) {
//    std::string hello = "Hello from C++";
//    return env->NewStringUTF(hello.c_str());
//}