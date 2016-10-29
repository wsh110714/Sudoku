package com.example.wsh.sudoku.Fragment;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

import com.example.wsh.sudoku.R;
import com.example.wsh.sudoku.util.Mylog;


/**
 * Created by wsh on 16-6-28.
 * 用于数组键盘输入，体验不好，作废
 */
@Deprecated
public class KeyBoardDialogFragment extends DialogFragment {
    private static final String TAG = "KeyBoardDialogFragment";
    public static final String EXTRA_NUMBER = "com.example.wsh.soduku.KeyBoardDialogFragment.Number";
    public static final String EXTRA_POINT_X = "POINT_X";
    public static final String EXTRA_POINT_Y = "POINT_Y";

    private int[] btnIds = {
            R.id.btn1, R.id.btn2, R.id.btn3,
            R.id.btn4, R.id.btn5, R.id.btn6,
            R.id.btn7, R.id.btn8, R.id.btn9,
    };


    public static KeyBoardDialogFragment newInstance(int x, int y) {
        Bundle args = new Bundle();
        args.putInt(EXTRA_POINT_X, x);
        args.putInt(EXTRA_POINT_Y, y);
        
        KeyBoardDialogFragment fragment = new KeyBoardDialogFragment();
        fragment.setArguments(args);
        return fragment;
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.layout_keyboard_popup_window, null);

        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int i = Integer.parseInt( ((Button)v).getText().toString() );
                sendResultToTargetFragment(Activity.RESULT_OK, i);
                dismiss();
            }
        };

        for(int i = 0; i < btnIds.length; i++) {
            Button btn = (Button) v.findViewById(btnIds[i]);
            btn.setOnClickListener(listener);
        }

        Button clearBtn = (Button) v.findViewById(R.id.clearBtn);
        clearBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendResultToTargetFragment(Activity.RESULT_OK, 0);
                dismiss();
            }
        });

        return v;
    }



    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        return dialog;
    }



    // 由于是dialogFragment,是为了给其他Fragment输入数字的，关系比较紧密
    // 所以直接使用mTarget来发送数据，省去了通讯宿主activity的麻烦
    private void sendResultToTargetFragment(int resultCode, int num) {
        if (getTargetFragment() == null)
            return;

        Intent i = new Intent();
        i.putExtra(EXTRA_NUMBER, num);

        getTargetFragment().onActivityResult(getTargetRequestCode(), resultCode, i);
    }


    @Override
    public void onStop() {
        Mylog.d(TAG, "onStop()");
        super.onStop();
    }

    @Override
    public void onResume() {


        DisplayMetrics dm = getResources().getDisplayMetrics();
        int screenW = dm.widthPixels;
        int screenH = dm.heightPixels;

        int min = screenW < screenH ? screenW : screenH;

        Window window = getDialog().getWindow();
        window.setLayout(min/100*50, min/100*55);

        WindowManager.LayoutParams lp = window.getAttributes();
        //lp.alpha = 1.0f;
        lp.dimAmount = 0f;
        lp.x = getArguments().getInt(EXTRA_POINT_X, 0);
        lp.y = getArguments().getInt(EXTRA_POINT_Y, 0);
        window.setAttributes(lp);

        window.setGravity(Gravity.TOP | Gravity.LEFT);


        super.onResume();
    }
}
