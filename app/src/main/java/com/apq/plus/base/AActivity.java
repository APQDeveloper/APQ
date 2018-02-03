package com.apq.plus.base;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.apq.plus.R;
import com.apq.plus.app.APPManager;


/**
 * Activity基类
 *
 * @author xhh
 */

public abstract class AActivity extends AppCompatActivity {

    /**
     * 打印类型枚举
     **/
    public enum Print {
        TOAST,
        SNACKBAR,
        DIALOG
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        APPManager.getInstance().addActivity(this);
    }

    @Override
    protected void onDestroy() {
        APPManager.getInstance().removeActivity(this);
        super.onDestroy();
    }

    /**
     * 打印字符串
     *
     * @param type    展示的类型TOAST: 弹出Toast提示,SNACKBAR: 弹出Snackbar提示,DIALOOG: 弹出Dialog提示框
     * @param time    展示时间,当展示类型为DIALOG时无效
     * @param message 标题以及正文0:正文,1:标题
     */
    public void print(Print type, int time, String... message) {
        if (message[0] == null) return;
        //运行在UI线程，方便直接调用
        runOnUiThread(() -> {
            switch (type) {
                case TOAST:
                    toast(message[0], time);
                    break;
                case SNACKBAR:
                    print(message[0], time);
                    break;
                case DIALOG:
                    dialog(message[0], message.length == 2 ? message[1] : null);
                    break;
            }
        });

    }

    public void toast(String message, int time) {
        Toast.makeText(this, message, time).show();
    }

    public void print(String message, int time) {
        Snackbar.make(getWindow().getDecorView(), message, time).show();
    }

    public void dialog(String message, String title) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        if (title != null) builder.setTitle(title);
        builder.setPositiveButton(getString(R.string.base_back), null);
        builder.setMessage(message);
        builder.show();
    }

}
