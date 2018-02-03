package com.apq.plus.util;

import android.content.Context;
import android.text.Html;
import android.widget.TextView;

import com.apq.plus.R;


/**
 * 获取异常信息以及手机信息
 *
 * @author xhh
 */
public class ExceptionUtil {

    private Context mContext;
    private PhoneUtil mPhoneUtil;

    public ExceptionUtil(Context context) {
        this.mContext = context;
        this.mPhoneUtil = new PhoneUtil(context);
    }

    public void printPhoneInfo(TextView textView) {
        textView.append(mContext.getString(R.string.log_message_phone_brand));
        textView.append(Html.fromHtml("<font color=\"#E51C23\">" + mPhoneUtil.getBrand() + "</font>"));
        textView.append("\n");
        textView.append(mContext.getString(R.string.log_message_phone_model));
        textView.append(Html.fromHtml("<font color=\"#E51C23\">" + mPhoneUtil.getModel() + "</font>"));
        textView.append("\n");
        textView.append(mContext.getString(R.string.log_message_phone_product));
        textView.append(Html.fromHtml("<font color=\"#E51C23\">" + mPhoneUtil.getProduct() + "</font>"));
        textView.append("\n");
        textView.append(mContext.getString(R.string.log_message_phone_ver));
        textView.append(Html.fromHtml("<font color=\"#E51C23\">" + mPhoneUtil.getAndroidVersion() + "</font>"));
        textView.append("\n");
        textView.append(mContext.getString(R.string.log_message_phone_appver));
        textView.append(Html.fromHtml("<font color=\"#E51C23\">" + mPhoneUtil.getAppVersion() + "</font>"));

        textView.append("\n");
    }

    public void printError(TextView textView, Throwable throwable) {
        textView.append(mContext.getString(R.string.log_message_error));
        textView.append(Html.fromHtml("<font color=\"#E51C23\">" + throwable.getMessage() + "</font>"));
        textView.append("\n");
        for (StackTraceElement stackTraceElement : throwable.getStackTrace()) {
            String className = stackTraceElement.getClassName();
            String methodName = stackTraceElement.getMethodName();
            String fileName = stackTraceElement.getFileName();
            String line = stackTraceElement.getLineNumber() + "";
            textView.append("\t\t\t\t\t\t");
            textView.append(Html.fromHtml("<font  color=\"#E51C23\">at</font>"));
            textView.append("\t" + className);
            textView.append("." + methodName);
            textView.append("(");
            textView.append(Html.fromHtml("<font color=\"#E51C23\">" + fileName + "</font>"));
            textView.append(":");
            textView.append(Html.fromHtml("<u><font color=\"#5677FC\">" + line + "</font></u>"));
            textView.append(")");
            textView.append("\n");
        }
    }

}
