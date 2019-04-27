package com.ljf.opencvocr.util;

import java.awt.Component;
import java.awt.image.BufferedImage;

import javax.swing.*;

import org.opencv.core.Mat;

public class ShowImage {

    public ShowImage(String title,Mat mat) {
        init(title,mat);
    }

    private void init(String title,Mat mat) {
        JFrame frame = new JFrame();
        frame.setTitle(title);
        frame.setBounds(100, 100, mat.width()+15, mat.height()+37);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().setLayout(null);
        BufferedImage image = ImgUtil.Mat2BufImg(mat, ".jpg");
        JLabel label = new JLabel(""){
            @Override
            public void setLabelFor(Component c) {
                super.setLabelFor(c);
            }
        };
        label.setBounds(0, 0, mat.width(), mat.height());
        frame.getContentPane().add(label);
        label.setIcon(new ImageIcon(image));
        frame.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
        frame.setVisible(true);
    }

}