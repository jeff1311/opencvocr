package com.ljf.opencvocr.service;

import org.opencv.core.Mat;
import org.opencv.core.Range;
import org.opencv.core.Rect;
import org.opencv.imgcodecs.Imgcodecs;

import com.ljf.opencvocr.util.ImgUtil;
import com.ljf.opencvocr.util.Util;

public class MatTest {

    static {
        //载入opencv库
        String opencvLib = Util.getClassPath() + "opencv/dll/opencv_java320.dll";
        System.load(opencvLib);
    }

    //不同的Mat头共享相同的数据区
    public static void demo1(){
        Mat a;
        a = Imgcodecs.imread("E:/ocr/test.jpg");
        ImgUtil.window("a",a);
        Mat b = a;
        ImgUtil.window("b",b);
        //修改b看会不会影响a
        for(int x = 0;x < 20;x ++){
            for(int y = 0;y < 20;y ++){
                double[] white = {255,255,255};
                b.put(x,y,white);
            }
        }
        ImgUtil.window("b修改后的a",a);
    }

    //ROI区域
    public static void demo2(){
        //读取图片
        Mat a = Imgcodecs.imread("E:/ocr/test1.jpg");
        ImgUtil.window("a",a);
        //第一种方法：指定矩形区域Rect
        Mat b = new Mat(a,new Rect(200,100,200,200));
        ImgUtil.window("b",b);
        //第二种方法：指定矩形区域Range
        Mat c = new Mat(a,new Range(200,400),new Range(200,400));
        ImgUtil.window("c",c);
//        //修改b看a和c有没有影响
//        for(int x = 0;x < b.cols();x ++){
//            for(int y = 0;y < b.rows();y ++){
//                double[] green = {0,255,0};
//                b.put(x,y,green);
//            }
//        }
//        ImgUtil.window("a",a);
//        ImgUtil.window("b",b);
//        ImgUtil.window("c",c);
    }

    public static void main(String[] args) {
//        demo1();
//        demo2();
    }

}
