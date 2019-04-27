package com.ljf.opencvocr.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.alibaba.fastjson.JSONObject;
import com.ljf.opencvocr.dao.Img;
import com.ljf.opencvocr.dao.Model;
import com.ljf.opencvocr.service.OcrThread;
import com.ljf.opencvocr.service.Upload;
import com.ljf.opencvocr.util.Util;

@Controller
public class OcrController {
	
	@Autowired
	SimpMessagingTemplate sm;
	
	private List<Img> images = null;

	private static List<JSONObject> ocrInfo = new ArrayList<JSONObject>();

	@RequestMapping("/ocr")
	public void ocr(HttpServletRequest request,HttpServletResponse response){
//		Util.cleanFiles(Constants.disk + "/ocr/test");
        Model uploadInfo = Upload.getInfo(request);
        Map<String, String> params = uploadInfo.getParams();
        String test = params.get("test");
        String uuid = params.get("uuid");
        images = uploadInfo.getImages();

        if(ocrInfo.size() != 0){
            ocrInfo.clear();
        }

        //线程池
        int corePoolSize = images.size() <= 4 ? images.size() : 4;//线程池的基本大小
        int maximumPoolSize = 4;//线程池中允许的最大线程数
        int keepAliveTime = 10;//空闲线程等待新任务的最长时间（秒）
        LinkedBlockingQueue<Runnable> runnables = new LinkedBlockingQueue<>();//队列
        ExecutorService executorService = new ThreadPoolExecutor(corePoolSize, maximumPoolSize,keepAliveTime, TimeUnit.SECONDS, runnables);

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

        //创建ocr对象
        OcrThread ocr1 = new OcrThread(task1,ocrInfo,uuid,sm);
        OcrThread ocr2 = new OcrThread(task2,ocrInfo,uuid,sm);
        OcrThread ocr3 = new OcrThread(task3,ocrInfo,uuid,sm);
        OcrThread ocr4 = new OcrThread(task4,ocrInfo,uuid,sm);

        //开启线程执行
        executorService.execute(ocr1);
        executorService.execute(ocr2);
        executorService.execute(ocr3);
        executorService.execute(ocr4);

        //识别完毕后返回数据
        int maxTime = 10 * 60;//超时时间设为一分钟
        int time = 0;
        while(++ time < maxTime){
            try {
                //每0.1秒循环一次，降低循环次数
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println(ocrInfo.size());
            if(ocrInfo.size() == images.size()){
                //跳出循环
                break;
            }
        }
        //关闭线程池
        executorService.shutdown();

        JSONObject json = new JSONObject();
        json.put("code", 200);
        json.put("ocrInfo", ocrInfo);
        Util.returnInfo(response, json);
	}
	
}
