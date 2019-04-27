package com.ljf.opencvocr.util;

import org.opencv.core.*;
import org.opencv.core.Point;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * 图片工具类
 * @author ljf
 * @since 2019-03-14
 */
public class ImgUtil {

    private static Logger logger = LoggerFactory.getLogger("ImgUtil");
    
//    static{
//		//载入本地库
//    	String opencvLib = Util.getClassPath() + "opencv/dll/opencv_java320.dll";
//        System.load(opencvLib);
//	}

	public static void autoCrop(String imgPath){
		
		//原图
		Mat src = Imgcodecs.imread(imgPath);
		//原图（灰）
		Mat srcGray = src.clone();
		Imgproc.cvtColor(srcGray, srcGray, Imgproc.COLOR_BGR2GRAY);
        //高斯滤波，降噪
        Imgproc.GaussianBlur(srcGray, srcGray, new Size(3, 3), 0);
        //轮廓检测
        Imgproc.Canny(srcGray, srcGray, 100, 100);
        //轮廓提取
        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(srcGray, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
        //找出最大轮廓
        double maxArea = 0;
        //最大轮廓索引
        int maxIndex = 0;
        for(int i = 0;i < contours.size();i ++){
        	Rect br = Imgproc.boundingRect(contours.get(i));
        	if(maxArea < br.area()){
        		maxArea = br.area();
        		maxIndex = i;
        	}
        }
        //最大轮廓
        Rect maxContour = Imgproc.boundingRect(contours.get(maxIndex));
        Mat srcImg = new Mat(src, maxContour);
        Mat tmpImg = new Mat();
        srcImg.copyTo(tmpImg);
        Imgcodecs.imwrite("E:/ocr/test/" + new Date().getTime() + ".jpg", tmpImg);
        Imgcodecs.imwrite("E:/ocr/test/" + new Date().getTime() + ".jpg", srcGray);
	}
	
	/**
	 * 灰化
	 * @param srcPath
	 * @return	
	 */
	public static Mat gray(String srcPath){
		Mat srcGray = Imgcodecs.imread(srcPath, Imgcodecs.IMREAD_GRAYSCALE);
		Mat dst = new Mat();
		Imgproc.cvtColor(srcGray, dst, Imgproc.THRESH_OTSU);//使用OTSU界定输入图像
//		Imgcodecs.imwrite(dstPath, dst);
		return dst;
	}
	
	/**
	 * 二值化
	 * @param src
	 * @param gray
	 * @return
	 */
	public static Mat binarize(Mat src,Mat gray){
		Mat m = new Mat();
		Imgproc.cvtColor(src, gray, Imgproc.COLOR_RGB2GRAY);
		Mat dst = new Mat();
		Imgproc.threshold(gray, dst, 100, 255, 4);//去水印
		Imgproc.threshold(dst, m, 0, 255, 0);//二值化
		Imgproc.GaussianBlur(m, m, new Size(3, 3), 0);
//		Imgcodecs.imwrite("H:/opencv/test-binary.jpg", m);
		return m;
	}
	
	/**
	 * 膨胀
	 * @param src
	 * @return
	 */
	public static Mat dilate(Mat src,int threshold){
		Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_CROSS,new Size(3,3));     //使用3*3交叉内核
		Mat dilated = new Mat();
		Imgproc.dilate(src, dilated, kernel, new Point(-1, -1), threshold);   //以这个内核为中心膨胀8倍
		//具体的内核大小和膨胀倍数根据实际情况而定，只要确保所有字符都粘在一起即可
//		Imgcodecs.imwrite("H:/opencv/test-dilate.jpg", dilated);
		return dilated;
	}
	
	/**
	 * 二值化（动态）
	 * @param src
	 * @param dst
	 */
	public static Mat binarization(String src, Mat dst) {
		Mat img = Imgcodecs.imread(src);
		Imgproc.cvtColor(img, dst, Imgproc.COLOR_RGB2GRAY);
        Imgproc.adaptiveThreshold(dst, dst, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY_INV, 5, 10);
//        Imgcodecs.imwrite("H:/opencv/test-binary.jpg", dst);
        return dst;
    }
	
	public static boolean isBlack(int colorInt)  
    {  
        Color color = new Color(colorInt);  
        if (color.getRed() + color.getGreen() + color.getBlue() <= 300)  
        {  
            return true;  
        }  
        return false;  
    }  

    public static boolean isWhite(int colorInt)  
    {  
        Color color = new Color(colorInt);  
        if (color.getRed() + color.getGreen() + color.getBlue() > 300)  
        {  
            return true;  
        }  
        return false;  
    }  
    
    public static int isBlack(int colorInt, int whiteThreshold) {
		final Color color = new Color(colorInt);
		if (color.getRed() + color.getGreen() + color.getBlue() <= whiteThreshold) {
			return 1;
		}
		return 0;
	}
	
    /**
     * Mat转换成BufferedImage
     * @param matrix 要转换的Mat
     * @param fileExtension 格式为 ".jpg", ".png", etc
     * @return
     */
    public static BufferedImage Mat2BufImg (Mat matrix, String fileExtension) {
        MatOfByte mob = new MatOfByte();
        Imgcodecs.imencode(fileExtension, matrix, mob);
        byte[] byteArray = mob.toArray();
        BufferedImage bufImage = null;
        try {
            InputStream in = new ByteArrayInputStream(byteArray);
            bufImage = ImageIO.read(in);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bufImage;
    }
 
    /**
     * 图片压缩
     * @param oldFile
     * @param newFile
     * @param width
     * @param height
     * @return
     */
    public static int zipImgAuto(InputStream oldFile, File newFile, int width, int height) {
        try {

            Image srcFile = ImageIO.read(oldFile);
            int w = srcFile.getWidth(null);
            int h = srcFile.getHeight(null);
            double ratio;
            if(width > 0){
                ratio = width / (double) w;
                height = (int) (h * ratio);
            }else{
                if(height > 0){
                    ratio = height / (double) h;
                    width = (int) (w * ratio);
                }
            }

            String srcImgPath = newFile.getAbsoluteFile().toString();

            String suffix = srcImgPath.substring(srcImgPath.lastIndexOf(".") + 1,srcImgPath.length());

            BufferedImage buffImg = null;
            if(suffix.equals("png")){
                buffImg = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            }else{
                buffImg = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            }

            Graphics2D graphics = buffImg.createGraphics();
            graphics.setBackground(new Color(255,255,255));
            graphics.setColor(new Color(255,255,255));
            graphics.fillRect(0, 0, width, height);
            graphics.drawImage(srcFile.getScaledInstance(width, height, Image.SCALE_SMOOTH), 0, 0, null);

            ImageIO.write(buffImg, suffix, new File(srcImgPath));

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return height;
    }

    /**
     * 图片裁剪
     * @param filePath
     * @param x
     * @param y
     * @param w
     * @param h
     * @param binaryFlag
     * @return
     */
    public static BufferedImage cropImage(String filePath, int x, int y, int w, int h,boolean binaryFlag){
        ImageInputStream iis = null;
        try {
            iis = ImageIO.createImageInputStream(new FileInputStream(filePath));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Iterator<ImageReader> it = ImageIO.getImageReadersByFormatName("jpg");
        ImageReader imgReader = it.next();
        imgReader.setInput(iis);
        ImageReadParam par = imgReader.getDefaultReadParam();
        par.setSourceRegion(new Rectangle(x, y, w, h));
        BufferedImage bi = null;
        try {
            bi = imgReader.read(0, par);
            //是否二值化
            if(binaryFlag){
                bi = binary(bi,bi);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bi;
    }

    /**
     * 二值化
     * @param src
     * @param dest
     * @return
     */
    public static BufferedImage binary(BufferedImage src, BufferedImage dest) {
        int width = src.getWidth();
        int height = src.getHeight();

        int[] inPixels = new int[width * height];
        int[] outPixels = new int[width * height];

        getRGB(src, 0, 0, width, height, inPixels);
        int index = 0;
        int means = getThreshold(inPixels, height, width);
        for (int row = 0; row < height; row++) {
            int ta = 0, tr = 0, tg = 0, tb = 0;
            for (int col = 0; col < width; col++) {
                index = row * width + col;
                ta = (inPixels[index] >> 24) & 0xff;
                tr = (inPixels[index] >> 16) & 0xff;
                tg = (inPixels[index] >> 8) & 0xff;
                tb = inPixels[index] & 0xff;
                if (tr > means) {
                    tr = tg = tb = 255;//白
                } else {
                    tr = tg = tb = 0;//黑
                }
                outPixels[index] = (ta << 24) | (tr << 16) | (tg << 8) | tb;
            }
        }
        setRGB(dest, 0, 0, width, height, outPixels);
        return dest;
    }

    private static int getThreshold(int[] inPixels, int h, int w) {
        int iniThreshold = 120;
        int finalThreshold = 0;
        int temp[] = new int[inPixels.length];
        for (int index = 0; index < inPixels.length; index++) {
            temp[index] = (inPixels[index] >> 16) & 0xff;
        }
        List<Integer> sub1 = new ArrayList<Integer>();
        List<Integer> sub2 = new ArrayList<Integer>();
        int means1 = 0, means2 = 0;
        while (finalThreshold != iniThreshold) {
            finalThreshold = iniThreshold;
            for (int i = 0; i < temp.length; i++) {
                if (temp[i] <= iniThreshold) {
                    sub1.add(temp[i]);
                } else {
                    sub2.add(temp[i]);
                }
            }
            means1 = getMeans(sub1);
            means2 = getMeans(sub2);
            sub1.clear();
            sub2.clear();
            iniThreshold = (means1 + means2) / 2;
        }
        finalThreshold -= 15;
        return finalThreshold;
    }

    private static int getMeans(List<Integer> data) {
        int result = 0;
        int size = data.size();
        for (Integer i : data) {
            result += i;
        }
        if(result != 0 && size != 0){
        	return (result / size);
        }else{
        	return 0;
        }
    }

    public static void setRGB(BufferedImage image, int x, int y, int w,int h, int[] pixels) {
        int type = image.getType();
        if (type == BufferedImage.TYPE_INT_ARGB || type == BufferedImage.TYPE_INT_RGB){
            image.getRaster().setDataElements(x, y, w, h, pixels);
        }else{
            image.setRGB(x, y, w, h, pixels, 0, w);
        }
    }

    public static void getRGB(BufferedImage image, int x, int y, int w,int h, int[] pixels) {
        int type = image.getType();
        if (type == BufferedImage.TYPE_INT_ARGB || type == BufferedImage.TYPE_INT_RGB){
            image.getRaster().getDataElements(x, y, w, h, pixels);
        }else{
            image.getRGB(x, y, w, h, pixels, 0, w);
        }
    }

    /**
     * inputStream转换为字节数组
     * @param input
     * @return
     */
    public static byte[] toByteArray(InputStream input) {
        if (input == null) {
            return null;
        }
        ByteArrayOutputStream output = null;
        byte[] result = null;
        try {
            output = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024 * 100];
            int n = 0;
            while (-1 != (n = input.read(buffer))) {
                output.write(buffer, 0, n);
            }
            result = output.toByteArray();
            if (output != null) {
                output.close();
            }
        } catch (Exception e) {}
        return result;
    }

    /**
     * 字节数组转换为BufferedImage
     * @param imagedata
     * @return
     */
    public static BufferedImage toBufferedImage(byte[] imagedata) {
        Image image = Toolkit.getDefaultToolkit().createImage(imagedata);
        if (image instanceof BufferedImage) {
            return (BufferedImage) image;
        }
        image = new ImageIcon(image).getImage();
        BufferedImage bimage = null;
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        try {
            int transparency = Transparency.OPAQUE;
            GraphicsDevice gs = ge.getDefaultScreenDevice();
            GraphicsConfiguration gc = gs.getDefaultConfiguration();
            bimage = gc.createCompatibleImage(image.getWidth(null), image.getHeight(null), transparency);
        } catch (HeadlessException e) {

        }
        if (bimage == null) {
            int type = BufferedImage.TYPE_INT_RGB;
            bimage = new BufferedImage(image.getWidth(null), image.getHeight(null), type);
        }
        Graphics g = bimage.createGraphics();
        g.drawImage(image, 0, 0, null);
        g.dispose();
        return bimage;
    }

    public static void window(String title,Mat mat){
    	new ShowImage(title,mat);
    }

    /**
     * opencv 检测图片亮度
     * brightnessException 计算并返回一幅图像的色偏度以及，色偏方向
     * cast 计算出的偏差值，小于1表示比较正常，大于1表示存在亮度异常；当cast异常时，da大于0表示过亮，da小于0表示过暗
     * 返回值通过cast、da两个引用返回，无显式返回值
     */
    public static float brightnessException (Mat srcImage) {
        Mat dstImage = new Mat();
        // 将RGB图转为灰度图
        Imgproc.cvtColor(srcImage,dstImage, Imgproc.COLOR_BGR2GRAY);
        float a = 0;
        int[] Hist = new int[256];
        for(int i = 0;i < 256;i ++){
            Hist[i] = 0;
        }
        for(int i = 0;i < dstImage.rows();i ++){
            for(int j = 0;j < dstImage.cols();j ++){
                //在计算过程中，考虑128为亮度均值点
                a += (float)(dstImage.get(i,j)[0] - 128);
                int x = (int)dstImage.get(i,j)[0];
                Hist[x] ++;
            }
        }
        float da = a / (float)(dstImage.rows() * dstImage.cols());
        System.out.println(da);
        float D = Math.abs(da);
        float Ma = 0;
        for(int i = 0;i < 256;i ++){
            Ma += Math.abs(i - 128 - da) * Hist[i];
        }
        Ma /= (float)((dstImage.rows() * dstImage.cols()));
        float M = Math.abs(Ma);
        float K = D / M;
        float cast = K;
        System.out.printf("亮度指数：%f\n",cast);
//        if(cast>=1) {
//            System.out.printf("亮度："+da);
//            if(da > 0) {
//                System.out.printf("过亮\n");
//                return 2;
//            } else {
//                System.out.printf("过暗\n");
//                return 1;
//            }
//        } else {
//            System.out.printf("亮度：正常\n");
//            return 0;
//        }
        return cast;
    }

}
