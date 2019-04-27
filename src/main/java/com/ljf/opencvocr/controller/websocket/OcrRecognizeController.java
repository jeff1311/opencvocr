package com.ljf.opencvocr.controller.websocket;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

@Controller
public class OcrRecognizeController {
	
	@Autowired
	SimpMessagingTemplate sm;
	
//	@SendTo()
//	@SendToUser()
	@MessageMapping("/recognize")
	public void send(Message<String> message){
		String msgStr = message.getPayload();
		JSONObject msg = JSON.parseObject(msgStr);
		String uuid = msg.getString("uuid");
		String text = msg.getString("text");
//		sm.convertAndSend("/recognize",msg);
		sm.convertAndSendToUser(uuid, "/test", text);
		System.out.println(msg);
	}
	
}
