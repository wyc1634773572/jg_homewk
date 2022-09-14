#include<opencv2/core/core.hpp>
#include<opencv2/highgui/highgui.hpp>
#include<opencv2/imgproc.hpp>
#include <iostream>
using namespace std;
using namespace cv;

#define COLOR_DIV 100

int main()
{
    Mat img = imread("girl.jpg",1);
    for (int i = 0;i < img.rows;i++) {
        for (int j = 0;j < img.cols;) {
            img.at<Vec3b>(i, j)[0] = img.at<Vec3b>(i, j)[0] / COLOR_DIV * COLOR_DIV + COLOR_DIV / 2;
            img.at<Vec3b>(i, j)[1] = img.at<Vec3b>(i, j)[1] / COLOR_DIV * COLOR_DIV + COLOR_DIV / 2;
            img.at<Vec3b>(i, j)[2] = img.at<Vec3b>(i, j)[2] / COLOR_DIV * COLOR_DIV + COLOR_DIV / 2;
            j += 2;
        }
    }
    //namedWindow("opecv_win");
    imshow("opecv_win",img);

    waitKey(0);
    return 0;
}
