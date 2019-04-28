package com.ljf.opencvocr.controller;

import com.alibaba.fastjson.JSONObject;
import com.ljf.opencvocr.dao.Img;
import com.ljf.opencvocr.dao.Model;
import com.ljf.opencvocr.service.OCR;
import com.ljf.opencvocr.service.OcrTaskThread;
import com.ljf.opencvocr.service.Upload;
import com.ljf.opencvocr.util.Util;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Controller
public class OcrController {
	
	@Autowired
	SimpMessagingTemplate sm;
	
	@RequestMapping("/ocr/idCard")
	public void idCard(HttpServletRequest request,HttpServletResponse response){
//		Util.cleanFiles(Constants.disk + "/ocr/test");
        JSONObject result = new JSONObject();
        String code,errMsg = null;
        Model uploadInfo = Upload.getInfo(request);
        Map<String, String> params = uploadInfo.getParams();
        String uuid = params.get("uuid");
        List<Img> images = uploadInfo.getImages();
        List<JSONObject> ocrInfo = new ArrayList<JSONObject>();
        //图片大于一使用多线程并行处理，小于一使用主线程处理
        if(images.size() > 1){
            //线程池
            int corePoolSize = images.size() <= 4 ? images.size() : 4;//线程池的基本大小
            int maximumPoolSize = 4;//线程池中允许的最大线程数
            int keepAliveTime = 10;//空闲线程等待新任务的最长时间（秒）
            LinkedBlockingQueue<Runnable> runnables = new LinkedBlockingQueue<>();//队列
            ExecutorService pool = new ThreadPoolExecutor(corePoolSize, maximumPoolSize,keepAliveTime, TimeUnit.SECONDS, runnables);

            //每个线程的任务
            List<Img> task1 = new ArrayList<Img>();
            List<Img> task2 = new ArrayList<Img>();
            List<Img> task3 = new ArrayList<Img>();
            List<Img> task4 = new ArrayList<Img>();

            //任务分配
            for(int i = 0;i < images.size();i ++){
                Img img = images.get(i);
                int n = i % 4;
                switch(n){
                    case 0:
                        task1.add(img);
                        break;
                    case 1:
                        task2.add(img);
                        break;
                    case 2:
                        task3.add(img);
                        break;
                    case 3:
                        task4.add(img);
                        break;
                }
            }

            //创建ocr任务对象
            OcrTaskThread ocr1 = new OcrTaskThread(task1,uuid,sm);
            OcrTaskThread ocr2 = new OcrTaskThread(task2,uuid,sm);
            OcrTaskThread ocr3 = new OcrTaskThread(task3,uuid,sm);
            OcrTaskThread ocr4 = new OcrTaskThread(task4,uuid,sm);

            //开启线程执行任务
            pool.execute(ocr1);
            pool.execute(ocr2);
            pool.execute(ocr3);
            pool.execute(ocr4);

            //关闭线程池，不再接新任务
            pool.shutdown();
            try {
                //等待任务完成，超时时间设为180秒
                pool.awaitTermination(180, TimeUnit.SECONDS);
                Thread.sleep(500);//等待0.5秒让websocket先行（前端显示效果）
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            //立即关闭线程池
            pool.shutdownNow();
            code = "200";
        }else if(images.size() == 1){
            JSONObject info = OCR.execute(images.get(0).getImg(),"idCard",false);
            if(uuid != null){
                info.put("imgId", images.get(0).getImgId());
                sm.convertAndSendToUser(uuid, "/idCard", info.toJSONString());
            }else{
                ocrInfo.add(info);
            }
            code = "200";
        }else{
            code = "500";
            errMsg = "没有图片";
        }

        result.put("code", code);
        result.put("errMsg",errMsg);
        result.put("ocrInfo", ocrInfo);
        Util.returnInfo(response, result);
	}

	@RequestMapping("/ocr")
	public void ocr(HttpServletRequest request,HttpServletResponse response){
        Model info = Upload.getInfo(request);
        BufferedImage img = info.getImages().get(0).getImg();
        JSONObject result = OCR.execute(img, "", true);
        Util.returnInfo(response, result);
    }

}
