package com.apq.plus.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import com.apq.plus.R;
import com.apq.plus.app.APPManager;
import com.apq.plus.base.AActivity;
import com.apq.plus.base.AAppBarStateChangeListener;
import com.apq.plus.util.ExceptionUtil;
import com.apq.plus.util.ExtrasUtil;

/**
 * Bug提示界面
 *
 * @author xhh
 */

public class BugActivity extends AActivity implements View.OnClickListener {

    private CollapsingToolbarLayout mCollapsingToolbarLayout;
    private AppBarLayout mAppBarLayout;
    private Toolbar mToolbar;
    private AppCompatTextView mTextDetial;
    private FloatingActionButton mFabSend;
    private FloatingActionButton mFabSendA;
    private Throwable mThrowable;
    private ExceptionUtil mExceptionInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bug);
        mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        Intent intent = getIntent();
        if (intent != null) {
            try {
                mThrowable = (Throwable) intent.getSerializableExtra(ExtrasUtil.EXTRA_BUG_THROWABLE);
            } catch (Exception e) {
                print(Print.DIALOG, 0, e.getMessage(), getString(R.string.base_prompt));
            }
        }
        initView();
        initData();

    }

    private void initView() {
        mAppBarLayout = findViewById(R.id.app_bar);
        mCollapsingToolbarLayout = findViewById(R.id.toolbar_layout);
        mTextDetial = findViewById(R.id.text_Detial);
        mFabSend = findViewById(R.id.fab);
        mFabSendA = findViewById(R.id.fab_send);
    }

    private void initData() {
        mExceptionInfo = new ExceptionUtil(BugActivity.this);

        mFabSend.setOnClickListener(this);
        mFabSendA.setOnClickListener(this);

        mAppBarLayout.addOnOffsetChangedListener(new AAppBarStateChangeListener() {
            @Override
            public void onAppBarStateChanged(AppBarLayout appBarLayout, State state) {
                switch (state) {
                    case IDLE:
                        mCollapsingToolbarLayout.setTitleEnabled(true);
                        break;
                    case EXPANDED:
                        mCollapsingToolbarLayout.setTitleEnabled(true);
                        mFabSendA.hide();
                        break;
                    case COLLAPSED:
                        mCollapsingToolbarLayout.setTitleEnabled(false);
                        mFabSendA.show();
                        break;
                }
            }
        });

        showError();
    }

    private void showError() {
        try {
            mToolbar.setSubtitle(mThrowable.getMessage());
            mTextDetial.setText("");
            mExceptionInfo.printPhoneInfo(mTextDetial);
            if (mThrowable != null) mExceptionInfo.printError(mTextDetial, mThrowable);
        } catch (Exception e) {
            print(Print.DIALOG, 0, e.getMessage(), getString(R.string.base_prompt));
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fab:
            case R.id.fab_send:
                print(Print.SNACKBAR, Snackbar.LENGTH_SHORT, getString(R.string.log_message_send));
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, mTextDetial.getText().toString());
                sendIntent.setType("text/plain");
                startActivity(Intent.createChooser(sendIntent, getString(R.string.log_message_send)));
                break;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                APPManager.getInstance().exitApp();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        APPManager.getInstance().exitApp();
        super.onBackPressed();
    }
}
