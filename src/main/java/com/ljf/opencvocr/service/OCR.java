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

	public static JSONObject execute(BufferedImage srcBi,int type, boolean test){
	    JSONObject result = null;
        if(Constants.OCR_IDCARD == type){
            result = idCard(srcBi,test);
        }else{
            result = ocr(srcBi,test);
        }
        return result;
	}

	private static JSONObject idCard(BufferedImage srcBi,boolean test){
        Mat src = buff2Mat(srcBi);
        //根据人脸识别裁剪身份证以内的区域
        Map<String, Mat> crop = Face.idcardCrop(src,test);
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
            Imgcodecs.imwrite(Util.mkDirs(Constants.DISK + "/ocr/test/e.jpg"), gray);
        }

        //二值化（自适应）
        int blockSize = 25;
        int threshold = 35;
        Imgproc.adaptiveThreshold(gray, gray, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY_INV, blockSize, threshold);

        //过滤杂纹
//		Imgproc.medianBlur(gray, gray,3);
        if(test){
            Imgcodecs.imwrite(Util.mkDirs(Constants.DISK + "/ocr/test/f.jpg"), gray);
        }

        Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT,new Size(3,3));//使用3*3交叉内核

        //膨胀（白色膨胀）
        Imgproc.dilate(gray, gray, kernel, new Point(-1, -1), 20);//以这个内核为中心膨胀N倍
        if(test){
            Imgcodecs.imwrite(Util.mkDirs(Constants.DISK + "/ocr/test/g.jpg"), gray);
        }

        //腐蚀（黑色膨胀）
        Imgproc.erode(gray, gray, kernel, new Point(-1, -1), 10);
        if(test){
            Imgcodecs.imwrite(Util.mkDirs(Constants.DISK + "/ocr/test/h.jpg"), gray);
        }

        //查找轮廓
        ArrayList<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(gray, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

        //创建一个白色图片作为背景
        Mat img = new Mat(cropSrc.rows(), cropSrc.cols(), CvType.CV_8UC1,new Scalar(255));
        if(test){
            Imgcodecs.imwrite(Util.mkDirs(Constants.DISK + "/ocr/test/i.jpg"),img);
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
            Imgcodecs.imwrite(Util.mkDirs(Constants.DISK + "/ocr/test/j.jpg"),cropSrc2);
            Imgcodecs.imwrite(Util.mkDirs(Constants.DISK + "/ocr/test/k.jpg"),img);
        }

        //OCR
        ITesseract tess = new Tesseract();
        //设置训练库的位置
        String dataPath = Util.getClassPath() + "tessdata";
        tess.setDatapath(dataPath);
        tess.setLanguage("chi_sim");//chi_sim eng
        String result = null;
        BufferedImage binary = ImgUtil.Mat2BufImg(img, ".jpg");
        try {
            result =  tess.doOCR(binary);
        } catch (TesseractException e) {
            e.printStackTrace();
        }

        return IdCardUtil.filterOcrInfo(result);
    }

    private static JSONObject ocr(BufferedImage srcBi,boolean test){
	    Util.cleanFiles(Constants.DISK + "/ocr/test");
        //BufferedImage转Mat
        Mat src = buff2Mat(srcBi);
        Mat src2 = buff2Mat(srcBi);
        Mat srcGray = src.clone();
        //灰度化
        Imgproc.cvtColor(srcGray,srcGray,Imgproc.COLOR_BGR2GRAY);
        if(test){
            Imgcodecs.imwrite(Util.mkDirs(Constants.DISK + "/ocr/test/a.jpg"),srcGray);
        }
//        //二值化
//        Imgproc.adaptiveThreshold(srcGray,srcGray,255, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY,25,10);
//        if(test){
//            Imgcodecs.imwrite(Util.mkDirs(Constants.DISK + "/ocr/test/b.jpg"),srcGray);
//        }

        //查找轮廓
//        Imgproc.Canny(srcGray, srcGray, 20, 60);
//        if(test){
//            Imgcodecs.imwrite(Util.mkDirs(Constants.DISK + "/ocr/test/c.jpg"),srcGray);
//        }

        ArrayList<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(srcGray, contours, hierarchy, 2, Imgproc.CHAIN_APPROX_SIMPLE);


        //创建一个白色图片作为背景
        Mat img = new Mat(src.rows(), src.cols(), CvType.CV_8UC1,new Scalar(255));

        for(int i = 0;i < contours.size();i ++){
            Rect rect = Imgproc.boundingRect(contours.get(i));

            //画出矩形
            if(test){
                int x = rect.x;
                int y = rect.y;
                int w = x + rect.width;
                int h = y + rect.height;
                Point point1 = new Point(x, y);
                Point point2 = new Point(w, h);
                Scalar scalar = new Scalar(0, 255, 0);
                Imgproc.rectangle(src2,point1,point2,scalar,1);
            }

            Mat r = new Mat(src, rect);
            //灰度化
            Imgproc.cvtColor(r,r,Imgproc.COLOR_BGR2GRAY);
            //二值化（自适应）
            Imgproc.adaptiveThreshold(r, r, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY, 25, 10);
            Mat roi = new Mat(img, rect);
            System.out.println(roi.width());
            if(roi.width() < 50 && roi.height() < 50){
                for(int y = 0;y < roi.rows();y ++){
                    for(int x = 0;x < roi.cols();x ++){
                        double[] data = r.get(y,x);
                        roi.put(y, x, data);
                    }
                }
            }

        }

        if(test){
            Imgcodecs.imwrite(Util.mkDirs(Constants.DISK + "/ocr/test/c.jpg"),src2);
        }

        if(test){
            Imgcodecs.imwrite(Util.mkDirs(Constants.DISK + "/ocr/test/d.jpg"),img);
        }

        //OCR
        ITesseract tess = new Tesseract();
        //设置训练库的位置
        String dataPath = Util.getClassPath() + "tessdata";
        tess.setDatapath(dataPath);
        tess.setLanguage("chi_sim");//chi_sim eng
        String text = null;
        BufferedImage binary = ImgUtil.Mat2BufImg(img, ".jpg");
        try {
            text =  tess.doOCR(binary);
        } catch (TesseractException e) {
            e.printStackTrace();
        }
        System.out.println(text);
        text = text.replace("\n","<br>");
        JSONObject json = new JSONObject();
        json.put("code",200);
        json.put("text",text);
        return json;
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

    public static Mat buff2Mat(BufferedImage src){
        //创建一个Mat,颜色为白色
        Mat dst = new Mat(src.getHeight(), src.getWidth(), CvType.CV_8UC3,new Scalar(255, 255, 255));
        //BufferedImage转Mat
        for(int y = 0;y < dst.rows();y ++){
            for(int x = 0;x < dst.cols();x ++){
                int rgba = src.getRGB(x,y);
                Color col = new Color(rgba, true);
                int r = col.getRed();
                int g = col.getGreen();
                int b = col.getBlue();
                double[] data = {b,g,r};
                dst.put(y,x,data);
            }
        }
        return dst;
    }

}