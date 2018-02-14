package com.apq.plus.View;

/**
 * Created by zhufu on 2/14/18.
 * 配合MaterialItemView使用
 */

public class TextInfo{
    public static final String DO_NOT_CHANGE_TITLE = "$[DO_NOT_CHANGE]";
    public static final int DO_NOT_CHANG_RES = -1;

    String title,subtitle;
    char shapeText;
    int shapeImage = 0;

    public TextInfo(String title,String subtitle){
        this.title = title;
        this.subtitle = subtitle;
        if (!title.isEmpty())
            this.shapeText = upperLetter(title.charAt(0));
    }

    public TextInfo(String title,String subtitle,int shapeImage){
        this.title = title;
        this.subtitle = subtitle;
        if (shapeImage != 0){
            shapeText = ' ';
            this.shapeImage = shapeImage;
        }
    }

    public TextInfo(String title,String subtitle,char shapeText){
        this.title = title;
        this.subtitle = subtitle;
        this.shapeText = shapeText;
    }

    private char upperLetter(char cr){
        if (cr >= 'a' && cr <= 'z'){
            cr+=32;
        }
        return cr;
    }
}
