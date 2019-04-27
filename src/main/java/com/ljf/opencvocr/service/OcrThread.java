package com.ljf.opencvocr.service;

import com.alibaba.fastjson.JSONObject;

import java.awt.image.BufferedImage;
import java.util.List;

public class OcrThread extends Thread {

    private List<BufferedImage> images;
    private List<JSONObject> ocrInfoList;

    public OcrThread(List<BufferedImage> images,List<JSONObject> ocrInfoList){
        this.images = images;
        this.ocrInfoList = ocrInfoList;
    }

    @Override
    public void run() {
        System.out.println(Thread.currentThread().getName());
        for(int i = 0;i < images.size();i ++){
            JSONObject info = OCR.execute(images.get(i),false);
            ocrInfoList.add(info);
        }
    }

}
