package com.example.wsh.sudoku.customView;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import com.example.wsh.sudoku.R;
import com.example.wsh.sudoku.model.IrregularGenerator;
import com.example.wsh.sudoku.model.SudokuGenerator;
import com.example.wsh.sudoku.util.Mylog;
import com.example.wsh.sudoku.util.Util;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;



/**
 * Created by wsh on 16-6-27.
 * 自定义的数独View类
 * 主要功能： 绘制数独界面，填写数字，单元格着色，错误单元格着色，候选小数逻辑
 * 可以独立工作，但没有解题是否正确判断;
 */
public class SudokuView extends View {
    public static final String TAG = "SudokuView";

    /**
     * 单元格宽度，高度
     */
    private float mBoxWidth;
    private float mBoxHeight;

    /**
     * 是否新状态，如果是那么回调一下 ，用于计时
     */
    private boolean isNewState = true;

    /**
     * view状态，是否可以点击
     */
    private boolean mCanClick = true;

    /**
     * 选中的单元格的横坐标和纵坐标
     */
    private int mCurrentX;
    private int mCurrentY;

    /**
     * 表示是否有单元格选中，可以用来控制是否可填入数字，是否显示选中的高亮
     */
    private boolean mSelectedTag = false;

    /**
     * 数独基本类型数据： 模式，大小，额外区域限制类型，锯齿连通二维数据，小宫高度宽度
     */
    private int mMode = SudokuGenerator.MODE_NORMAL;
    private int mSize = SudokuGenerator.SIZE_NINE;
    private int mLimitType = SudokuGenerator.LIMIT_TYPE_NORMAL;
    private int[][] mIrregular;
    private int[] mIrregularColor;
    private int mXSize = 3; //小宫的宽度
    private int mYSize = 3; //小宫的高度

    /**
     * 数独数据，需要从外部数据拷贝过来
     */
    private int[][] mSudokuData = new int[mSize][mSize];

    /**
     * 数据是否只读，也就是初始化的固定数字，不用去外部获取是否只读
     */
    private boolean[][] mReadOnly = new boolean[mSize][mSize];

    /**
     * 存放了数独每个单元格的候选数,这个数据结构数据流分为两部分： 填入， 清除
     * 填入两种方式： 手工填入{@link #setCellCandidateNum(int)}
     * 系统提示一次性全部填入 {@link #switchShowCandidateNumTag()}
     * 清除三种方式： 手工清除 {@link #clearCurrentCellAllCandidate()}
     * 系统帮忙一次性清除 {@link #switchShowCandidateNumTag()}
     * 填写大数的时候自动删除区域内的 {@link #setSelectedCellNum(int)}
     */
    private final SparseArray<HashSet<Integer>> mCandidateSetArray = new SparseArray<>();

    /**
     * 数独九宫格中的可选小数是否显示， true：表示显示可选小数： false：表示关闭小数显示
     */
    private boolean mShowCandidateNumTag = false;

    /**
     * 选中单元格对应的矩形块，着色使用
     */
    //private final Rect mCurrentRect = new Rect();

    /**
     * 需要上色的数字，单元格中所有与此相同数字上色，着色使用
     */
    private int mColorNum = 0;

    /**
     * 错误的单元格集合，需要上色，着色使用
     */
    private HashMap<Point, Integer> mCheckedWrongPoints;

    /**
     * 检测DoubleClick使用
     */
    private GestureDetector mGesture;

    /**
     * 画笔
     */
    private Paint mBackgroundPaint = new Paint(); //背景画笔
    private Paint mLinePaint = new Paint(5); //线条
    private Paint mDarkLinePaint = new Paint(); //粗线条
    private TextPaint mNumPaint = new TextPaint(); //数字
    private TextPaint mReadOnlyNumPaint = new TextPaint(); //只读数字
    private TextPaint mSmallNumPaint = new TextPaint(); //小数字
    private Paint mSelectedPaint = new Paint(); //选中单元格矩形框
    private Paint mSameNumPaint = new Paint(); //相同数字单元格矩形框
    private Paint mCheckWrongPaint = new Paint(); //错误单元格矩形框
    private Paint mWindowPaint = new Paint(); //额外单元格矩形框画笔

    private Paint mDiagonalPaint1 = new Paint(); //对角线画笔
    private Paint mDiagonalPaint2 = new Paint(); //对角线画笔
    //Path path = new Path();

    //额外单元格矩形框画笔
    private Paint mIrregularPaint1 = new Paint();
    private Paint mIrregularPaint2 = new Paint();
    private Paint mIrregularPaint3 = new Paint();
    private Paint mIrregularPaint4 = new Paint();
    private Paint mIrregularPaint5 = new Paint();

    /**
     * 监听器
     */
    private Listener mListener;

    /**
     * 监听器接口
     */
    public interface Listener {
        /**
         * 单击表格内一个单元格时触发回调
         */
        void onClick();

        /**
         * 某单元格数字有变动，在给单元格数字设置数据的时候回调
         *
         * @param x   单元格横坐标
         * @param y   单元格纵坐标
         * @param num 变化后的数字
         */
        void onDataSetChanged(int x, int y, int num);

        /**
         * 当数独谜题九宫格填满时回调
         */
        void onFilled();

        /**
         * 第一此Touch回调，用于计时，只调用一次
         */
        void onFirstClickWhenNewState();
    }





    public SudokuView(Context context) {
        super(context);
        init(context);
    }

    public SudokuView(Context context, int mode, int size, int limitType, @Nullable String irregular) {
        super(context);

        this.mMode = mode;
        this.mSize = size;
        this.mLimitType = limitType;
        init(context);

        attachIrregular(irregular);
    }

    public SudokuView(Context context, SudokuGenerator.MinState state) {
        super(context);

        this.mMode = state.mMode;
        this.mSize = state.mSize;
        this.mLimitType = state.mLimitType;
        attachIrregular(state.mIrregular);
        init(context);
    }

    public SudokuView(Context context, @Nullable AttributeSet attr) {
        super(context, attr);

        TypedArray ta = context.obtainStyledAttributes(attr, R.styleable.SudokuView);
        mMode = ta.getInt(R.styleable.SudokuView_mode, SudokuGenerator.MODE_NORMAL);
        mSize = ta.getInt(R.styleable.SudokuView_size, SudokuGenerator.SIZE_NINE);
        mLimitType = ta.getInt(R.styleable.SudokuView_limitType, SudokuGenerator.LIMIT_TYPE_NORMAL);
        ta.recycle();

        init(context);
    }

    Paint choiceIrregularPaint(int key) {
        switch (key) {
            case 1:
                return mIrregularPaint1;
            case 2:
                return mIrregularPaint2;
            case 3:
                return mIrregularPaint3;
            case 4:
                return mIrregularPaint4;
            default:
                return mIrregularPaint5;

        }
    }

    private void init(Context context) {
        if (mSize == SudokuGenerator.SIZE_FOUR) {
            mXSize = mYSize = 2;
        } else if (mSize == SudokuGenerator.SIZE_SIX) {
            mXSize = 3;
            mYSize = 2;
        } else if (mSize == SudokuGenerator.SIZE_EIGHT) {
            mXSize = 4;
            mYSize = 2;
        } else {
            mXSize = mYSize = 3;
        }

        initCandidateSetMap();

        mBackgroundPaint.setColor(Color.BLACK);
        mBackgroundPaint.setStyle(Paint.Style.STROKE);
        mBackgroundPaint.setStrokeWidth(5);

        mLinePaint.setColor(Color.GRAY);
        mDarkLinePaint.setColor(Color.BLACK);
        mDarkLinePaint.setStrokeWidth(4);

        mNumPaint.setColor(0xff303F9F);
        mNumPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        mNumPaint.setTextAlign(Paint.Align.CENTER);
        mNumPaint.setFakeBoldText(true);

        mReadOnlyNumPaint.setColor(Color.BLACK);
        mReadOnlyNumPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        mReadOnlyNumPaint.setFakeBoldText(true);
        mReadOnlyNumPaint.setTextAlign(Paint.Align.CENTER);

        mSmallNumPaint.setColor(Color.BLACK);
        mSmallNumPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        mSmallNumPaint.setFakeBoldText(true);
        mSmallNumPaint.setTextAlign(Paint.Align.CENTER);
        mSmallNumPaint.setAntiAlias(true);

        mSelectedPaint.setColor(Color.BLACK);
        mSelectedPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        mSelectedPaint.setStrokeWidth(1);
        mSelectedPaint.setAlpha(100);

        mSameNumPaint.setColor(0xff85a71d);
        mSameNumPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        mSameNumPaint.setStrokeWidth(1);

        mCheckWrongPaint.setColor(Color.RED);
        mCheckWrongPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        mCheckWrongPaint.setStrokeWidth(1);

        mWindowPaint.setColor(Color.GRAY);
        mWindowPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        mWindowPaint.setStrokeWidth(1);
        mWindowPaint.setAlpha(100);

        mDiagonalPaint1.setColor(0xffd2b48c);
        mDiagonalPaint1.setStyle(Paint.Style.FILL_AND_STROKE);
        mDiagonalPaint1.setStrokeWidth(1);

        mDiagonalPaint2.setColor(0xffffb6c1);
        mDiagonalPaint2.setStyle(Paint.Style.FILL_AND_STROKE);
        mDiagonalPaint2.setStrokeWidth(1);

        //设置虚线的间隔和点的长度
        //PathEffect effects = new DashPathEffect(new float[]{16,32,16,32},1);
        //mDiagonalPaint.setPathEffect(effects);

        //锯齿宫画笔
        mIrregularPaint1.setColor(0xffe0ffff);
        mIrregularPaint1.setStyle(Paint.Style.FILL_AND_STROKE);
        mIrregularPaint1.setStrokeWidth(1);
        //mIrregularPaint1.setAlpha(150);

        mIrregularPaint2.setColor(0xfffaf0e6);
        mIrregularPaint2.setStyle(Paint.Style.FILL_AND_STROKE);
        mIrregularPaint2.setStrokeWidth(1);
        //mIrregularPaint2.setAlpha(150);

        mIrregularPaint3.setColor(0xffe6e6fa);
        mIrregularPaint3.setStyle(Paint.Style.FILL_AND_STROKE);
        mIrregularPaint3.setStrokeWidth(1);
        //mIrregularPaint3.setAlpha(150);

        mIrregularPaint4.setColor(0xffffdab9);
        mIrregularPaint4.setStyle(Paint.Style.FILL_AND_STROKE);
        mIrregularPaint4.setStrokeWidth(1);
        //mIrregularPaint4.setAlpha(150);

        mIrregularPaint5.setColor(0xffffc0cb);
        mIrregularPaint5.setStyle(Paint.Style.FILL_AND_STROKE);
        mIrregularPaint5.setStrokeWidth(1);
        //mIrregularPaint5.setAlpha(150);


        //监听手势，目前只监听单击双击； 单击选中，双击清除
        mGesture = new GestureDetector(context,
                new GestureDetector.SimpleOnGestureListener() {
                    @Override
                    public boolean onSingleTapUp(MotionEvent e) {
                        int selectedX = (int) (e.getX() / mBoxWidth);
                        int selectedY = (int) (e.getY() / mBoxHeight);

                        if (selectedX < 0 || selectedX > mSize - 1
                                || selectedY < 0 || selectedY > mSize - 1) {
                            return true;
                        }
                        selectCell(selectedX, selectedY);

                        return true;
                    }

                    @Override
                    public boolean onDoubleTap(MotionEvent e) { //单击时候已经选中过此单元格
                        int selectedX = (int) (e.getX() / mBoxWidth);
                        int selectedY = (int) (e.getY() / mBoxHeight);
                        if (selectedX < 0 || selectedX > mSize - 1
                                || selectedY < 0 || selectedY > mSize - 1) {
                            return true;
                        }
                        clearCurrentCellNum();
                        clearCurrentCellAllCandidate();
                        return true;
                    }
                });
    }

    /**
     * 添加锯齿模式的锯齿连通数据
     * @param irregular 锯齿宫连通图二维数据
     */
    public void attachIrregular(String irregular) {
        if (mMode == SudokuGenerator.MODE_IRREGULAR) {
            String irregularStr = irregular;
            if (irregularStr == null) {
                irregularStr = IrregularGenerator.randomGetIrregular(mSize);
                if (irregularStr == null) {
                    throw new NullPointerException("irregular == null");
                }
            }

            if (irregularStr.length() != mSize * mSize) {
                throw new RuntimeException("irregular's length not equal size");
            }

            mIrregular = Util.string2IntArray(irregularStr);
            mIrregularColor = new int[mSize];
            generateIrregularColor();
        }
    }

    public void generateIrregularColor() {
        boolean[][] tmp = new boolean[mSize][mSize];

        for (int i = 0; i < mSize; i++) {
            for (int j = 0; j < mSize; j++) {

                if (j+1 < mSize) {
                    tmp[mIrregular[i][j] - 1][mIrregular[i][j+1] - 1] = true;
                }
                if (j-1 >= 0) {
                    tmp[mIrregular[i][j] - 1][mIrregular[i][j-1] - 1] = true;
                }
                if (i+1 < mSize) {
                    tmp[mIrregular[i][j] - 1][mIrregular[i+1][j] - 1] = true;
                }
                if (i-1 >= 0 ) {
                    tmp[mIrregular[i][j] - 1][mIrregular[i-1][j] - 1] = true;
                }
            }
        }

        for (int i = 0; i < mSize; i++) {

            ArrayList<Integer> colors = new ArrayList<>(5);
            for (int k = 0; k < 5; k++) {
                colors.add(k+1);
            }

            for (int j = 0; j < mSize; j++) {
                if (j != i && tmp[i][j] && mIrregularColor[ j ] != 0) {
                    colors.remove(Integer.valueOf(mIrregularColor[j]));
                }
            }

            mIrregularColor[i] = colors.get(0);
        }
    }

    /**
     * 关联数据
     * @param sudokuStr 外部数据
     * @param puzzle 谜题原始数据，用来指定数字是否只读，可以为空，为空则默认不为零的数据都是只读的
     * @param irregular 锯齿宫连通数据
     * @return true: 数据关联成功
     */
    public boolean attachData(String sudokuStr, @Nullable String puzzle, @Nullable String irregular) {
        if (sudokuStr == null || sudokuStr.length() != mSize*mSize) {
            Mylog.d(TAG, "sudokuStr is invalid");
            return false;
        }
        Util.string2IntArray(sudokuStr, mSudokuData);

        //mReadOnly
        if (puzzle == null || puzzle.length() != mSize*mSize) {
            for (int i = 0; i < mSize; i++) {
                for (int j = 0; j < mSize; j++) {
                    mReadOnly[i][j] = mSudokuData[i][j] != 0;
                }
            }
        } else {
            int[][] tmp = Util.string2IntArray(puzzle);
            for (int i = 0; i < mSize; i++) {
                for (int j = 0; j < mSize; j++) {
                    mReadOnly[i][j] = tmp[i][j] != 0;
                }
            }
        }

        //mIrregular
        attachIrregular(irregular);

        //重绘制
        invalidate();

        return true;
    }

    public boolean attachPuzzle(String puzzle, @Nullable String irregular, @Nullable String playingPuzzle) {
        if (puzzle == null || puzzle.length() != mSize*mSize) {
            Mylog.d(TAG, "puzzle is invalid");
            return false;
        }

        //mReadOnly
        int[][] tmp = Util.string2IntArray(puzzle);
        for (int i = 0; i < mSize; i++) {
            for (int j = 0; j < mSize; j++) {
                mReadOnly[i][j] = tmp[i][j] != 0;
            }
        }

        //mSudokuData
        if (playingPuzzle == null || playingPuzzle.length() != mSize*mSize) {
            Util.string2IntArray(puzzle, mSudokuData);
        } else {
            Util.string2IntArray(playingPuzzle, mSudokuData);
        }

        //mIrregular
        attachIrregular(irregular);

        //重绘制
        invalidate();

        return true;
    }

    /**
     * 将view数据和状态初始化
     * 常量以及一些不用
     */
    public void initState() {
        this.isNewState = true;
        this.mCanClick = true;
        this.mCurrentX = 0;
        this.mCurrentY = 0;
        this.mSelectedTag = false;

        //数据
        for (int i = 0; i < mSize; i++) {
            for (int j = 0; j < mSize; j++) {
                mSudokuData[i][j] = 0;
                mReadOnly[i][j] = false;
            }
        }

        //候选数
        initCandidateSetMap();
        this.mShowCandidateNumTag = false;

        //相同着色
        this.mColorNum = 0;

        //错误单元格
        this.mCheckedWrongPoints = null;

        invalidate();
    }

    /**
     * 锁定view状态，不能点击，不能赋值，清除颜色
     */
    public void lockState() {
        setCanClick(false);
        clearColorAndSelected();
    }

    /**
     * 设置是否选中单元格，如果没有选中，则所有对单元格的操作都将无法进行
     * @param selectedTag true: 选中单元格; false: 没有单元格选中
     */
    public void setSelectedTag(boolean selectedTag) {
        mSelectedTag = selectedTag;
        invalidate();
    }

    public boolean isSelected() {
        return mSelectedTag;
    }

    /**
     * 给单元格设值
     *
     * @param num 数字
     */
    private void setCellNum(int x, int y, int num) {
        mSudokuData[x][y] = num;
    }

    /**
     * 给选中单元格填写数字
     *
     * @param num 填写的数字
     */
    public void setSelectedCellNum(int num) {
        if (mSelectedTag && !mReadOnly[mCurrentX][mCurrentY] && num > 0 && num <= mSize) {
            setCellNum(mCurrentX, mCurrentY, num);

            //填写数字的时候，同时更新曾经填写过的候选小数
            removeRegionCandidateByNum(mCurrentX, mCurrentY, num);

            invalidate();

            //由于不是直接使用外部数据，所以需要通知外部数据有变化
            if (mListener != null) {
                mListener.onDataSetChanged(mCurrentX, mCurrentY, num);
                if (isCompleted()) {
                    mListener.onFilled();
                    if (isSolvedByLogic()) { //谜题求解正确
                        Snackbar.make(this, "Well Done", Snackbar.LENGTH_LONG).show();
                    }
                }
            }
        }
    }

    /**
     * 判断数独空格是否填完
     *
     * @return true：填完
     */
    private boolean isCompleted() {
        for (int i = 0; i < mSize; i++) {
            for (int j = 0; j < mSize; j++) {
                if (mSudokuData[i][j] <= 0) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * 清楚选中的单元格数字
     */
    public void clearCurrentCellNum() {
        if (mSelectedTag && !mReadOnly[mCurrentX][mCurrentY]) {
            setCellNum(mCurrentX, mCurrentY, 0);

            invalidate();

            //由于不是直接使用外部数据，所以需要通知外部数据有变化
            if (mListener != null) {
                mListener.onDataSetChanged(mCurrentX, mCurrentY, 0);
            }
        }
    }

    /**
     * 给当前单元格填写候选数，如果当前单元格存在此候选数了，则删除
     * @param num 待填入的候选数
     */
    public void setCellCandidateNum(int num) {
        if (!mSelectedTag && num > 0 && num <= mSize) {
            return;
        }

        int index = mCurrentX * mSize + mCurrentY;

        HashSet<Integer> set = mCandidateSetArray.get(index);
        if (set == null) {
            set = new HashSet<>(mSize);
            mCandidateSetArray.put(index, set);
        }

        if (set.contains(num)) {
            set.remove(num);
        } else {
            set.add(num);
        }

        setCellNum(mCurrentX, mCurrentY, 0);
        invalidate();
    }

    /**
     * 清除当前单元格所有的候选数
     */
    public void clearCurrentCellAllCandidate() {
        if (mSelectedTag) {
            clearCellCandidate(mCurrentX, mCurrentY);
            invalidate();
        }
    }

    /**
     * 得到指定单元格的矩形方块区域，用于绘制
     *
     * @param x    单元格在网格中的横坐标
     * @param y    单元格在网格中的纵坐标
     * @param rect 得到的矩形区域
     */
    private Rect getCellRect(int x, int y, Rect rect) {
        rect.set((int) (x * mBoxWidth + 14), (int) (y * mBoxHeight + 14),
                (int) (x * mBoxWidth + mBoxWidth - 14), (int) (y * mBoxHeight + mBoxHeight - 14));
        return rect;
    }

    /**
     * 得到单元格所在行矩形块
     *
     * @param x    单元格横坐标
     * @param y    单元格纵坐标
     * @param rect 得到的矩形块
     */
    private void getRowRect(int x, int y, Rect rect) {
        rect.set(0, (int) (y * mBoxHeight),
                (int) (mSize * mBoxWidth), (int) (y * mBoxHeight + mBoxHeight));
    }

    /**
     * 得到单元格所在列矩形块
     *
     * @param x    单元格横坐标
     * @param y    单元格纵坐标
     * @param rect 得到的矩形块
     */
    private void getLineRect(int x, int y, Rect rect) {
        rect.set((int) (x * mBoxWidth), 0,
                (int) (x * mBoxWidth + mBoxWidth), (int) (mSize * mBoxHeight));
    }

    /**
     * 选中网格中某个单元格，需要发生的事件
     *
     * @param x 单元格横坐标
     * @param y 单元格纵坐标
     */
    private void selectCell(int x, int y) {
        invalidate();

        //给选中单元格坐标赋值
        mCurrentX = Math.min(Math.max(x, 0), mSize - 1);
        mCurrentY = Math.min(Math.max(y, 0), mSize - 1);
        //getCellRect(mCurrentX, mCurrentY, mCurrentRect);

//        if (!mReadOnly[mCurrentX][mCurrentY]) {
//            setSelectedTag(true);
//        } else {
//            setSelectedTag(false);
//        }
        setSelectedTag(true);

        if (mCheckedWrongPoints == null || mCheckedWrongPoints.isEmpty()
                || mCheckedWrongPoints.get(new Point(mCurrentX, mCurrentY)) == null) { //不是错误的，才上色
            if (mSudokuData[mCurrentX][mCurrentY] != 0) { //不是空格才上色
                setColorNum(mSudokuData[mCurrentX][mCurrentY]);
            }
        }

        invalidate();

        //非只读的单元格需要回调监听事件
        if (mListener != null && !mReadOnly[mCurrentX][mCurrentY]) {
            mListener.onClick();
        }
    }

    /**
     * 得到单元格字符串
     *
     * @param x 单元格横坐标
     * @param y 单元格纵坐标
     * @return 单元格字符串
     */
    public String getCellString(int x, int y) {
        if (mSudokuData[x][y] == 0) {
            return String.valueOf("");
        } else {
            return String.valueOf(mSudokuData[x][y]);
        }
    }

    /**
     * 设置所有相同的需要上色的数字
     *
     * @param colorNum [1-9]，待上色的数字
     */
    private void setColorNum(int colorNum) {
        mColorNum = colorNum;
    }

    public void colorSameNum(int colorNum) {
        if (colorNum > 0 && colorNum <= mSize) {
            setColorNum(colorNum);
            invalidate();
        }
    }

    /**
     * 设置错误数字的单元格集合
     *
     * @param points 返回错误单元格集合
     */
    public void setWrongCell(@Nullable HashMap<Point, Integer> points) {
        if (points == null || points.isEmpty()) {
            return;
        }
        mCheckedWrongPoints = points;
        invalidate();
    }

    /**
     * 清楚所有单元格的颜色和选中框
     */
    public void clearColorAndSelected() {
        setSelectedTag(false);
        mColorNum = 0;
        mCheckedWrongPoints = null;
        invalidate();
    }

    /**
     * 事件发生在view内部， view内事件横坐标，纵坐标位置的数据是否只读
     *
     * @param x 相当于：MotionEvent.getX()，不是单元格坐标
     * @param y MotionEvent.getY()，不是单元格坐标
     */
    public boolean isReadOnly(int x, int y) {
        int i = (int) (x / mBoxWidth);
        int j = (int) (y / mBoxHeight);

        //如果坐标超出范围，返回true
        if (i < 0 || i >= mSize || j < 0 || j >= mSize) {
            return true;
        }
        return mReadOnly[i][j];
    }

    /**
     * 输出二维数组
     *
     * @return 返回二维整形数组转换成的字符串
     */
    public String getText() {
        return Util.intArray2String(this.mSudokuData);
    }

    /**
     * 设置点击监监听器
     *
     * @param listener 监听器
     */
    public void setListener(Listener listener) {
        mListener = listener;
    }

    /**
     * 设置是否可点击
     *
     * @param canClick ture：可点击
     */
    private void setCanClick(boolean canClick) {
        mCanClick = canClick;
    }


    //-------------Candidate begin -- 可选小数逻辑功能，View类自带此逻辑判断 -----------------------------
    /**
     * 初始化可选小数的数据结构
     */
    public void initCandidateSetMap() {
        for (int i = 0; i < mSize; i++) {
            for (int j = 0; j < mSize; j++) {
                int index = i * mSize + j;

                HashSet<Integer> set = mCandidateSetArray.get(index);
                if (set == null) {
                    set = new HashSet<>(mSize);
                    mCandidateSetArray.put(index, set);
                } else { //如果已经存在，需要清空
                    set.clear();
                }
            }
        }
    }

    /**
     * 更新所有单元格的可选数
     */
    private void fillCandidateSetMap() {
        for (int i = 0; i < mSize; i++) {
            for (int j = 0; j < mSize; j++) {
                updateCandidateSet(i, j);
            }
        }
    }

    /**
     * 切换此开关，可以决定是否填满可选数
     * @return true: 填满; false: 清空
     */
    public boolean switchShowCandidateNumTag() {
        this.mShowCandidateNumTag = !this.mShowCandidateNumTag;
        if (mShowCandidateNumTag) {
            fillCandidateSetMap();
        } else {
            initCandidateSetMap();
        }
        invalidate();
        return mShowCandidateNumTag;
    }

    /**
     * 删除区域内所有单元格的指定可选小数，并删除此单元格的所有候选数
     * @param row    单元格横坐标
     * @param column 单元格纵坐标
     * @param num 指定数字
     */
    private void removeRegionCandidateByNum(int row, int column, int num) {
        //清除本单元格所有候选数
        clearCellCandidate(row, column);

        //清除区域内所有单元格的指定候选数
        for (int k = 0; k < mSize; k++) {
            //更新列
            removeCellCandidateByNum(row, k, num);

            //更新行
            removeCellCandidateByNum(k, column, num);
        }

        //清除小宫候选数
        if (mMode == SudokuGenerator.MODE_IRREGULAR) {//锯齿宫
            int groupId = mIrregular[row][column];
            for (int i = 0; i < mSize; i++) {
                for (int j = 0; j < mSize; j++) {
                    if (mIrregular[i][j] == groupId) {
                        removeCellCandidateByNum(i, j, num);
                    }
                }
            }
        } else { //规则宫
            int startX = (row/mXSize)*mXSize;
            int startY = (column/mYSize)*mYSize;
            for (int i = startX; i < startX + mXSize; i++) {
                for (int j = startY; j < startY + mYSize; j++) {
                    removeCellCandidateByNum(i, j, num);
                }
            }
        }

        removeLimitTypeRegionCandidateByNum(row, column, num);
    }

    private void removeLimitTypeRegionCandidateByNum(int row, int column, int num) {
        if (mLimitType == SudokuGenerator.LIMIT_TYPE_DIAGONAL) {
            if (row == column) {
                for (int i = 0; i < mSize; i++) {
                    removeCellCandidateByNum(i, i, num);
                }
            } else if (row + column == mSize - 1) {
                for (int i = 0; i < mSize; i++) {
                    removeCellCandidateByNum(i, mSize-1-i, num);
                }
            }
        } else if (mSize == SudokuGenerator.SIZE_NINE && mLimitType == SudokuGenerator.LIMIT_TYPE_WINDOW) {
            int beginX = (row > 0 && row < 4) ? 1 : (row > 4 && row < 8 ? 5 : 0);
            int beginY = (column > 0 && column < 4) ? 1 : (column > 4 && column < 8 ? 5 : 0);

            if (beginX != 0 && beginY != 0) {
                for (int i = beginX; i < beginX + 3; i++) {
                    for (int j = beginY; j < beginY + 3; j++) {
                        removeCellCandidateByNum(i, j, num);
                    }
                }
            }
        } else if (mSize == SudokuGenerator.SIZE_NINE && mLimitType == SudokuGenerator.LIMIT_TYPE_PERCENTAGE) {
            if (row + column == mSize - 1) {
                for (int i = 0; i < mSize; i++) {
                    removeCellCandidateByNum(i, mSize-1-i, num);
                }
            } else {
                int beginX = 0,beginY = 0;
                if (row > 0 && row < 4 && column > 0 && column < 4) {
                    beginX = beginY = 1;
                } else if (row > 4 && row < 8 && column > 4 && column < 8) {
                    beginX = beginY = 5;
                }
                if (beginX != 0) {
                    for (int i = beginX; i < beginX + 3; i++) {
                        for (int j = beginY; j < beginY + 3; j++) {
                            removeCellCandidateByNum(i, j, num);
                        }
                    }
                }
            }
        }
    }

    /**
     * 清除指定单元格的指定候选数
     * @param row    单元格横坐标
     * @param column 单元格纵坐标
     * @param num    指定的候选数
     */
    private void removeCellCandidateByNum(int row, int column, int num) {
        HashSet<Integer> set = mCandidateSetArray.get(row * mSize + column);
        if (set != null) {
            set.remove(num);
        }
    }

    /**
     * 清除指定单元格内所有候选数
     */
    private void clearCellCandidate(int row, int column) {
        HashSet<Integer> set = mCandidateSetArray.get(row * mSize + column);
        if (set != null) {
            set.clear();
        }
    }

    /**
     * 重新计算一单元格的候选数
     * @param row    单元格横坐标
     * @param column 单元格纵坐标
     */
    private void updateCandidateSet(int row, int column) {
        int index = row * mSize + column;

        HashSet<Integer> set = mCandidateSetArray.get(index);
        if (set == null) {
            set = new HashSet<>(mSize);
            mCandidateSetArray.put(index, set);
        }

        //如果这一格已经填入数字，就没有候选数子，清空set，直接返回
        if (mSudokuData[row][column] != 0) {
            set.clear();
            return;
        }

        //先放入所有的候选数，然后有的就删除
        for (int i = 0; i < mSize; i++) {
            set.add(i + 1);
        }

        //单元格还没有填入数字，开始计算可选数，这里有部分单元格重复检测了
        for (int k = 0; k < mSize; k++) {
            //同列出现，删除
            if (mSudokuData[row][k] != 0) {
                set.remove(mSudokuData[row][k]);
            }

            //同行出现，删除
            if (mSudokuData[k][column] != 0) {
                set.remove(mSudokuData[k][column]);
            }
        }

        //小宫内出现
        if (mMode == SudokuGenerator.MODE_IRREGULAR) {//锯齿宫出现
            int groupId = mIrregular[row][column];
            for (int i = 0; i < mSize; i++) {
                for (int j = 0; j < mSize; j++) {
                    if (mIrregular[i][j] == groupId && mSudokuData[i][j] != 0) {
                        set.remove(mSudokuData[i][j]);
                    }
                }
            }
        } else { //规则宫出现
            int startX = (row / mXSize) * mXSize;
            int startY = (column / mYSize) * mYSize;
            for (int i = startX; i < startX + mXSize; i++) {
                for (int j = startY; j < startY + mYSize; j++) {
                    if (mSudokuData[i][j] != 0) {
                        set.remove(mSudokuData[i][j]);
                    }
                }
            }
        }

        //额外区域检测
        if (mLimitType == SudokuGenerator.LIMIT_TYPE_DIAGONAL) { //对角线更新
            if (row == column) {
                for (int i = 0; i < mSize; i++) {
                    if (mSudokuData[i][i] != 0) {
                        set.remove(mSudokuData[i][i]);
                    }
                }
            } else if (row + column == mSize - 1) {
                for (int i = 0; i < mSize; i++) {
                    if (mSudokuData[i][mSize-1-i] != 0) {
                        set.remove(mSudokuData[i][mSize-1-i]);
                    }
                }
            }
        } else if (mSize == SudokuGenerator.SIZE_NINE && mLimitType == SudokuGenerator.LIMIT_TYPE_WINDOW) {
            int beginX = (row > 0 && row < 4) ? 1 : (row > 4 && row < 8 ? 5 : 0);
            int beginY = (column > 0 && column < 4) ? 1 : (column > 4 && column < 8 ? 5 : 0);

            if (beginX != 0 && beginY != 0) {
                for (int i = beginX; i < beginX + 3; i++) {
                    for (int j = beginY; j < beginY + 3; j++) {
                        if (mSudokuData[i][j] != 0) {
                            set.remove(mSudokuData[i][j]);
                        }
                    }
                }
            }
        } else if (mSize == SudokuGenerator.SIZE_NINE && mLimitType == SudokuGenerator.LIMIT_TYPE_PERCENTAGE) {
            if (row + column == mSize - 1) {
                for (int i = 0; i < mSize; i++) {
                    if (mSudokuData[i][mSize-1-i] != 0) {
                        set.remove(mSudokuData[i][mSize-1-i]);
                    }
                }
            } else {
                int beginX = 0,beginY = 0;
                if (row > 0 && row < 4 && column > 0 && column < 4) {
                    beginX = beginY = 1;
                } else if (row > 4 && row < 8 && column > 4 && column < 8) {
                    beginX = beginY = 5;
                }
                if (beginX != 0) {
                    for (int i = beginX; i < beginX + 3; i++) {
                        for (int j = beginY; j < beginY + 3; j++) {
                            if (mSudokuData[i][j] != 0) {
                                set.remove(mSudokuData[i][j]);
                            }
                        }
                    }
                }
            }
        }
    }
    //-------------Candidate end -- 可选数逻辑，View类自带此逻辑判断 ------------------------------------

    //------------- 其他的一些逻辑算法  begin ----------------------------------------------------------------
    /**
     * 判断数独是否正确完成，通过行列小九宫数字都不一样的逻辑算法来判断
     * @return true：数据谜题解决正确
     */
    private boolean isSolvedByLogic() {
        for (int count = 0; count < mSize; count++) {

            boolean[] rowExisting = new boolean[mSize];
            boolean[] columnExisting = new boolean[mSize];
            boolean[] regionExisting = new boolean[mSize];

            for (int i = 0; i < mSize; i++) {
                //一列中所有的数字
                rowExisting[mSudokuData[count][i] - 1] = true;

                //一行中所有的数字
                columnExisting[mSudokuData[i][count] - 1] = true;
            }

            if (mMode == SudokuGenerator.MODE_IRREGULAR) {
                int groupId = count + 1;
                for (int i = 0; i < mSize; i++) {
                    for (int j = 0; j < mSize; j++) {
                        if (mIrregular[i][j] == groupId) {
                            regionExisting[mSudokuData[i][j] - 1] = true;
                        }
                    }
                }
            } else {
                //小宫内数字
                int startRow = (count / mXSize) * mXSize;
                int startColumn = (count % mXSize) * mYSize;
                for (int row = 0; row < startRow + mXSize; row++) {
                    for (int column = 0; column < startColumn + mYSize; column++) {
                        regionExisting[mSudokuData[row][column] - 1] = true;
                    }
                }
            }

            //都变成了true，那么谜题解答成功
            for (int k = 0; k < mSize; k++) {
                if (!rowExisting[k] || !columnExisting[k] || !regionExisting[k]) {
                    Mylog.d(TAG, "isSolvedByLogic() return false");
                    return false;
                }
            }
        }
        return checkLimitType();
    }

    private boolean checkLimitType() {
        if (mLimitType == SudokuGenerator.LIMIT_TYPE_DIAGONAL) { //对角线额外区域
            boolean[] limitExisting1 = new boolean[mSize];
            boolean[] limitExisting2 = new boolean[mSize];
            for (int i = 0; i < mSize; i++) {
                limitExisting1[mSudokuData[i][i]-1] = true;
                limitExisting2[mSudokuData[i][mSize-1-i]-1] = true;
            }

            //都变成了true，那么谜题解答成功
            for (int k = 0; k < mSize; k++) {
                if (!limitExisting1[k] || !limitExisting2[k]) {
                    Mylog.d(TAG, "checkLimitType()， diagonal return false");
                    return false;
                }
            }
        } else if (mSize == SudokuGenerator.SIZE_NINE && mLimitType == SudokuGenerator.LIMIT_TYPE_WINDOW) {
            boolean[] limitExisting1 = new boolean[mSize];
            boolean[] limitExisting2 = new boolean[mSize];
            boolean[] limitExisting3 = new boolean[mSize];
            boolean[] limitExisting4 = new boolean[mSize];

            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 3; j++) {
                    limitExisting1[mSudokuData[i+1][j+1]-1] = true;
                    limitExisting2[mSudokuData[i+5][j+1]-1] = true;
                    limitExisting3[mSudokuData[i+1][j+5]-1] = true;
                    limitExisting4[mSudokuData[i+5][j+5]-1] = true;
                }
            }

            //都变成了true，那么谜题解答成功
            for (int k = 0; k < mSize; k++) {
                if (!limitExisting1[k] || !limitExisting2[k]
                        || !limitExisting3[k] || !limitExisting4[k]) {
                    Mylog.d(TAG, "checkLimitType()， window return false");
                    return false;
                }
            }
        } else if (mSize == SudokuGenerator.SIZE_NINE && mLimitType == SudokuGenerator.LIMIT_TYPE_PERCENTAGE){
            boolean[] limitExisting1 = new boolean[mSize];
            boolean[] limitExisting2 = new boolean[mSize];
            boolean[] limitExisting3 = new boolean[mSize];

            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 3; j++) {
                    limitExisting1[mSudokuData[i+1][j+1]-1] = true;
                    limitExisting2[mSudokuData[i+5][j+5]-1] = true;
                }
            }

            for (int i = 0; i < mSize; i++) {
                limitExisting3[mSudokuData[i][mSize-1-i]-1] = true;
            }

            //都变成了true，那么谜题解答成功
            for (int k = 0; k < mSize; k++) {
                if (!limitExisting1[k] || !limitExisting2[k] || !limitExisting3[k]) {
                    Mylog.d(TAG, "checkLimitType()， percentage return false");
                    return false;
                }
            }
        }


        return true;
    }



    //------------- 其他的一些逻辑算法 end ----------------------------------------------------------------

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        this.mBoxWidth = w / mSize;
        this.mBoxHeight = h / mSize;
        super.onSizeChanged(w, h, oldw, oldh);
    }

    /**
     * 绘制单元格
     */
    protected void drawRect(Canvas canvas, Paint paint, int x1, int y1, int x2, int y2) {
        canvas.drawRect((int) (x1 * mBoxWidth + 1), (int) (y1 * mBoxHeight + 1),
                (int) (x2 * mBoxWidth + mBoxWidth - 1), (int) (y2 * mBoxHeight + mBoxHeight - 1),
                paint);
    }

    protected void drawRect(Canvas canvas, Paint paint, int x1, int y1, int x2, int y2, int off) {
        canvas.drawRect(
                (int) (x1 * mBoxWidth + 2 + off),
                (int) (y1 * mBoxHeight + 1 + off),
                (int) (x2 * mBoxWidth + mBoxWidth - 1 - off),
                (int) (y2 * mBoxHeight + mBoxHeight - 1 - off),
                paint);
    }

    /**
     *绘制单元格的下线条
     */
    protected void drawCellDownLine(Canvas canvas, Paint paint, int x, int y) {
        if (y == mSize-1 || mIrregular[x][y] == mIrregular[x][y+1]) {
            return;
        }

        canvas.drawLine(x*mBoxWidth, (y+1) * mBoxHeight, (x+1)*mBoxWidth, (y+1) * mBoxHeight,
                paint);
    }

    /**
     *绘制单元格的右线条
     */
    protected void drawCellRightLine(Canvas canvas, Paint paint, int x, int y) {
        if (x == mSize-1 || mIrregular[x][y] == mIrregular[x+1][y]) {
            return;
        }

        canvas.drawLine((x+1)*mBoxWidth, y * mBoxHeight, (x+1)*mBoxWidth, (y+1) * mBoxHeight,
                paint);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        //绘制间隔底色
        if (mMode == SudokuGenerator.MODE_NORMAL && mLimitType == SudokuGenerator.LIMIT_TYPE_NORMAL) {
            if (mSize == SudokuGenerator.SIZE_FOUR) {
                drawRect(canvas, mWindowPaint, 2, 0, 3, 1);
                drawRect(canvas, mWindowPaint, 0, 2, 1, 3);
            } else if (mSize == SudokuGenerator.SIZE_SIX) {
                drawRect(canvas, mWindowPaint, 3, 0, 5, 1);
                drawRect(canvas, mWindowPaint, 0, 2, 2, 3);
                drawRect(canvas, mWindowPaint, 3, 4, 5, 5);
            } else if (mSize == SudokuGenerator.SIZE_EIGHT) {
                drawRect(canvas, mWindowPaint, 4, 0, 7, 1);
                drawRect(canvas, mWindowPaint, 0, 2, 3, 3);
                drawRect(canvas, mWindowPaint, 4, 4, 7, 5);
                drawRect(canvas, mWindowPaint, 0, 6, 3, 7);
            } else if (mSize == SudokuGenerator.SIZE_NINE) {
                drawRect(canvas, mWindowPaint, 3, 0, 5, 2);
                drawRect(canvas, mWindowPaint, 0, 3, 2, 5);
                drawRect(canvas, mWindowPaint, 6, 3, 8, 5);
                drawRect(canvas, mWindowPaint, 3, 6, 5, 8);
            }
        }

        //绘制锯齿宫
        if (mMode == SudokuGenerator.MODE_IRREGULAR) {
            for (int i = 0; i < mSize; i++) {
                for (int j = 0; j < mSize; j++) {
                    drawRect(canvas, choiceIrregularPaint(mIrregularColor[mIrregular[i][j]-1]),
                            i, j, i, j);
                }
            }
            for (int i = 0; i < mSize; i++) {
                for (int j = 0; j < mSize; j++) {
                    drawCellDownLine(canvas, mDarkLinePaint, i, j);
                    drawCellRightLine(canvas, mDarkLinePaint, i, j);
                }
            }
        }

        //绘制额外区域
        switch (mLimitType) {
            case SudokuGenerator.LIMIT_TYPE_DIAGONAL:
                if (mSize%2 == 1) {
                    for (int i = 0; i < mSize; i++) {
                        drawRect(canvas, mWindowPaint, i, i, i, i);
                        drawRect(canvas, mWindowPaint, i, mSize - 1 - i, i, mSize - 1 - i);
                    }
                } else {
                    for (int i = 0; i < mSize; i++) {
                        drawRect(canvas, mDiagonalPaint1, i, i, i, i);
                        drawRect(canvas, mDiagonalPaint2, i, mSize - 1 - i, i, mSize - 1 - i);
                    }
                }

                break;
            case SudokuGenerator.LIMIT_TYPE_WINDOW:
                if (mSize == SudokuGenerator.SIZE_NINE) {
                    drawRect(canvas, mWindowPaint, 1, 1, 3, 3);
                    drawRect(canvas, mWindowPaint, 5, 1, 7, 3);
                    drawRect(canvas, mWindowPaint, 1, 5, 3, 7);
                    drawRect(canvas, mWindowPaint, 5, 5, 7, 7);
                }
                break;
            case SudokuGenerator.LIMIT_TYPE_PERCENTAGE:
                if (mSize == SudokuGenerator.SIZE_NINE) {
                    drawRect(canvas, mWindowPaint, 1, 1, 3, 3);
                    drawRect(canvas, mWindowPaint, 5, 5, 7, 7);
                    for (int i = 0; i < mSize; i++) {
                        drawRect(canvas, mWindowPaint, i, mSize - 1 - i, i, mSize - 1 - i);
                    }
                }
                break;
        }

        //绘制数字,x表示单元格横坐标，y表示单元格纵坐标
        mNumPaint.setTextSize(mBoxHeight * 0.75f);
        mReadOnlyNumPaint.setTextSize(mBoxHeight * 0.75f);
        mSmallNumPaint.setTextSize(mBoxHeight * 0.75f / 5 * 2);

        Paint.FontMetrics fontMetrics = mNumPaint.getFontMetrics();
        float offY = (fontMetrics.descent - fontMetrics.ascent) / 2 - fontMetrics.descent;

        Paint.FontMetrics smallFontMetrics = mSmallNumPaint.getFontMetrics();
        float offSY = (smallFontMetrics.descent - smallFontMetrics.ascent) / 2
                - smallFontMetrics.descent;
        float offSX = 0;

        if (mSize == SudokuGenerator.SIZE_SIX) { //六宫格的小数，需要垂直方向下移一点
            offSY += mBoxHeight / 6;
        } else if (mSize == SudokuGenerator.SIZE_FOUR) { //四宫格的小数，需要水平垂直方向偏移
            offSX += mBoxWidth / 6;
            offSY += mBoxHeight / 6;
        }

        for (int x = 0; x < mSize; x++) {
            for (int y = 0; y < mSize; y++) {
                if (mSudokuData[x][y] != 0) {   //有大数字显示数字

                    //给指定的相同数字上色，前面已经过滤了0, 也就是表示空格不上色
                    if (mSudokuData[x][y] == mColorNum) {
                        drawRect(canvas, mSameNumPaint, x, y, x, y, 6);
                    }

                    canvas.drawText(getCellString(x, y),
                            x * mBoxWidth + mBoxWidth / 2,
                            y * mBoxHeight + mBoxHeight / 2 + offY,
                            mReadOnly[x][y] ? mReadOnlyNumPaint : mNumPaint);

                } else {  //没有大数字，显示小数字，没有填写就是空的
                    HashSet<Integer> set = mCandidateSetArray.get(x * mSize + y);
                    if (set != null) {
                        int count = (mSize == SudokuGenerator.SIZE_FOUR) ? 2 : 3;
                        for (Integer k : set) {
                            int xx = (k - 1) % count;
                            int yy = (k - 1) / count;
                            canvas.drawText(String.valueOf(k),
                                    x * mBoxWidth +  mBoxWidth / 3 * xx + mBoxWidth / 6 + offSX,
                                    y * mBoxHeight + mBoxHeight / 3 * yy + mBoxHeight / 6 + offSY,
                                    mSmallNumPaint);
                        }
                    }
                } //end if
            } // end for
        } //end for

        //错误单元格上色
        if (mCheckedWrongPoints != null && !mCheckedWrongPoints.isEmpty()) {
            Iterator<HashMap.Entry<Point, Integer>> it = mCheckedWrongPoints.entrySet().iterator();
            while (it.hasNext()) {
                HashMap.Entry<Point, Integer> entry = it.next();
                Point point = entry.getKey();
                Integer value = entry.getValue();
                if (mSudokuData[point.x][point.y] != value) {//如果这个错误值已经修改过，则在map中删除这条记录
                    it.remove();
                } else { //如果错误没有改变，则继续给此单元格上色，错误标记一直留存，直到颜色被清空或者单元格数字被修改
                    drawRect(canvas, mCheckWrongPaint, point.x, point.y, point.x, point.y, 6);

                    //给此单元格上色的时候，由于颜色不是透明的，需要重新绘制他们的数字
                    canvas.drawText(getCellString(point.x, point.y),
                            point.x * mBoxWidth + mBoxWidth / 2,
                            point.y * mBoxHeight + mBoxHeight / 2 + offY,
                            mReadOnly[point.x][point.y] ? mReadOnlyNumPaint : mNumPaint);
                }
            }
        }

        //绘制选中单元格边框颜色
        if (mSelectedTag) {
//            float radius = mBoxWidth < mBoxHeight ? mBoxWidth/4 : mBoxHeight/4;
//            canvas.drawCircle(mCurrentRect.centerX(),mCurrentRect.centerY(), radius, mSelectedPaint);
//            canvas.drawRect(mCurrentRect, mSelectedPaint);

            drawRect(canvas, mSelectedPaint, mCurrentX, mCurrentY, mCurrentX, mCurrentY, 12);
        }

        //绘制内部线条
        for (int i = 1; i < mSize; i++) {
            //横线用mYSize判定粗线
            canvas.drawLine(0, i * mBoxHeight, mBoxWidth * mSize, i * mBoxHeight,
                    i % mYSize == 0 && mMode == SudokuGenerator.MODE_NORMAL ? mDarkLinePaint : mLinePaint);

            canvas.drawLine(i * mBoxWidth, 0, i * mBoxWidth, mBoxHeight * mSize,
                    i % mXSize == 0 && mMode == SudokuGenerator.MODE_NORMAL ? mDarkLinePaint : mLinePaint);
        }

        //绘制外部矩形框
        canvas.drawRect(3, 3, mBoxWidth*mSize, mBoxHeight*mSize, mBackgroundPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (isNewState && mListener != null) {
            mListener.onFirstClickWhenNewState();
            isNewState = false;
            Mylog.d(TAG, "onTouchEvent()");
        }

        //如果处于不可点击状态，直接返回
        if (!mCanClick) {
            return true;
        }

        mGesture.onTouchEvent(event);
        return true;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        //transforming the view ratio to the ratio value
        int ratio = 1;

        //get the suggestion
        int width = getMeasuredWidth();
        int height = getMeasuredHeight();

        //subtract the Padding
        int widthMinusPadding = width - getPaddingLeft() - getPaddingRight();
        int heightMinusPadding = height - getPaddingTop() - getPaddingBottom();

        //calculating new sizes
        int maxWidth = heightMinusPadding * ratio;
        int maxHeight = widthMinusPadding / ratio;

        //adding padding
        if (widthMinusPadding > maxWidth) {
            width = maxWidth + getPaddingLeft() + getPaddingRight();
        } else {
            height = maxHeight + getPaddingTop() + getPaddingBottom();
        }
        setMeasuredDimension(width, height);
    }


    @Override
    protected Parcelable onSaveInstanceState() {
        return super.onSaveInstanceState();
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        super.onRestoreInstanceState(state);
    }
}