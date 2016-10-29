package com.example.wsh.SudokuGenerator;

import android.os.AsyncTask;

import com.example.wsh.sudoku.model.SudokuGenerator;

/**
 * Created by wsh on 16-7-10.
 * 用于执行后台任务，主要是数据库任务和数独生成任务
 */
public class BackGroundTask extends AsyncTask<SudokuGenerator.MinState, Void, SudokuGenerator.MinState> {
    private static final String TAG = "BackGroundTask";

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected SudokuGenerator.MinState doInBackground(SudokuGenerator.MinState... params) {
        return null;
    }

    @Override
    protected void onProgressUpdate(Void... values) {
        super.onProgressUpdate(values);
    }

    @Override
    protected void onPostExecute(SudokuGenerator.MinState state) {
        if (mListener != null && state != null) {
            mListener.onPostResult(state);
        }
    }

    public interface Listener {
        void onPostResult(SudokuGenerator.MinState state);
    }

    Listener mListener;

    public void setListener(Listener listener) {
        this.mListener = listener;
    }

}
