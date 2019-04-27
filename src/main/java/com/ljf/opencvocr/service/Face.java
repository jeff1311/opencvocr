package com.ljf.opencvocr.service;

import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

import com.ljf.opencvocr.util.Constants;
import com.ljf.opencvocr.util.ImgUtil;
import com.ljf.opencvocr.util.Util;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 面部识别
 * @author ljf
 * @since 2019-03-27
 */
public class Face {

//	static {
//		//载入本地库
//		String opencvLib = Util.getClassPath() + "opencv/dll/opencv_java320.dll";
//		System.load(opencvLib);
//	}

	/**
	 * 身份证正面裁剪（根据人脸识别）
	 * @param src
	 */
	public static Map<String,Mat> idcardCrop(Mat src,boolean test){
		// OpenCV图像识别库一般位于opencv/sources/data下面
		// 1 读取OpenCV自带的人脸识别特征XML文件
		String faceXmlPath = Util.getClassPath() + "/opencv/xml/haarcascade_frontalface_alt.xml";
		CascadeClassifier facebook = new CascadeClassifier(faceXmlPath);

		// 2 修改尺寸
		Size size = null;
		if(src.width() > src.height()){
			if(src.width() > 2000){
				int width = 2000;
				int height = width * src.height() / src.width();
				size = new Size(width, height);
				Imgproc.resize(src, src, size);
			}
		}else{
			if(src.height() > 2000){
				int height = 2000;
				int width = height * src.width() / src.height();
				size = new Size(width, height);
				Imgproc.resize(src, src, size);
			}
		}

		// 3 特征匹配
		Rect[] faces = autoRotate(src, facebook);

        Mat srcClone = null;
		if(test){
            Util.mkDirs(Constants.disk + "/ocr/test");
            srcClone = src.clone();
            // 为每张识别到的人脸画一个圈
            for (int i = 0; i < faces.length; i++) {
                int x = faces[i].x;
                int y = faces[i].y;
                int w = x + faces[i].width;
                int h = y + faces[i].height;
                Point p1 = new Point(x, y);
                Point p2 = new Point(w, h);
                Scalar scalar = new Scalar(0, 255, 0);
                Imgproc.rectangle(srcClone, p1, p2, scalar, 3);
            }
            String fileName1 = Constants.disk + "/ocr/test/a.jpg";
            Imgcodecs.imwrite(Util.mkDirs(fileName1), srcClone);
        }

		// 4 算出身份证区域并裁图
		int maxIndex = 0;
		if(faces.length > 1){
			double maxArea = 0d;
			for(int i = 0;i < faces.length;i ++){
				if(maxArea < faces[i].area()){
					maxArea = faces[i].area();
					maxIndex = i;
				}
			}
		}

		Rect faceRect = faces[maxIndex];

		// 5 把人像区域置为白色
		// 根据人脸检测得到的矩形位置算出大概人像区域
		int x1 = (int) (faceRect.x - faceRect.width / 3.8);
		int y1 = (int) (faceRect.y - faceRect.height / 1.8);
		int w1 = src.width();
		int h1 = (int) (y1 + faceRect.height * 2.1);
		Point point3 = new Point(x1, y1);
		Point point4 = new Point(w1, h1);
		Rect f = new Rect(point3,point4);
		Mat mask = new Mat(src, f);

		// 6 遍历roi区域像素，变为背景色
		for(int x = 0; x < mask.rows();x ++){
			for( int y = 0; y < mask.cols();y ++){
				double[] data = mask.get(0,0);
				mask.put(x,y,data);
			}
		}

		int x0 = (int) (faceRect.x - faceRect.width * 2.8);
		int y0 = (int) (faceRect.y - faceRect.height / 1.6);
		int w0 = (int) (x0 + faceRect.width * 4.2);
		int h0 = (int) (y0 + faceRect.height * 2.9);
		Point point1 = new Point(x0, y0);
		Point point2 = new Point(w0, h0);
		Rect rect = new Rect(point1,point2);
		Mat crop = new Mat(src, rect);

		// 7 裁的区域统一修改尺寸宽度为1200，后面方便取文字区域膨胀操作
		int width = 1200;
		int height = width * crop.height() / crop.width();
		Size cropSize = new Size(width, height);
		Imgproc.resize(crop, crop, cropSize);
		
		Mat key = crop.clone();

		// 8 亮度检测，亮度过低灰度范围相应缩小
        float v = ImgUtil.brightnessException(key);
        int min = 0;
        int max = 100;
        if(v < 1){
            max = 50;
        }

        // 9 遍历剪裁区域像素，除黑字以外都变为灰色
		double[] gray = {150,150,150};
		double[] black = {0,0,0};
		for(int x = 0; x < key.rows();x ++){
			for( int y = 0; y < key.cols();y ++){
				double[] data = key.get(x,y);
				if((data[0] >= min && data[0] <= max) && (data[1] >= min && data[1] <= max) && (data[2] >= min && data[2] <= max)){
					key.put(x,y,black);
				}else{
					key.put(x,y,gray);
				}
			}
		}

        // 保存图片
        if(test){
            String fileName2 = Constants.disk + "/ocr/test/b.jpg";
            Imgcodecs.imwrite(Util.mkDirs(fileName2), src);

            String fileName3 = Constants.disk + "/ocr/test/c.jpg";
            Imgcodecs.imwrite(Util.mkDirs(fileName3), crop);

            String fileName4 = Constants.disk + "/ocr/test/d.jpg";
            Imgcodecs.imwrite(Util.mkDirs(fileName4), key);
        }

		Map<String,Mat> map = new HashMap<String,Mat>();
		map.put("key", key);
		map.put("crop", crop);
		return map;
	}
	
	/**
	 * 人脸识别检测
	 * @param imgPath
	 */
	public static String detect(String imgPath,String outputPath){
		// 1 读取OpenCV自带的人脸识别特征XML文件
		// OpenCV 图像识别库一般位于 opencv\sources\data 下面
		String faceXmlPath = Util.getClassPath() + "/opencv/xml/haarcascade_frontalface_alt.xml";
		CascadeClassifier facebook = new CascadeClassifier(faceXmlPath);
		// 2 读取测试图片
		Mat image = Imgcodecs.imread(imgPath);
		// 3 修改尺寸
		Size size = null;
        if(image.width() > image.height()){        	
        	int width = 1200;
        	int height = width * image.height() / image.width();
        	size = new Size(width, height);
        }else{
        	int height = 1200;
        	int width = height * image.width() / image.height();
        	size = new Size(width, height);
        }
		Imgproc.resize(image, image, size);
		// 4 特征匹配
		Rect[] faces = autoRotate2(image, facebook);

		// 5 为每张识别到的人脸画一个圈
		for (int i = 0; i < faces.length; i++) {
			int x = faces[i].x;
			int y = faces[i].y;
			int w = x + faces[i].width;
			int h = y + faces[i].height;
			Point point1 = new Point(x, y);
			Point point2 = new Point(w, h);
			Scalar scalar = new Scalar(0, 255, 0);
			Imgproc.rectangle(image, point1, point2, scalar);
		}
		// 6 保存图片
		String fileName = new Date().getTime() + ".jpg";
		Imgcodecs.imwrite(outputPath + fileName, image);
		return fileName;
	}

	/**
	 * 特征匹配&自动旋转
	 * @param src
	 * @param facebook
	 * @return
	 */
	public static Rect[] autoRotate(Mat src,CascadeClassifier facebook){
		MatOfRect face = new MatOfRect();
		Rect[] faces = null;
		boolean hasface = false;
		int times = 0;
		while(!hasface && times < 3){
			times ++;
			if(times == 2){
				// 向左旋转90度
				rotateLeft(src);
			}else if(times == 3){
				// 向右旋转180度
				rotateRight(src);
				rotateRight(src);
			}
			facebook.detectMultiScale(src, face);
			// 匹配 Rect 矩阵 数组
			faces = face.toArray();
			System.out.println("匹配到 " + faces.length + " 个人脸");
			if(faces.length > 0){
				double maxArea = 0d;
				for(Rect f : faces){
//					int x = f.x;
//					int y = f.y;
//					int w = x + f.width;
//					int h = y + f.height;
//					Point point1 = new Point(x, y);
//					Point point2 = new Point(w, h);
//					Scalar scalar = new Scalar(0, 255, 0);
//					Imgproc.rectangle(src, point1, point2, scalar);
//					Imgcodecs.imwrite("E:/ocr/test/face.jpg", src);

					if(maxArea < f.area()){
						maxArea = f.area();
					}
				}
				if(maxArea > 40000 && maxArea < 200000){
					hasface = true;
				}
			}
		}
		return faces;
	}

	/**
	 * 特征匹配&自动旋转
	 * @param src
	 * @param facebook
	 * @return
	 */
	public static Rect[] autoRotate2(Mat src,CascadeClassifier facebook){
		MatOfRect face = new MatOfRect();
		Rect[] faces = null;
		boolean hasface = false;
		int times = 0;
		while(!hasface && times < 3){
			times ++;
			if(times == 2){
				// 向左旋转90度
				rotateLeft(src);
			}else if(times == 3){
				// 向右旋转180度
				rotateRight(src);
				rotateRight(src);
			}
			facebook.detectMultiScale(src, face);
			// 匹配 Rect 矩阵 数组
			faces = face.toArray();
			System.out.println("匹配到 " + faces.length + " 个人脸");
			if(faces.length > 0){
				double maxArea = 0d;
				for(Rect f : faces){
//					int x = f.x;
//					int y = f.y;
//					int w = x + f.width;
//					int h = y + f.height;
//					Point point1 = new Point(x, y);
//					Point point2 = new Point(w, h);
//					Scalar scalar = new Scalar(0, 255, 0);
//					Imgproc.rectangle(src, point1, point2, scalar);
//					Imgcodecs.imwrite("E:/ocr/test/face.jpg", src);

					System.out.println(f.area());
					if(maxArea < f.area()){
						maxArea = f.area();
					}
				}
				if(maxArea > 1000){
					hasface = true;
				}
			}
		}
		return faces;
	}

	/**
	 * 图像整体向左旋转90度
	 * @param src Mat
	 * @return 旋转后的Mat
	 */
	public static Mat rotateLeft(Mat src) {
		// 此函数是转置、（即将图像逆时针旋转90度，然后再关于x轴对称）
		Core.transpose(src, src);
		// flipCode = 0 绕x轴旋转180， 也就是关于x轴对称
		// flipCode = 1 绕y轴旋转180， 也就是关于y轴对称
		// flipCode = -1 此函数关于原点对称
		Core.flip(src, src, 0);
		return src;
	}

	/**
	 * 图像整体向右旋转90度
	 * @param src Mat
	 * @return 旋转后的Mat
	 */
	public static Mat rotateRight(Mat src) {
		// 此函数是转置、（即将图像逆时针旋转90度，然后再关于x轴对称）
		Core.transpose(src, src);
		// flipCode = 0 绕x轴旋转180， 也就是关于x轴对称
		// flipCode = 1 绕y轴旋转180， 也就是关于y轴对称
		// flipCode = -1 此函数关于原点对称
		Core.flip(src, src, 1);
		return src;
	}

}
