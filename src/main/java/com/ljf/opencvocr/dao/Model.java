package com.ljf.opencvocr.dao;

import java.util.List;
import java.util.Map;

public class Model {

    private Map<String,String> params;
    private List<Img> images;

    public Model(Map<String,String> params,List<Img> images){
        this.params = params;
        this.images = images;
    }

    public Map<String, String> getParams() {
        return params;
    }
    public void setParams(Map<String, String> params) {
        this.params = params;
    }
    public List<Img> getImages() {
        return this.images;
    }
    public void setImages(List<Img> images) {
        this.images = images;
    }

}
