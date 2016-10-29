package com.example.wsh.sudoku.customView;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ToggleButton;

import com.example.wsh.sudoku.R;
import com.example.wsh.sudoku.model.SudokuGenerator;
import com.example.wsh.sudoku.util.Mylog;


/**
 * Created by wsh on 16-7-20.
 */
public class KeyboardView extends LinearLayout {
    private static final String TAG = "KeyboardView";

    private int mSize = SudokuGenerator.SIZE_NINE;

    private static int[] ids = {R.id.clearBtn, R.id.editBtn,
            R.id.btn1, R.id.btn2, R.id.btn3, R.id.btn4, R.id.btn5,
            R.id.btn6, R.id.btn7, R.id.btn8, R.id.btn9};

    private ToggleButton[] mToggleButtons;
    private ToggleButton mEditBtn;

    public interface ClickListener {
        void onNumClick(int num);
        void onClearClick();
        void onEndEditMode();
    }

    private ClickListener mClickListener;




    public KeyboardView(Context context) {
        super(context);
        init(context);
    }

    public KeyboardView(Context context, int sizeType) {
        super(context);
        this.mSize = sizeType;
        init(context);
    }

    public KeyboardView(Context context, AttributeSet attr) {
        super(context, attr);
        TypedArray ta = context.obtainStyledAttributes(attr, R.styleable.SudokuView);
        mSize = ta.getInt(R.styleable.SudokuView_size, SudokuGenerator.SIZE_NINE);
        ta.recycle();
        init(context);
    }

    private void init(Context context) {
        Mylog.d(TAG, "init()");

        if (!isInEditMode()) {
            switch (mSize) {
                case SudokuGenerator.SIZE_EIGHT:
                    LayoutInflater.from(context).inflate(R.layout.layout_keyboard_eight, this, true);
                    break;
                case SudokuGenerator.SIZE_SEVEN:
                    LayoutInflater.from(context).inflate(R.layout.layout_keyboard_seven, this, true);
                    break;
                case SudokuGenerator.SIZE_SIX:
                    LayoutInflater.from(context).inflate(R.layout.layout_keyboard_six, this, true);
                    break;
                case SudokuGenerator.SIZE_FIVE:
                    LayoutInflater.from(context).inflate(R.layout.layout_keyboard_five, this, true);
                    break;
                case SudokuGenerator.SIZE_FOUR:
                    LayoutInflater.from(context).inflate(R.layout.layout_keyboard_four, this, true);
                    break;
                default:
                    LayoutInflater.from(context).inflate(R.layout.layout_keyboard_nine, this, true);
                    break;
            }
        }

        setListener();
    }

    private void setListener() {
        mToggleButtons = new ToggleButton[mSize+2];
        for (int i = 0; i < mToggleButtons.length; i++) {
            mToggleButtons[i] = (ToggleButton) findViewById(ids[i]);
        }

        //单独给mEditBtn赋值一个引用
        mEditBtn = (ToggleButton) findViewById(R.id.editBtn);

        //为button设置监听函数
        OnClickListener l = new OnClickListener() {
            @Override
            public void onClick(View v) {
                Mylog.d(TAG, "onClick()");
                ToggleButton btn = (ToggleButton) v;

                switch (v.getId()) {
                    case R.id.clearBtn:
                        if (mClickListener != null) {
                            mClickListener.onClearClick();
                        }
                        break;
                    case R.id.editBtn:
                        if (mClickListener != null && !btn.isChecked()) {
                            mClickListener.onEndEditMode();
                        }
                        break;
                    default:
                        if (mClickListener != null) {
                            int i = Integer.parseInt(btn.getText().toString());
                            mClickListener.onNumClick(i);
                        }
                        break;
                }
            }
        };

        for (ToggleButton btn : mToggleButtons) {
            btn.setOnClickListener(l);
        }
    }

    public boolean isEditMode() {
        return mEditBtn.isChecked();
    }

    public void setClickListener(ClickListener clickListener) {
        mClickListener = clickListener;
    }

}
