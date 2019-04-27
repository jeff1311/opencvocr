package com.ljf.opencvocr.service;

import java.awt.image.BufferedImage;
import java.util.List;

import org.springframework.messaging.simp.SimpMessagingTemplate;

import com.alibaba.fastjson.JSONObject;
import com.ljf.opencvocr.dao.Img;

public class OcrThread extends Thread {
	
	SimpMessagingTemplate sm;
    private List<Img> imgs;
    private List<JSONObject> ocrInfoList;
    private String uuid;

    public OcrThread(List<Img> images,List<JSONObject> ocrInfoList,String uuid,SimpMessagingTemplate sm){
        this.imgs = images;
        this.ocrInfoList = ocrInfoList;
        this.uuid = uuid;
        this.sm = sm;
    }

    @Override
    public void run() {
        System.out.println(Thread.currentThread().getName());
        for(int i = 0;i < imgs.size();i ++){
            JSONObject info = OCR.execute(imgs.get(i).getImg(),false);
            info.put("imgId", imgs.get(i).getImgId());
            ocrInfoList.add(info);
            if(uuid != null){            	
            	sm.convertAndSendToUser(uuid, "/idCard", info.toJSONString());
            }
        }
    }

}
