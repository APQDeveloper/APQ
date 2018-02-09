package com.apq.plus.View;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.Nullable;
import android.support.v4.view.GravityCompat;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.apq.plus.R;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by zhufu on 2/4/18.
 * 继承自LinearLayout的自定义View，实现一个大写字母+标题+副标题的MD效果
 * 布局自R.layout.material_item_view
 */

public class MaterialItemView extends LinearLayout {
    private TextView title,subtitle,titleT;
    public CircleImageView shape;
    public AppCompatImageView shapeImage;

    private Boolean useShapeText = true;

    public MaterialItemView(Context context) {
        super(context);
        init(context);
    }

    public MaterialItemView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public MaterialItemView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public MaterialItemView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }


    private void init(Context context){
        LayoutInflater.from(context).inflate(R.layout.material_item_view,this);
        title = findViewById(R.id.title);
        subtitle = findViewById(R.id.subtitle);
        titleT = findViewById(R.id.title_text);
        shape = findViewById(R.id.title_shape);
        shapeImage = findViewById(R.id.title_shape_img);
    }

    public void setTitle(String str){
        setTitle(str,upperLetter(str.charAt(0)));
    }

    public void setTitle(String str,Character shapeText){
        title.setText(str);
        titleT.setText(shapeText.toString());
    }

    public void setSubtitle(String str){
        subtitle.setText(str);
        if (str.isEmpty()){
            subtitle.setVisibility(View.GONE);
            title.setTextSize(20);
            ((RelativeLayout)title.getParent()).setVerticalGravity(Gravity.CENTER_VERTICAL);
        }
        else {
            subtitle.setVisibility(View.VISIBLE);
            title.setTextSize(18);
            ((RelativeLayout)title.getParent()).setVerticalGravity(Gravity.NO_GRAVITY);
        }
    }

    public void setShapeColor(int color){
        shape.setCircleBackgroundColor(color);
    }

    public void setShapeResource(int resource){
        shape.setImageResource(R.color.transparent);
        if (resource!=0){
            titleT.setVisibility(View.INVISIBLE);
            titleT.setText("");
            shapeImage.setImageResource(resource);
            useShapeText = false;
        }
        else {
            titleT.setVisibility(View.VISIBLE);
            shapeImage.setImageResource(0);
            useShapeText = true;
        }
    }

    public void setShapeBitmap(Bitmap bitmap){
        shape.setImageResource(R.color.transparent);
        if (bitmap!=null){
            titleT.setVisibility(View.INVISIBLE);
        }
        else {
            titleT.setVisibility(View.VISIBLE);
        }
        shapeImage.setImageBitmap(bitmap);
    }

    public int getShapeVisibility(){
        if (shape.getVisibility() == View.INVISIBLE && shapeImage.getVisibility() == View.INVISIBLE && titleT.getVisibility() == View.INVISIBLE)
            return View.INVISIBLE;
        else return View.VISIBLE;
    }

    public void setShapeVisibility(int visibility){
        shape.setVisibility(visibility);
        shapeImage.setVisibility(visibility);
        titleT.setVisibility(visibility);
    }

    private char upperLetter(char cr){
        if (cr >= 'a' && cr <= 'z'){
            cr+=32;
        }
        return cr;
    }
}
