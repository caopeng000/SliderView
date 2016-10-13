package com.cn.sliderview;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.util.AttributeSet;
import android.widget.ImageView;

public class RecordImageView extends ImageView {

    private boolean isShowAnimation = false;
    private AnimationDrawable animationDrawable;

    public RecordImageView(Context context) {
        super(context);
    }


    public RecordImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public RecordImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        super.onWindowFocusChanged(hasWindowFocus);
        if (isShowAnimation)
            startAnimation();
    }

    public void startAnimation() {
        setImageResource(R.drawable.podcast_animation);
        animationDrawable = (AnimationDrawable) getDrawable();
        animationDrawable.start();
        isShowAnimation = true;
    }

    public void stopAnimation() {
        if (animationDrawable != null && animationDrawable.isRunning()) {
            animationDrawable.stop();
            isShowAnimation = false;
        }
    }

    public boolean isShowAnimation() {
        return isShowAnimation;
    }

    public void setIsShowAnimation(boolean isShowAnimation) {
        this.isShowAnimation = isShowAnimation;
    }
}
