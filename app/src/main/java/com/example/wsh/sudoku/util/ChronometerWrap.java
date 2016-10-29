package com.example.wsh.sudoku.util;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.SystemClock;
import android.view.View;
import android.widget.Chronometer;
import android.widget.TextView;

/**
 * Created by wsh on 16-7-14.
 */
public class ChronometerWrap {
    private final static int STATE_RUN = 1;
    private final static int STATE_STOP = 2;

    public Chronometer mChronometer;

    private int mState = STATE_STOP;

    private long mBaseTime;

    private long mRunTime = 0; //到上次暂停时，共运行的时间;

    public ChronometerWrap(Chronometer chronometer) {
        this.mChronometer = chronometer;
    }

    public void setupChronometer(Chronometer chronometer) {
        this.mChronometer = chronometer;
        mChronometer.setBase(mBaseTime);
    }

    private void setBaseTime(long time) {
        mBaseTime = time;
        mChronometer.setBase(time);
    }

    /**
     * 总是一个新的开始
     */
    public void newStart() {
        mChronometer.stop();
        mRunTime = 0;
        start();
    }

    /**
     * 只有调用了stop过后，start才表示重新开始; 否则表示继续上一次
     * 每次移动时间基准线
     *      1. 记录上次开始到暂停计时器运行的时间，将基准线移动到 （现在实时 - 上次运行时间）
     *          相当于重新设定现在的实时为基准线计时，不过需要设定基准线的时候将原来运行的时间也包括进去
     *          上次运行的时间包括所有前面间隔运行的时间，这个runtime是累计的
     */
    public void start() {
        if (mState != STATE_RUN) {
            mChronometer.setBase(SystemClock.elapsedRealtime() - mRunTime);
            mChronometer.start();
            mState = STATE_RUN;
        }
    }

    /**
     * 停止过后，下一次start重新开始
     */
    public void stop() {
        if (mState != STATE_STOP) {
            mRunTime = SystemClock.elapsedRealtime() - mChronometer.getBase();
            mChronometer.stop();
            mState = STATE_STOP;
        }
    }

    /**
     * 获取文本
     */
    public String getText() {
        return mChronometer.getText().toString();
    }

    /**
     * 返回现在总共运行的时间
     * @return
     */
    public long getTime() {
        return SystemClock.elapsedRealtime() - mChronometer.getBase();
    }

    public void init(int escapedTime) {
        setBaseTime(SystemClock.elapsedRealtime() - escapedTime);
        mChronometer.start();
        mChronometer.stop();
        mState = STATE_STOP;
        mRunTime = escapedTime;
    }
}
