package com.ljf.opencvocr.service;

import com.alibaba.fastjson.JSONObject;
import com.ljf.opencvocr.dao.Img;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.List;

public class OcrTaskThread extends Thread {
	
	SimpMessagingTemplate sm;
    private List<Img> imgs;
    private String uuid;

    public OcrTaskThread(List<Img> images, String uuid, SimpMessagingTemplate sm){
        this.imgs = images;
        this.uuid = uuid;
        this.sm = sm;
    }

    @Override
    public void run() {
        System.out.println(Thread.currentThread().getName());
        for(int i = 0;i < imgs.size();i ++){
            JSONObject info = OCR.execute(imgs.get(i).getImg(),false);
            info.put("imgId", imgs.get(i).getImgId());
            if(uuid != null){
            	sm.convertAndSendToUser(uuid, "/idCard", info.toJSONString());
            }
        }
    }

}
