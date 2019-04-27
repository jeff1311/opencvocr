package com.ljf.opencvocr.dao;

import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Map;

public class Model {

    private Map<String,String> params;
    private List<BufferedImage> images;

    public Model(Map<String,String> params,List<BufferedImage> images){
        this.params = params;
        this.images = images;
    }

    public Map<String, String> getParams() {
        return params;
    }
    public void setParams(Map<String, String> params) {
        this.params = params;
    }
    public List<BufferedImage> getImages() {
        return this.images;
    }
    public void setImages(List<BufferedImage> images) {
        this.images = images;
    }

}
