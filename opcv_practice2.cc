#include<opencv2/core/core.hpp>
#include<opencv2/highgui/highgui.hpp>
#include<opencv2/imgproc.hpp>
#include <iostream>
using namespace std;
using namespace cv;

Mat g_srcim, g_dstboxim, g_dstblurim, g_dstgaussim;
static int default_val = 5;

void OnBoxChange(int blurval, void* v) {
    boxFilter(g_srcim,g_dstboxim,-1,Size(blurval +1, blurval +1));
    imshow("boxfilter", g_dstboxim);
}

void OnBlurChange(int blurval, void* v) {
    blur(g_srcim, g_dstblurim, Size(blurval + 1, blurval + 1));
    imshow("blur", g_dstblurim);
}

void OnGaussChange(int blurval, void* v) {
    GaussianBlur(g_srcim, g_dstgaussim, Size(blurval*2 + 1, blurval*2 + 1),0,0);
    imshow("gauss", g_dstgaussim);
}

int main()
{
    g_srcim = imread("C:\\Users\\wangyicheng\\Desktop\\restudy\\girl_480.jpg",1);
    g_dstboxim = g_srcim.clone();
    g_dstblurim = g_srcim.clone();
    g_dstgaussim = g_srcim.clone();

    imshow("srcimage", g_srcim);

    namedWindow("boxfilter");             //Must creat new window before createTrackbar
    createTrackbar("内核size", "boxfilter",&default_val,10, OnBoxChange);

    namedWindow("blur");
    createTrackbar("内核size", "blur", &default_val, 10, OnBlurChange);

    namedWindow("gauss");
    createTrackbar("内核size", "gauss", &default_val, 10, OnGaussChange);
    
    waitKey(0);
}

