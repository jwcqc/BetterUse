package me.hyman.betteruse.ui.view;

import android.content.Context;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.view.MotionEvent;

import me.hyman.betteruse.ui.activity.basic.BaseAppCompatActivity;

public class MyToolbar extends Toolbar {

    private long lastClickTime = 0;

    public MyToolbar(Context context) {
        super(context);
    }

    public MyToolbar(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MyToolbar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        boolean handler = super.onTouchEvent(ev);

        if(ev.getAction() == MotionEvent.ACTION_UP) {
            if(lastClickTime != 0) {
                if(System.currentTimeMillis() - lastClickTime <= 500) {
                    BaseAppCompatActivity activity = BaseAppCompatActivity.getRunningActivity();
                    if(activity != null && activity instanceof onToolbarDoubleClick) {
                        ((onToolbarDoubleClick)activity).onToolbarDoubleClick();
                    }
                }
            }

            lastClickTime = System.currentTimeMillis();
        }

        return handler;
    }

    public interface onToolbarDoubleClick {
        boolean onToolbarDoubleClick();
    }
}
