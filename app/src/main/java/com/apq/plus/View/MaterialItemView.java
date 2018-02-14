package com.apq.plus.View;

import android.content.Context;
import android.content.res.TypedArray;
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

import org.w3c.dom.Text;

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

    public MaterialItemView(Context context) {
        super(context);
        init(context,null);
    }

    public MaterialItemView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context,attrs);
    }

    public MaterialItemView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context,attrs);
    }

    public MaterialItemView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context,attrs);
    }


    private void init(Context context,AttributeSet attrs){
        LayoutInflater.from(context).inflate(R.layout.material_item_view,this);
        title = findViewById(R.id.title);
        subtitle = findViewById(R.id.subtitle);
        titleT = findViewById(R.id.title_text);
        shape = findViewById(R.id.title_shape);
        shapeImage = findViewById(R.id.title_shape_img);

        if (attrs == null)
            return;
        TypedArray ta = context.obtainStyledAttributes(attrs,R.styleable.MaterialItemView);
        set(new TextInfo(((String) ta.getText(R.styleable.MaterialItemView_android_title)),((String) ta.getText(R.styleable.MaterialItemView_android_subtitle)),ta.getResourceId(R.styleable.MaterialItemView_src,0)));
        ta.recycle();
    }

    public void set(TextInfo t){
        //Title & Subtitle
        if (t.title == null || !t.title.equals(TextInfo.DO_NOT_CHANGE_TITLE))
            title.setText(t.title);
        if (t.subtitle == null || !t.subtitle.equals(TextInfo.DO_NOT_CHANGE_TITLE))
            subtitle.setText(t.subtitle);
        if (t.subtitle == null || !t.subtitle.equals(TextInfo.DO_NOT_CHANGE_TITLE)) {
            if (t.subtitle == null || t.subtitle.isEmpty()) {
                subtitle.setVisibility(View.GONE);
                title.setTextSize(20);
                ((RelativeLayout) title.getParent()).setVerticalGravity(Gravity.CENTER_VERTICAL);
            } else {
                subtitle.setVisibility(View.VISIBLE);
                title.setTextSize(18);
                ((RelativeLayout) title.getParent()).setVerticalGravity(Gravity.NO_GRAVITY);
            }
        }
        //Shape Image
        if (t.shapeImage != TextInfo.DO_NOT_CHANG_RES) {
            if (t.shapeImage != 0) {
                shapeImage.setImageResource(t.shapeImage);
                titleT.setVisibility(View.INVISIBLE);
                titleT.setText(" ");
            } else {
                shapeImage.setImageResource(0);
                char c[] = {t.shapeText};
                titleT.setVisibility(View.VISIBLE);
                titleT.setText(new String(c));
            }
        }
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

}