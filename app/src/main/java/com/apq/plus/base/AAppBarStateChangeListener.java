package com.apq.plus.base;

import android.support.design.widget.AppBarLayout;

/**
 * 重写AppBarLayout状态监听
 *
 * @author xhh
 */
public abstract class AAppBarStateChangeListener implements AppBarLayout.OnOffsetChangedListener {

    public enum State {
        EXPANDED,
        COLLAPSED,
        IDLE
    }

    private State mCurrentState = State.IDLE;

    @Override
    public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
        if (verticalOffset == 0) {
            if (mCurrentState != State.EXPANDED) {
                onAppBarStateChanged(appBarLayout, State.EXPANDED);
            }
            mCurrentState = State.EXPANDED;
        } else if ((Math.abs(verticalOffset) >= appBarLayout.getTotalScrollRange())) {
            if (mCurrentState != State.COLLAPSED) {
                onAppBarStateChanged(appBarLayout, State.COLLAPSED);
            }
            mCurrentState = State.COLLAPSED;
        } else {
            if (mCurrentState != State.IDLE) {
                onAppBarStateChanged(appBarLayout, State.IDLE);
            }
            mCurrentState = State.IDLE;
        }
    }

    public abstract void onAppBarStateChanged(AppBarLayout appBarLayout, State state);
}
