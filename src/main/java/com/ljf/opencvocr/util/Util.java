package com.ljf.opencvocr.util;

import com.alibaba.fastjson.JSONObject;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

public class Util {

    public static void returnInfo(HttpServletResponse response, JSONObject json){
        response.setContentType("text/html;charset=utf-8");
        PrintWriter pw = null;
        try {
            pw = response.getWriter();
            pw.print(json.toJSONString());
            pw.flush();
            pw.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if(pw != null){
                pw.close();
            }
        }
    }

    /**获取classpath*/
    public static String getClassPath() {
        String classPath = Thread.currentThread().getContextClassLoader().getResource("").getPath();
        String rootPath = "";
        //windows
        if ("\\".equals(File.separator)) {
            rootPath = classPath.substring(1);
        }
        //linux
        if ("/".equals(File.separator)) {
            rootPath = classPath;
        }
        return rootPath;
    }

    /**创建文件，文件夹不存在自动创建*/
    public static File mkFile(String path){
        String dirs = path.substring(0,path.lastIndexOf("/"));
        File f = new File(dirs);
        if(!f.exists()){
            f.mkdirs();
        }
        return new File(path);
    }

    public static String mkDirs(String path){
        String dirs = path.substring(0,path.lastIndexOf("/"));
        File f = new File(dirs);
        if(!f.exists()){
            f.mkdirs();
        }
        return path;
    }

    public static void cleanFiles(String path){
        File b = new File(path);
        File[] files = b.listFiles();
        if(files != null && files.length > 0){        	
        	for(File f : files){
        		f.delete();
        	}
        }
    }

}
