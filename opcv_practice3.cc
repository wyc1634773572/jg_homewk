#include<opencv2/core/core.hpp>
#include<opencv2/highgui/highgui.hpp>
#include<opencv2/imgproc.hpp>
#include <iostream>
using namespace std;
using namespace cv;


int main()
{
	Mat girlimg = imread("girl.jpg", 1);
	Mat girlimg2 = imread("girl.jpg",1);
	Mat girlimg3 = imread("girl.jpg", 1);
	Mat sceneimg = imread("scene.jpg");
	Mat lightimg = imread("light.jpg", 1);
	Mat mask = imread("light.jpg", 0);
	vector<Mat> channel;

	Mat ROI = girlimg(Rect(128,128, lightimg.cols, lightimg.rows));
	lightimg.copyTo(ROI, lightimg);

	split(girlimg2, channel);

	resize(sceneimg, sceneimg, Size(480, 480));
	addWeighted(girlimg3, 0.6, sceneimg, 0.4, 0.0, girlimg3);
	

	imshow("ROI", girlimg);
	imshow("split1", channel[0]);
	imshow("split2", channel[1]);
	imshow("split3", channel[2]);
	imshow("addweight", girlimg3);
	waitKey(0);
	imwrite("ROI.jpg", girlimg);
	imwrite("split.jpg", channel[0]);
	imwrite("addWeighted.jpg", girlimg3);
    return 0;
}
