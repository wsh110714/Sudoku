package com.example.wsh.sudoku.customView;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.support.v4.content.ContextCompat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.ToggleButton;

import com.example.wsh.sudoku.R;
import com.example.wsh.sudoku.util.Mylog;


/**
 * Created by wsh on 16-7-7.
 * 自定义的popupWindow，用于给数独谜题做键盘输入
 */
public class KeyBoardPopupWindow extends PopupWindow {
    private static final String TAG = "KeyBoardPopupWindow";
    private Context mContext;

    /**
     * 包裹的自定义布局view
     */
    private View mContentView;

    /**
     * window宽度，高度
     */
    private int mMyWidth;
    private int mMyHeight;

    /**
     * 窗口是否固定不消失
     * true: 一旦出现，固定不消失
     * false： 出现后，根据键盘点击设置，或者外部事件拦截设置决定是否消失
     */
    private boolean mWindowFixed = false;

    /**
     * 是否四编辑模式，如果是则键盘内部点击事件发生后，window不消失
     * true: 是编辑模式，键盘内部时间发生后，window消失
     * false： 不是编辑模式，键盘内部时间发生后，window不消失
     */
    private boolean mEditMode = false;

    /**
     * 事件监听器，包括内部事件和外部事件一些特殊情况需要监听
     */
    private Listener mListener;

    /**
     * 自定义布局内的数字button，不保存button0的id，这个id暂时只是布局占位需要，没有事件
     */
    private final int[] mBtnIds = {
            R.id.btn1, R.id.btn2, R.id.btn3, R.id.btn4, R.id.btn5,
            R.id.btn6, R.id.btn7, R.id.btn8, R.id.btn9,
            R.id.clearBtn, R.id.fixedBtn, R.id.editBtn,
    };




    public KeyBoardPopupWindow(Context context) {
        this.mContext = context;
        initPopupWindow();
    }

    public int getMyWidth() {
        return mMyWidth;
    }

    public int getMyHeight() {
        return mMyHeight;
    }

    public void setListener(Listener listener) {
        mListener = listener;
    }

    private void setWindowFixed(Boolean windowFixed) {
        mWindowFixed = windowFixed;
    }

    private void setEditMode(boolean editMode) {
        mEditMode = editMode;
    }

    public boolean isEditMode() {
        return mEditMode;
    }

    /**
     * 显示PopupWindow,相对于指定View内部指定的位置
     * @param view 指定依附的view
     * @param gravity view内部的位置，例如 Gravity.END | Gravity.BOTTOM
     * @param HorizontalIn true： 相对于view水平方向的内部，false: 相对于view水平的外部
     * @param VerticalIn true: 相对于View垂直方向的内部， false：相对于View垂直方向的外部
     */
    public void showAtCustomerLocation(View view, int gravity,
                                       boolean HorizontalIn, boolean VerticalIn) {
        int[] location = new int[2];
        view.getLocationOnScreen(location);

        int offX = 0;
        int offY = 0;


        //水平方向位置
        switch (gravity & Gravity.HORIZONTAL_GRAVITY_MASK) {
            case Gravity.CENTER_HORIZONTAL:
                offX = location[0] + view.getWidth()/2 - getMyWidth()/2;
                break;
            case Gravity.END:
            case Gravity.RIGHT:
                if (HorizontalIn) {
                    offX = location[0] + view.getWidth() - getMyWidth();
                } else {
                    offX = location[0] + view.getWidth();
                }
                break;
            case Gravity.START:
            case Gravity.LEFT:
            default:
                if (HorizontalIn) {
                    offX = location[0];
                } else {
                    offX = location[0] - getMyWidth();
                }
                break;
        }

        //垂直方向位置
        switch (gravity & Gravity.VERTICAL_GRAVITY_MASK) {
            case Gravity.CENTER_VERTICAL:
                offY = location[1] + view.getHeight()/2 - getMyHeight()/2;
                break;
            case Gravity.BOTTOM:
                if (VerticalIn) {
                    offY = location[1] + view.getHeight() - getMyHeight();
                } else {
                    offY = location[1] + view.getHeight();
                }
                break;
            case Gravity.TOP:
            default:
                if (VerticalIn) {
                    offY = location[1];
                } else {
                    offY = location[1] - getMyHeight();
                }
                break;
        }
        showAtLocation(view, Gravity.NO_GRAVITY, offX, offY);
    }

    /**
     * 定义自己的PopupWindow布局和属性
     */
    private void initPopupWindow() {
        LayoutInflater inflater = LayoutInflater.from(mContext);

        mContentView = inflater.inflate(R.layout.layout_keyboard_popup_window, null);

        setContentView(mContentView);

        setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
        setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);

        //true有焦点，则点击边界内正常接收事件，事件后窗口没有dismiss
        //      点击边界外也可接收到事件，接收到ACTION_DOWN事件后窗口dismiss，事件不再传递。
        //false无焦点,则点击边界内可正常接收事件，事件后窗口没有dismiss
        //      点击边界外只能接收到ACTION_OUTSIDE事件，事件结束后窗口自动dismiss，外部其他事件可传递。
        setFocusable(false);

        setTouchable(true);
        setOutsideTouchable(true);
        setBackgroundDrawable(new ColorDrawable(
                        ContextCompat.getColor(mContext, R.color.white_alphaA0)));

        setAnimationStyle(Animation.RELATIVE_TO_PARENT);

        //强制绘制，获取高度宽度
        mandatoryDraw();

        //提前拦截事件处理
        setTouchInterceptor(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_OUTSIDE
                        && NotDismissedWhenOutEvent(event)) {
                        return true; //拦截了事件，父View就不会处理，窗体也就不会消失了
                }
                return false;
            }
        });

        //为button设置监听函数
        View.OnClickListener l = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ToggleButton btn = (ToggleButton) v;

                switch (v.getId()) {
                    case R.id.editBtn:
                        setEditMode(btn.isChecked());
                        return;
                    case R.id.fixedBtn:
                        setWindowFixed(btn.isChecked());
                        return;
                    case R.id.clearBtn:
                        if (mListener != null) {
                            mListener.onClearClick();
                        }
                        break;
                    default:
                        if (mListener != null) {
                            int i = Integer.parseInt(btn.getText().toString());
                            mListener.onNumClick(i);
                        }
                        break;
                }

                if (DismissedWhenInEvent()) {
                    Mylog.d(TAG, "ondClick() call dismiss()");
                    dismiss();
                }
            }
        };

        for (int id: mBtnIds) {
            Button btn = (Button) mContentView.findViewById(id);
            btn.setOnClickListener(l);
        }
    }

    /**
     * popupWindow外部事件ACTION_OUTSIDE发生时，窗体会自动dismiss
     * 这里加一个条件判断，决定外部事件发生是否dismiss
     * @param event 外部事件
     * @return true： 不dismiss; false: dismiss
     */
    private boolean NotDismissedWhenOutEvent(MotionEvent event) {
        //窗体固定肯定不dismiss，返回true；不固定则回调
        if (mWindowFixed) {
            return true;
        }

        //外部事件发生时，外部要求这个事件发生窗体不消失
        return mListener != null && mListener.notDismissedWhenOutSideEventOccur(event);
    }

    /**
     * popupWindow内部事件发生时，窗体默认不dismiss
     * 这里加一个条件判断，决定内部事件发生时是否dismiss，例如window上的button点击事件
     * @return true: dismisss; false：not dismisss
     */
    private boolean DismissedWhenInEvent() {
        //1. 窗口固定，则肯定返回false
        //2. 如果窗口不固定，则看是否编辑模式，如果编辑模式则返回false
        //mWindowFixed优先级大
        if (mWindowFixed || mEditMode) {
            return false;
        }
        return true;
    }

    /**
     * 测量contentView
     */
    private void mandatoryDraw() {
        mContentView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);

        //强制刷新后拿到PopupWindow的宽高
        mMyWidth = mContentView.getMeasuredWidth();
        mMyHeight = mContentView.getMeasuredHeight();
    }

    /**
     * 按钮监听接口
     */
    public interface Listener {
        /**
         * 普通模式下的button的click事件发生后，回调此函数
         * @param num 点击的number
         */
        void onNumClick(int num);

        /**
         * 普通模式，清除按钮点击回调
         */
        void onClearClick();

        /**
         * 外部事件发生时回调，给外部一个机会决定是否这个事件发生后窗体需要消失
         * 也就是说在窗口不固定的情况下， 外部特殊事件窗口也可以不消失
         * @param event window发生的外部事件
         * @return true： 不消失
         */
        boolean notDismissedWhenOutSideEventOccur(MotionEvent event);
    }
}
