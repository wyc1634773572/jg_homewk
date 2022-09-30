#include<opencv2/core/core.hpp>
#include<opencv2/highgui/highgui.hpp>
#include<opencv2/imgproc.hpp>
#include <iostream>
using namespace std;
using namespace cv;

Mat g_srcim, g_dstboxim;

void OnBoxChange(int blurval, void* v) {
    boxFilter(g_srcim,g_dstboxim,-1,Size(blurval +1, blurval +1));
    imshow("dst", g_dstboxim);
}

int main()
{
    int default_val = 5;

    g_srcim = imread("C:\\Users\\wangyicheng\\Desktop\\restudy\\girl_480.jpg",1);
    g_dstboxim = g_srcim.clone();
    imshow("srcimage", g_srcim);
    namedWindow("dst");             //Must creat new window before createTrackbar

    createTrackbar("内核size", "dst",&default_val,10, OnBoxChange);
    
    waitKey(0);
}
