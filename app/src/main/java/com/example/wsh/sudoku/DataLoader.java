package com.example.wsh.sudoku;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;

import com.example.wsh.sudoku.util.Mylog;

/**
 * Created by wsh on 16-7-15.
 */
public abstract class DataLoader<D> extends AsyncTaskLoader<D> {

    private final static String TAG = "DataLoader";

    private D mData;

    public DataLoader(Context context) {
        super(context);
    }

    @Override
    protected void onStartLoading() {
        Mylog.d(TAG, "onStartLoading");
        if (mData != null) {
            deliverResult(mData);
        } else {
            forceLoad();
        }
    }

    @Override
    public void deliverResult(D data) {
        Mylog.d(TAG, "deliverResult");
        mData = data;
        if (isStarted()) {
            super.deliverResult(data);
        }
    }
}
