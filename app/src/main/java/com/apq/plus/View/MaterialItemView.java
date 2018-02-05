package com.apq.plus.View;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
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
    private CircleImageView shape;
    public MaterialItemView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        LayoutInflater.from(context).inflate(R.layout.material_item_view,this);
        title = findViewById(R.id.title);
        subtitle = findViewById(R.id.subtitle);
        titleT = findViewById(R.id.title_text);
        shape = findViewById(R.id.title_shape);
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
    }

    public void setShapeColor(int color){
        shape.setCircleBackgroundColor(color);
    }

    private char upperLetter(char cr){
        if (cr >= 'a' && cr <= 'z'){
            cr+=32;
        }
        return cr;
    }
}
