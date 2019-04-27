package com.ljf.opencvocr.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * 扫码签名WebSocket
 * @author ljf
 * @since 2018-10-31
 */
//定义为一个WebSocket服务器，用于监听用户连接的终端访问URL地址,客户端可以通过这个URL来连接到WebSocket服务器端
@ServerEndpoint(value="/websocket/{userId}/{pageId}")
public class UploadWebSocket {
	
	private static Logger logger = LoggerFactory.getLogger(UploadWebSocket.class);
	
    //用来存放每个客户端对应的WebSocket对象，适用于同时与多个客户端通信
    public static CopyOnWriteArraySet<UploadWebSocket> webSocketSet = new CopyOnWriteArraySet<UploadWebSocket>();
    
    //若要实现服务端与指定客户端通信的话，可以使用Map来存放，其中Key可以为用户标识
    public static ConcurrentMap<Session,Object> webSocketMap = new ConcurrentHashMap<Session,Object>();
 
    //与某个客户端的连接会话，通过它实现定向推送(只推送给某个用户)
    private Session session;
	
    //用户ID
    private String userId;
    
    //页面ID
    private String pageId;
    
    //socketId（用来区分是否是同一对象）
    private long socketId = new Date().getTime();
 
    /**
     * 连接建立
     * @param userId 用户ID
     * @param pageId 页面ID
     * @param session WebSocket session
     */
    @OnOpen
    public void onOpen(@PathParam("userId")String userId,@PathParam("pageId")String pageId,Session session){
    	for (Session s : webSocketMap.keySet()) {
    		UploadWebSocket ws = (UploadWebSocket) webSocketMap.get(s);
    		if(ws.getUserId().equals(userId)&&ws.getPageId().equals(pageId)){					
    			try {
    				ws.sendMessage(s,"scanned");
    			} catch (IOException e) {
    				logger.error("", e);
    			}
    			break;
    		}
    	}
    	this.userId = userId;
    	this.pageId = pageId;
        this.session = session;
        webSocketSet.add(this);//加入set中
        webSocketMap.put(session,this);//加入map中
        logger.info("有新连接加入！当前连接数为：{}",webSocketSet.size());
    }
 
    /**
     * 连接关闭
     */
    @OnClose
    public void onClose() {
    	for (Session s : webSocketMap.keySet()) {
    		UploadWebSocket ws = (UploadWebSocket) webSocketMap.get(s);
    		if(ws.getUserId().equals(userId) &&
    				ws.getPageId().equals(pageId) &&
    				ws.getSocketId() != this.socketId){					
    			try {
    				ws.sendMessage(s,"closed");
    			} catch (IOException e) {
    				logger.error("", e);
    			}
    			break;
    		}
    	}
        webSocketSet.remove(this);//从set中删除
        webSocketMap.remove(this.session);//从map中删除
        logger.info("有一连接关闭！当前连接数为：{}",webSocketSet.size());
    }
 
    /**
     * 收到客户端消息
     * @param message 客户端发送过来的消息
     * @throws Exception 
     */
    @OnMessage
    public void onMessage(String message) throws Exception {
    	//推送给同一个操作的两个页面（申请页和签名页）
		for (Session session : webSocketMap.keySet()) {
			UploadWebSocket ws = (UploadWebSocket) webSocketMap.get(session);
			if(ws.getUserId().equals(this.userId) &&
					ws.getPageId().equals(this.getPageId()) &&
					ws.getSocketId() != this.socketId){					
				try {
					this.sendMessage(this.session, message);
					ws.sendMessage(session,message);
				} catch (IOException e) {
					logger.error("", e);
				}
				break;
			}
		}
    }
   
    /**
     * 发生错误
     * @param session
     * @param error
     */
    @OnError
    public void onError(Session session, Throwable error) {
        logger.error("",error);
    }
 
    /**
     * 单发
     * @param session
     * @param message
     * @throws IOException
     */
    public void sendMessage(Session session,String message) throws IOException {
    	synchronized(this) {
    		try {
				if(session.isOpen()){//该session如果已被删除，则不执行发送请求，防止报错
					session.getBasicRemote().sendText(message);
				}
			} catch (IOException e) {
				logger.error("", e);
			}
 
    	}
    }

	public String getUserId() {
		return userId;
	}

	public String getPageId() {
		return pageId;
	}

	public long getSocketId() {
		return socketId;
	}

}
