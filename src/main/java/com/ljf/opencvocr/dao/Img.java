package com.ljf.opencvocr.dao;

import java.awt.image.BufferedImage;

public class Img {
	
	private String imgId;
	private BufferedImage img;
	
	public Img(String imgId,BufferedImage img){
		this.imgId = imgId;
		this.img = img;
	}
	
	public String getImgId() {
		return imgId;
	}
	public void setImgId(String imgId) {
		this.imgId = imgId;
	}
	public BufferedImage getImg() {
		return img;
	}
	public void setImg(BufferedImage img) {
		this.img = img;
	}
	
}
