package com.example.wsh.sudoku.Fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;

import com.example.wsh.sudoku.util.Mylog;

/**
 * Created by wsh on 16-7-23.
 */
public class BaseFragment extends Fragment {
    private static final String TAG = "BaseFragment";
    private static final String STATE_IS_HIDDEN = "STATE_IS_HIDDEN";

    public void updateUIDataFromArguments(final Bundle args) {
        /**
         * do something for hide changed to show
         * you can update ui, or update some data
         */
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            boolean isHidden = savedInstanceState.getBoolean(STATE_IS_HIDDEN);
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            if (isHidden) {
                ft.hide(this);
            }
            ft.commit();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        Mylog.d(TAG, "onSaveInstanceState()");
        super.onSaveInstanceState(outState);
        outState.putBoolean(STATE_IS_HIDDEN, isHidden());
    }

    @Override
    public void onResume() {
        Mylog.d(TAG, "onResume()");
        super.onResume();
        if (!isHidden()) {
            setTitle();
        }
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            setTitle();
        }
    }

    protected void setTitle() {
    }
}
