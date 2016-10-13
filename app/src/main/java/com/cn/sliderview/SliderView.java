package com.cn.sliderview;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.nineoldandroids.animation.ObjectAnimator;
import com.nineoldandroids.view.ViewHelper;

/**
 * 主播功能的滑块自定义 View
 */
public class SliderView extends LinearLayout {
    /**
     * 动画持续时间
     */
    private static final int ANIMATION_DURATION_TIME = 350;
    /**
     * 进入触摸滑动模式
     */
    private boolean touchState;
    /**
     * 暂停录音模式
     */
    private boolean pauseState;
    /**
     * 用于检测当前点击的是不是控制开关
     */
    private boolean selectView;
    /**
     * 进入录音模式
     */
    private boolean isLiveState;
    /**
     * 静音模式
     */
    private boolean isMuteState;

    /**
     * 禁止状态
     */
    private boolean forbidState = false;
    /**
     * 中间区域
     */
    private float mCenterPosition;
    private float mCurrentTouchX;
    private float mEndTouchX;
    private float mWidth;

    private RecordImageView recViewIV;
    private ImageView stopPlayIV;
    private ImageView muteIV;
    private TextView recTV;

    public SliderView(Context context) {
        super(context);
    }

    public SliderView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        init();
    }

    private void init() {
        recViewIV = (RecordImageView) findViewById(R.id.recIV);
        stopPlayIV = (ImageView) findViewById(R.id.stopPlayIV);
        muteIV = (ImageView) findViewById(R.id.muteIV);
        recTV = (TextView) findViewById(R.id.recText);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return true;
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //getX()是表示Widget相对于自身左上角的x坐标
        //getRawX()是表示相对于屏幕左上角的x坐标值
        float touchX = event.getX();
        float touchY = event.getY();
        mWidth = this.getWidth();
        float x = touchX;
        int btWidth = recViewIV.getWidth();
        // view 的宽度 的一半，用于计算坐标
        int radiusWidth = btWidth / 2;
        int leftLimitPosition = radiusWidth;
        float positionCenter = mWidth / 2;
        float rigthLimitPosition = mWidth - btWidth;
        mCenterPosition = mWidth / 2 - radiusWidth;

        if (!forbidState) {

            switch (event.getAction()) {

                case MotionEvent.ACTION_DOWN:
                    //判断点击的区域
                    boolean state1 = pointInView(recViewIV, touchX, touchY);
                    if (state1) {
                        selectView = true;
                    }
                    touchState = true;
                    break;

                case MotionEvent.ACTION_MOVE:
                    mCurrentTouchX = touchX;
                    if (isLiveState && !isMuteState) {
                        if (mCurrentTouchX < positionCenter) { //左滑动
                            muteIV.setVisibility(GONE);
                            stopPlayIV.setVisibility(VISIBLE);
                            recTV.setGravity(Gravity.END);
                            recTV.setText(R.string.slider_left_scroll_stop_live);
                        } else if (mCurrentTouchX > positionCenter) { //右滑动
                            muteIV.setVisibility(VISIBLE);
                            stopPlayIV.setVisibility(GONE);
                            recTV.setGravity(Gravity.START);
                            recTV.setText(R.string.slider_right_scroll_mute);
                        }
                    } else if (isMuteState) {
                        if (mCurrentTouchX < positionCenter) { //左滑动
                            muteIV.setVisibility(GONE);
                            stopPlayIV.setVisibility(VISIBLE);
                            recTV.setGravity(Gravity.END);
                            recTV.setText(R.string.slider_left_scroll_stop_live);
                        } else if (mCurrentTouchX > positionCenter) { //右滑动
                            muteIV.setVisibility(VISIBLE);
                            stopPlayIV.setVisibility(GONE);
                            recTV.setGravity(Gravity.START);
                            recTV.setText(R.string.slider_right_scroll_cancel_mute);
                        }
                    }

                    if (touchState) {
                        if (mCurrentTouchX < 0 || mCurrentTouchX > mWidth) {
                            touchState = false;
                            return true;
                        }
                        if (selectView) {
                            float moveX = mCurrentTouchX - radiusWidth;
                            if (moveX <= 0)
                                moveX = 0;
                            if (moveX > rigthLimitPosition)
                                moveX = rigthLimitPosition;
                            ViewHelper.setTranslationX(recViewIV, moveX);
                        }

                    } else {
                        return super.onTouchEvent(event);
                    }

                    break;

                case MotionEvent.ACTION_UP:

                    if (isLiveState || isMuteState) {
                        stopPlayIV.setVisibility(VISIBLE);
                        muteIV.setVisibility(VISIBLE);
                    }

                    if (!isLiveState && !isMuteState) {
                        recTV.setGravity(Gravity.CENTER);
                        recTV.setText(R.string.podcast_scroll_warning_text);
                    } else {
                        recTV.setGravity(Gravity.CENTER);
                        recTV.setText("");
                    }

                    mEndTouchX = x;

                    float translationX = mCenterPosition;

                    touchState = false;

                    // 开始直播功能
                    if (!isLiveState && mEndTouchX > mWidth - radiusWidth) {
                        setStart();
                        return true;
                    } else if (!isLiveState) {
                        //这个坐标点 是 该View 里面的坐标点，不能和屏幕的坐标点结合
                        translationX = 0;
                    }

                    float moveX = mEndTouchX;

                    if (isLiveState && moveX <= radiusWidth) {
                        setStop();
                        return true;
                    } else if (isLiveState && moveX >= mWidth) {
                        translationX = mCenterPosition;
                        if (pauseState) {
                            setResume(translationX);
                        } else {
                            setPause(translationX);
                        }
                        return true;
                    } else if (isLiveState && moveX > leftLimitPosition && moveX <=
                            mCenterPosition) {
                        translationX = mCenterPosition;
                    } else if (isLiveState && moveX > mCenterPosition && moveX < rigthLimitPosition)
                        translationX = mCenterPosition;

                    animatorStart(translationX);

                    break;
            }
        }
        return true;
    }

    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        super.onWindowFocusChanged(hasWindowFocus);
        showRecordAnimation();
    }

    /**
     * 显示录音的动画效果
     */
    private void showRecordAnimation() {
        if (isLiveState) {
            recViewIV.setBackgroundResource(R.drawable.podcast_record_animation);
            AnimationDrawable animationDrawable = (AnimationDrawable) recViewIV.getBackground();
            //在动画start()之前要先stop()，不然在第一次动画之后会停在最后一帧，这样动画就只会触发一次
            animationDrawable.stop();
            animationDrawable.start();
        }
    }

    public void setStart() {
        isLiveState = true;
        isMuteState = false;
        recTV.setText("");
        recViewIV.startAnimation();
        muteIV.setVisibility(VISIBLE);
        stopPlayIV.setVisibility(VISIBLE);
        animatorStart(mCenterPosition);
        showRecordAnimation();
    }

    private void setResume(float translationX) {
        if (isLiveState) {
            recViewIV.startAnimation();
        } else {
            recViewIV.setImageResource(R.drawable.ic_podcast_rec_default);
        }
        muteIV.setImageResource(R.drawable.ic_podcast_mute_default);
        stopPlayIV.setVisibility(VISIBLE);
        muteIV.setVisibility(VISIBLE);
        recTV.setText("");
        pauseState = false;
        animatorStart(translationX); //加快动画速度
    }

    private void setPause(float translationX) {
        stopPlayIV.setVisibility(VISIBLE);
        muteIV.setVisibility(VISIBLE);
        muteIV.setImageResource(R.drawable.ic_podcast_mute_rec);
        recViewIV.stopAnimation();
        recViewIV.setIsShowAnimation(false);
        recViewIV.setImageResource(R.drawable.ic_podcast_rec_mute);
        recTV.setText("");
        pauseState = true;
        animatorStart(translationX); //加快动画速度
    }

    public void setStop() {
        float translationX;
        recTV.setText(R.string.podcast_scroll_warning_text);
        recViewIV.setImageResource(R.drawable.ic_podcast_rec_default);
        isLiveState = false;
        stopPlayIV.setVisibility(GONE);
        muteIV.setVisibility(GONE);
        recViewIV.stopAnimation();
        translationX = 0;
        animatorStart(translationX);
    }

    private void animatorStart(float translationX) {
        if (recViewIV != null)
            ObjectAnimator.ofFloat(recViewIV, "translationX", translationX).setDuration(ANIMATION_DURATION_TIME).start();
    }

    /**
     * 判断当前点击的区域 是否属于 view
     *
     * @param view   view
     * @param localX 点击父控件的x坐标
     * @param localY 点击父控件的y坐标
     * @return boolean
     */
    public boolean pointInView(View view, float localX, float localY) {

        return localX >= view.getLeft() && localX <= view.getRight() &&
                localY >= view.getTop() && localY <= view.getBottom();

    }

}
