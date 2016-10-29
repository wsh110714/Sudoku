package com.example.wsh.sudoku.Fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import com.example.wsh.sudoku.Fragment.BaseFragment;

/**
 * Created by wsh on 16-7-19.
 */
public interface FragmentCallback {
    void toggleFragment(@Nullable BaseFragment from, @Nullable Bundle toArgs, String toTag);
    void setToolbarTitle(String title);
}


