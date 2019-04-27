package com.ljf.opencvocr.service;

import com.alibaba.fastjson.JSONObject;
import com.ljf.opencvocr.util.Constants;
import com.ljf.opencvocr.util.IdCardUtil;
import com.ljf.opencvocr.util.ImgUtil;
import com.ljf.opencvocr.util.Util;

import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.opencv.core.*;
import org.opencv.core.Point;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Map;

/**
 * 图片文字识别
 * @author ljf
 * @since 2019-03-17
 */
public class OCR {

	static {
		//载入opencv库
		String opencvLib = Util.getClassPath() + "opencv/dll/opencv_java320.dll";
		System.load(opencvLib);
	}

	public static JSONObject execute(BufferedImage srcBi, boolean test){
        //创建一个Mat,颜色为白色
        Mat src = new Mat(srcBi.getHeight(), srcBi.getWidth(), CvType.CV_8UC3,new Scalar(255, 255, 255));
        //BufferedImage转Mat
        for(int y = 0;y < src.rows();y ++){
            for(int x = 0;x < src.cols();x ++){
                int rgba = srcBi.getRGB(x,y);
                Color col = new Color(rgba, true);
                int r = col.getRed();
                int g = col.getGreen();
                int b = col.getBlue();
                double[] data = {b,g,r};
                src.put(y,x,data);
            }
        }

		//根据人脸识别裁剪身份证以内的区域
		Map<String, Mat> crop = Face.idcardCrop(src,false);
		Mat cropSrc = crop.get("crop");
		Mat cropSrc2 = null;
		if(test){
            cropSrc2 = cropSrc.clone();
        }
		Mat key = crop.get("key");
		//灰度化
		Mat gray = key;
		Imgproc.cvtColor(gray, gray, Imgproc.COLOR_BGR2GRAY);
        if(test){
            Imgcodecs.imwrite(Util.mkDirs(Constants.disk + "/ocr/test/e.jpg"), gray);
        }

		//二值化（自适应）
		int blockSize = 25;
		int threshold = 35;
		Imgproc.adaptiveThreshold(gray, gray, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY_INV, blockSize, threshold);
		
		//过滤杂纹
//		Imgproc.medianBlur(gray, gray,3);
        if(test){
            Imgcodecs.imwrite(Util.mkDirs(Constants.disk + "/ocr/test/f.jpg"), gray);
        }

		//膨胀（白色膨胀）
		Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT,new Size(3,3));//使用3*3交叉内核
		Imgproc.dilate(gray, gray, kernel, new Point(-1, -1), 20);//以这个内核为中心膨胀N倍
        if(test){
            Imgcodecs.imwrite(Util.mkDirs(Constants.disk + "/ocr/test/g.jpg"), gray);
        }

		//腐蚀（黑色膨胀）
		Mat kernel3 = Imgproc.getStructuringElement(Imgproc.MORPH_RECT,new Size(3,3));//使用3*3交叉内核
		Imgproc.erode(gray, gray, kernel3, new Point(-1, -1), 10);
        if(test){
            Imgcodecs.imwrite(Util.mkDirs(Util.mkDirs(Constants.disk + "/ocr/test/h.jpg")), gray);
        }

		//查找轮廓
		ArrayList<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(gray, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

        //创建一个白色图片作为背景
        Mat img = new Mat(cropSrc.rows(), cropSrc.cols(), CvType.CV_8UC1,new Scalar(255));
        if(test){
            Imgcodecs.imwrite(Util.mkDirs(Constants.disk + "/ocr/test/i.jpg"),img);
        }

        for(int i = 0;i < contours.size();i ++){
            Rect rect = Imgproc.boundingRect(contours.get(i));

            if(rect.area() > 1500){
                //画出矩形
                if(test){
                    int x = rect.x;
                    int y = rect.y;
                    int w = x + rect.width;
                    int h = y + rect.height;
                    Point point1 = new Point(x, y);
                    Point point2 = new Point(w, h);
                    Scalar scalar = new Scalar(0, 255, 0);
                    Imgproc.rectangle(cropSrc2,point1,point2,scalar,1);
                }
                Mat r = new Mat(cropSrc, rect);
                //灰度化
                Imgproc.cvtColor(r,r,Imgproc.COLOR_BGR2GRAY);
                //二值化（自适应）
                Imgproc.adaptiveThreshold(r, r, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY, 41, 30);
                //去除面积小于20像素的区域
                clean(r,20);
                Mat roi = new Mat(img, rect);
                for(int y = 0;y < roi.rows();y ++){
                    for(int x = 0;x < roi.cols();x ++){
                        double[] data = r.get(y,x);
                        roi.put(y, x, data);
                    }
                }

            }
        }

        if(test){
            Imgcodecs.imwrite(Util.mkDirs(Constants.disk + "/ocr/test/j.jpg"),cropSrc2);
            Imgcodecs.imwrite(Util.mkDirs(Constants.disk + "/ocr/test/k.jpg"),img);
        }

        //OCR
        ITesseract instance = new Tesseract();
        //设置训练库的位置
        String dataPath = Util.getClassPath() + "tessdata";
        instance.setDatapath(dataPath);
        instance.setLanguage("chi_sim");//chi_sim eng
        String result = null;
        BufferedImage binary = ImgUtil.Mat2BufImg(img, ".jpg");
        try {
            result =  instance.doOCR(binary);
        } catch (TesseractException e) {
            e.printStackTrace();
        }
        
		return IdCardUtil.filterOcrInfo(result);
	}

    public static void clean(Mat src,int size){
        //轮廓检测
        Mat temp = src.clone();
        Imgproc.Canny(temp, temp, 20, 60);
        ArrayList<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(temp, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
        //去除面积小于N像素的区域
        for(int j = 0;j < contours.size();j ++){
            Rect br = Imgproc.boundingRect(contours.get(j));
            if(br.area() <= size){
                Mat r = new Mat(src, br);
                for(int y = 0;y < r.rows();y ++){
                    for(int x = 0;x < r.cols();x ++){
                        double[] data = {255};
                        r.put(y, x, data);
                    }
                }
            }
        }
    }
	
}