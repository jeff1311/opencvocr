package com.ljf.opencvocr.controller;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FaceController extends HttpServlet {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
//        Model upload = Upload.getInfo(req);
//        Map<String, String> params = upload.getParams();
//        String storagePath = params.get("storagePath");
//        String storageName = params.get("storageName");
//        BufferedImage img = upload.getImg();
//        String outputPath = "E:/ocr/face/";
//        String imgName = new Date().getTime() + ".jpg";
//        ImageIO.write(img,"jpg",new File(outputPath + imgName));
//        String imgPath = Face.detect(outputPath + imgName,outputPath);
//        JSONObject json = new JSONObject();
//        json.put("code", 200);
//        json.put("imgPath", "/img/" + imgPath);
//        Util.returnInfo(resp,json);
    }

}
