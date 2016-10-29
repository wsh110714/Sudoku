package com.example.wsh.sudoku.model;


import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import com.example.wsh.sudoku.util.Util;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Random;
import java.util.Stack;


/**
 * Created by wsh on 16-6-30.
 * 数据谜题解析和生成类
 */
public class SudokuGenerator {

    //基本状态，需要用于intent传递
    public static class MinState implements Parcelable {
        public int mMode;
        public int mSize;
        public int mLimitType;
        public int mLevel;
        public boolean mIrregularFixed;
        public String mIrregular;
        public String mPuzzle;


        public MinState() {

        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(mMode);
            dest.writeInt(mSize);
            dest.writeInt(mLimitType);
            dest.writeInt(mLevel);
            dest.writeInt(mIrregularFixed ? 1 : 0);
            dest.writeString(mIrregular);
            dest.writeString(mPuzzle);
        }

        public static final Creator<MinState> CREATOR
                = new Creator<MinState>() {
            @Override
            public MinState createFromParcel(Parcel source) {
                return new MinState(source);
            }

            @Override
            public MinState[] newArray(int size) {
                return new MinState[size];
            }
        };

        private MinState(Parcel in) {
            mMode = in.readInt();
            mSize = in.readInt();
            mLimitType = in.readInt();
            mLevel = in.readInt();
            mIrregularFixed = in.readInt() == 1;
            mIrregular = in.readString();
            mPuzzle = in.readString();
        }
    }

    public static final String TAG = "SudokuGenerator";

    /**
     * 各种静态参数类型有： 模式，大小，额外区域限制，生成难度等级，挖洞策略
     */
    public static final int MODE_NORMAL = 1;
    public static final int MODE_IRREGULAR = 2;     // for size = 9,8,6

    public static final int SIZE_NINE = 9;
    public static final int SIZE_EIGHT = 8;
    public static final int SIZE_SIX = 6;
    public static final int SIZE_FOUR = 4;
    public static final int SIZE_SEVEN = 7;
    public static final int SIZE_FIVE = 5;

    public static final int LIMIT_TYPE_NORMAL = 1;     // 普通类型，无附加限制的额外区域
    public static final int LIMIT_TYPE_DIAGONAL = 2;   // for size = 9
    public static final int LIMIT_TYPE_WINDOW = 3;     // for size = 9
    public static final int LIMIT_TYPE_PERCENTAGE = 4; // for size = 9

    public static final int LEVEL_NONE = 0;
    public static final int LEVEL_EASY = 1;
    public static final int LEVEL_MID = 2;
    public static final int LEVEL_HARD = 3;
    public static final int LEVEL_VERY_HARD = 4;

    private static final int ORDER_RANDOM = 1;  //全盘随机
    private static final int ORDER_SNAKE = 2;   //蛇形顺序
    private static final int ORDER_INDEX = 3;   //按照索引从上到下，从左到右

    /**
     * 基本属性， 需要构造时提供
     * mMode： 规则模式，锯齿模式
     * mSize:  数独大小，目前限制4,6,8,9
     * mLimitType: 数独额外区域类型， 无额外区域，对角线，窗口，百分比
     * mLevel： 难度等级
     * mIrregularFixed: 是否固定
     * mIrregular: 锯齿模式下需要的锯齿连通图字符串
     * mPuzzle: 原始谜题，可外部提供，可自己生成
     */
    public int mMode;
    public int mSize;
    public int mLimitType;
    private int mLevel; //only for 9宫
    private boolean mIrregularFixed = false;
    public String mIrregular;  //for mMinState.mMode == MODE_IRREGULAR
    public String mPuzzle;

    /**
     * 次基本属性， 依赖基本属性变化而变化
     * mSXSize, mSYSize: 规则宫内小宫宽度和高度，依赖{@link #mSize}而变化
     * mTotal： 数独单元格总数，依赖{@link #mSize}而变化
     * mIrregularArray: 锯齿模式下需要的锯齿连通图二维数据，依赖{@link #mIrregular}变化
     */
    private int mSXSize;
    private int mSYSize;
    private int mTotal;
    private int[][] mIrregularArray;
    private int[][][] mIrregularGroupIds;

    /**
     * 生成指定难度等级的数独
     * mHoleStategy: 挖洞策略，依赖{@link #mLevel} 变化
     * mHoleLimitNum： 挖洞限制， 依赖{@link #mLevel} 变化
     */
    private int mHoleStrategy = getHoleStrategy();
    private int mHoleLimitNum = getHoleLimit();

    /**
     * 算法过程中需要的随机数产生器
     */
    Random random = new Random();

    /**
     * 算法过程中是否有时间限制
     */
    private boolean mTimeLimited = true;
    private long mLimitTime = 200;
    private long mTimeOut;

    /**
     * 数独谜题二位数组
     */
    private int[][] mSudokuArray;





    public SudokuGenerator(int mode, int size, int limitType) {
        if (!paramsIsMatched(mode, size, limitType)) {
            throw new RuntimeException("paramsIsMatched false");
        }

        mMode = mode;
        mSize = size;
        mLimitType = limitType;
        mLevel = getDefaultLevel();
        mPuzzle = null;

        mSudokuArray = new int[mSize][mSize];

        mSXSize = getXSize();
        mSYSize = getYSize();
        mTotal = mSize * mSize;
    }

    public SudokuGenerator(MinState minState) {
        if (!paramsIsMatched(minState.mMode, minState.mSize, minState.mLimitType)) {
            throw new RuntimeException("paramsIsMatched false");
        }

        mMode = minState.mMode;
        mSize = minState.mSize;
        mLimitType = minState.mLimitType;

        if (minState.mSize == SIZE_NINE) {
            setLevel(minState.mLevel);
        } else {
            mLevel = getDefaultLevel();
        }

        mSudokuArray = new int[mSize][mSize];

        if (minState.mPuzzle != null) {
            attachPuzzle(minState.mPuzzle, minState.mIrregular);
        }

        if (minState.mIrregular != null) {
            attachIrregular(minState.mIrregular);
        }

        mSXSize = getXSize();
        mSYSize = getYSize();
        mTotal = mSize * mSize;
    }

    public MinState getMinState() {
        MinState state = new MinState();
        state.mMode = mMode;
        state.mSize = mSize;
        state.mLimitType = mLimitType;
        state.mLevel = mLevel;
        state.mIrregular = mIrregular;
        state.mIrregularFixed = mIrregularFixed;
        state.mPuzzle = mPuzzle;
        return state;
    }

    public static boolean paramsIsMatched(int mode, int size, int limitType) {
        boolean matched = false;

        if (mode == MODE_IRREGULAR) { // 5,6,7,8,9
            switch (limitType) {
                case LIMIT_TYPE_PERCENTAGE: //9
                case LIMIT_TYPE_WINDOW:
                    matched = false;
                    break;
                case LIMIT_TYPE_DIAGONAL: //4,5,6,7,8,9
                case LIMIT_TYPE_NORMAL:
                    matched = size == SIZE_FOUR || size == SIZE_SIX || size == SIZE_EIGHT
                            || size == SIZE_NINE || size == SIZE_SEVEN || size == SIZE_FIVE;
                    break;
            }
        } else if (mode == MODE_NORMAL) { //4,6,8,9
            switch (limitType) {
                case LIMIT_TYPE_PERCENTAGE: //9
                case LIMIT_TYPE_WINDOW:
                    matched = size == SIZE_NINE;
                    break;
                case LIMIT_TYPE_DIAGONAL: //6,8,9
                    matched = size == SIZE_SIX || size == SIZE_EIGHT
                            || size == SIZE_NINE;
                    break;
                case LIMIT_TYPE_NORMAL: //4,6,8,9
                    matched = size == SIZE_FOUR || size == SIZE_SIX
                            || size == SIZE_EIGHT || size == SIZE_NINE;
                    break;
            }
        }

        if (!matched) {
            System.out.println("paramsIsMatched return false, mode = " + mode
                    + ", size = " + size + ", limitType = " + limitType);
        }
        return matched;
    }

    public boolean attachPuzzle(@NonNull String puzzle, String irregular) {
        if (puzzle.length() != mSize*mSize) {
            throw new RuntimeException("attachPuzzle(), puzzle is invalid");
        }
        mPuzzle = puzzle;
        Util.string2IntArray(mPuzzle, mSudokuArray);

        if (mMode == SudokuGenerator.MODE_IRREGULAR) {
            if (irregular == null || irregular.length() != mSize*mSize) {
                throw new RuntimeException("attachPuzzle(), irregular is invalid");
            }
            mIrregular = irregular;
            mIrregularFixed = true;

            if (mIrregularArray == null) {
                mIrregularArray = Util.string2IntArray(mIrregular);
            } else {
                Util.string2IntArray(mIrregular, mIrregularArray);
            }
            mIrregularGroupIds = generateIrregularGroupIds(mIrregularArray);
        }
        return true;
    }

    //随机获取一个irregular
    public void attachRandomIrregular() {
        if (mMode != MODE_IRREGULAR) {
            return;
        }

        if (mIrregular != null && mIrregularFixed) {
            return;
        }

        if (mSize == SIZE_FOUR || mSize == SIZE_FIVE) {
            mIrregular = IrregularGenerator.randomGetIrregular(mSize);
        } else {
            mIrregular = new IrregularGenerator(mSize).produceIrregular();
        }

        if (mIrregularArray != null) {
            Util.string2IntArray(mIrregular, mIrregularArray);
        } else {
            mIrregularArray = Util.string2IntArray(mIrregular);
        }
        mIrregularGroupIds = generateIrregularGroupIds(mIrregularArray);
    }

    public void attachIrregular(@NonNull String irregular) {
        if (mMode == MODE_IRREGULAR) {
            return;
        }

        if (irregular.length() != mSize * mSize) {
            throw new RuntimeException("attachIrregular(), irregular is invalid");
        }

        if (mIrregular != null && mIrregularFixed) {
            throw new RuntimeException("attachIrregular(), mIrregular is existed and fixed");
        }

        mIrregular = irregular;
        mIrregularFixed = true;

        if (mIrregularArray != null) {
            Util.string2IntArray(mIrregular, mIrregularArray);
        } else {
            mIrregularArray = Util.string2IntArray(mIrregular);
        }
        mIrregularGroupIds = generateIrregularGroupIds(mIrregularArray);
    }

    private int[][][] generateIrregularGroupIds(int[][] irregularArray) {
        int[][][] groupIds = new int[mSize][mSize][2];

        for (int groupId = 1; groupId <= mSize; groupId++) {
            int count = 0;
            for (int i = 0; i < mSize; i++) {
                for (int j = 0; j < mSize; j++) {
                    if (irregularArray[i][j] == groupId) {
                        groupIds[groupId - 1][count][0] = i;
                        groupIds[groupId - 1][count][1] = j;
                        count++;
                    }
                }
            }
        }

        return groupIds;
    }

    /**
     * 给sudoku指定难度等级，难度等级决定了生成数独谜题时候需要的挖洞策略和限制
     * @param level 难度
     */
    public void setLevel(int level) {
        mLevel = modifyLevel(mSize, level);
        mHoleStrategy = getHoleStrategy();
        mHoleLimitNum = getHoleLimit();
    }

    public static int modifyLevel(int size, int level) {
        int mLevel;
        if (size == SIZE_NINE) {
            switch (level) {
                case LEVEL_EASY:
                case LEVEL_MID:
                case LEVEL_HARD:
                case LEVEL_VERY_HARD:
                    mLevel = level;
                    break;
                default:
                    mLevel = LEVEL_MID;
                    break;
            }
        } else {
             mLevel = LEVEL_NONE;
        }

        return mLevel;
    }


    private int getDefaultLevel() {
        switch (mSize) {
            case SIZE_NINE:
                return LEVEL_MID;
            default:
                return LEVEL_NONE;
        }
    }

    /**
     * 得到数独小宫的宽度和高度,for 4,6,8,9
     * @return 小宫的宽度
     */
    private int getXSize() {
        switch (mSize) {
            case 4:
                return 2;
            case 6:
                return 3;
            case 8:
                return 4;
            case 9:
                return 3;
            default:
                return 0;
        }
    }

    private int getYSize() {
        switch (mSize) {
            case 4:
            case 6:
            case 8:
                return 2;
            case 9:
                return 3;
            default:
                return 0;
        }
    }

    /**
     * 参数配置： 得到数独生成需要初始化的数字个数
     * @return 初始化数字个数
     */
    private int getInitNum() {
        if (mMode == MODE_IRREGULAR) {
            switch (mSize) {
                case SIZE_NINE:
                    switch (mLimitType) {
                        case LIMIT_TYPE_WINDOW:
                            return 4;
                        case LIMIT_TYPE_PERCENTAGE:
                            return 5;
                        case LIMIT_TYPE_DIAGONAL:
                            return 8;
                        case LIMIT_TYPE_NORMAL:
                        default:
                            return 9;
                    }
                case SIZE_EIGHT:
                case SIZE_SEVEN:
                case SIZE_SIX:
                case SIZE_FIVE:
                case SIZE_FOUR:
                default:
                    return mSize;
            }
        } else {
            switch (mSize) {
                case SIZE_NINE:
                    switch (mLimitType) {
                        case LIMIT_TYPE_WINDOW:
                            return 6;
                        case LIMIT_TYPE_PERCENTAGE:
                            return 11;
                        case LIMIT_TYPE_DIAGONAL:
                            return 11;
                        case LIMIT_TYPE_NORMAL:
                        default:
                            return 11;
                    }
                case SIZE_EIGHT:
                case SIZE_SIX:
                case SIZE_FOUR:
                case SIZE_SEVEN:
                case SIZE_FIVE:
                default:
                    return mSize;
            }
        }
    }

    /**
     * 参数配置： 生成数独时，需要在终盘挖洞的个数
     * @return 挖洞个数
     */
    private int getNeedHoleNum() {
        Random random = new Random();
        switch (mSize) {
            case SIZE_FOUR:
                return random.nextInt(4) + 9;
            case SIZE_FIVE:
                return random.nextInt(5) + 14;
            case SIZE_SIX:
                return random.nextInt(7) + 20;
            case SIZE_SEVEN:
                return random.nextInt(8) + 29;
            case SIZE_EIGHT:
                return random.nextInt(10) + 38;
            case SIZE_NINE:
                switch (mLevel) {
                    case LEVEL_EASY:
                        return random.nextInt(7) + 43;
                    case LEVEL_MID:
                        return random.nextInt(6) + 50;
                    case LEVEL_HARD:
                        return random.nextInt(5) + 56;
                    case LEVEL_VERY_HARD:
                        return random.nextInt(4) + 61;
                }
            default:
                return 0;
        }
    }

    private int getHoleStrategy() {
        switch (mSize) {
            case SIZE_NINE:
                switch (mLevel) {
                    case LEVEL_HARD:
                        return ORDER_SNAKE;
                    case LEVEL_VERY_HARD:
                        return ORDER_INDEX;
                    case LEVEL_EASY:
                    case LEVEL_MID:
                    default:
                        return ORDER_RANDOM;
                }
            case SIZE_FOUR:
            case SIZE_FIVE:
            case SIZE_SIX:
            case SIZE_SEVEN:
            case SIZE_EIGHT:
            default:
                return ORDER_RANDOM;
        }
    }

    private int getHoleLimit() {
        switch (mSize) {
            case SIZE_NINE:
                switch (mLevel) {
                    case LEVEL_HARD:
                        return 1;
                    case LEVEL_MID:
                        return 2;
                    case LEVEL_EASY:
                        return 3;
                    case LEVEL_VERY_HARD:
                    default:
                        return 0;
                }
            case SIZE_FOUR:
            case SIZE_FIVE:
            case SIZE_SIX:
            case SIZE_SEVEN:
            case SIZE_EIGHT:
            default:
                return 0;
        }
    }

    /**
     * 检测具体数独中， 位置(row,column)处数据是否有效
     * 主要逻辑判断有： 行不重复， 列不重复， 规则小宫不重复，
     *                锯齿宫不重复， 额外对角线不重复， 额外窗口不重复，  额外百分比区域不重复
     * @param tmpSudoku 需要检测的数独， 可能为算法过程中的临时数独
     * @param row 横坐标
     * @param column 纵坐标
     * @return true: 有效； false: 无效
     */
    public boolean isValid(int[][] tmpSudoku, int row, int column) {
        return isValid(tmpSudoku, row, column, tmpSudoku[row][column]);
    }

    /**
     * 功能等同函数 {@link #isValid(int[][], int, int)}
     * 检测具体数独中， 位置(row,column)处，如果填入数据value，是否有效
     * 主要逻辑判断有： 行不重复， 列不重复， 规则小宫不重复，
     *               锯齿宫不重复， 额外对角线不重复， 额外窗口不重复，  额外百分比区域不重复
     * @param tmpSudoku 需要检测的数独， 可能为算法过程中的临时数独
     * @param row 横坐标
     * @param column 纵坐标
     * @param value 需要填入的value
     * @return true: 有效； false: 无效
     */
    public boolean isValid(int[][] tmpSudoku, int row, int column, int value) {
        if (!isBaseValid(tmpSudoku, row, column, value)) {
            return false;
        }

        if (mMode == MODE_IRREGULAR) {
            if (!isIrregularValid(tmpSudoku, row, column, value)) {
                return false;
            }
        } else {
            if (!isRegionValid(tmpSudoku, row, column, value)) {
                return false;
            }
        }

        switch (mLimitType) {
            case LIMIT_TYPE_DIAGONAL:
                return isXValid(tmpSudoku, row, column, value);
            case LIMIT_TYPE_WINDOW:
                return isWindowValid(tmpSudoku, row, column, value);
            case LIMIT_TYPE_PERCENTAGE:
                return isPercentageValid(tmpSudoku, row, column, value);
            case LIMIT_TYPE_NORMAL:
            default:
                return true;
        }
    }

    public boolean isBaseValid(int[][] tmpSudoku, int row, int column, int value) {
        for (int i = 0; i < mSize; i++) {
            //判断行第i个数字
            if (i != column && tmpSudoku[row][i] == value) {
                return false;
            }

            //判断列第i个数字
            if (i != row && tmpSudoku[i][column] == value) {
                return false;
            }
        }
        return true;
    }

    public boolean isRegionValid(int[][] tmpSudoku, int row, int column, int value) {
        /**
        for (int i = 0; i < mSize; i++) {
            int r = (row/mSXSize)*mSXSize + i%mSXSize;
            int c = (column/mSYSize)*mSYSize + i/mSXSize;
            if (r != row && c != column && tmpSudoku[r][c] == value) {
                return false;
            }
        }*/

        int beginX = (row/mSXSize)*mSXSize;
        int beginY = (column/mSYSize)*mSYSize;
        for (int i = beginX; i < beginX + mSXSize; i++) {
            for (int j = beginY; j < beginY + mSYSize; j++) {
                if (i != row && j != column && tmpSudoku[i][j] == value) {
                    return false;
                }
            }
        }
        return true;
    }

    public boolean isIrregularValid(int[][] tmpSudoku, int row, int column, int value) {
//        int groupId = mIrregularArray[row][column];
//        for (int i = 0; i < mSize; i++) {
//            for (int j = 0; j < mSize; j++) {
//                if (mIrregularArray[i][j] == groupId && tmpSudoku[i][j] == value
//                        && i != row && j != column) {
//                    return false;
//                }
//            }
//        }

        int groupId = mIrregularArray[row][column];
        for (int i = 0; i < mSize; i++) {
            int x = mIrregularGroupIds[groupId-1][i][0];
            int y = mIrregularGroupIds[groupId-1][i][1];
            if (tmpSudoku[x][y] == value && x != row && y!= column) {
                return false;
            }
        }

        return true;
    }

    public boolean isXValid(int[][] tmpSudoku, int row, int column, int value) {
        //如果是对角线重叠的中心点，需要检测两次
        if (row == column) {
            for (int i = 0; i < mSize; i++) {
                if (row != i && tmpSudoku[i][i] == value) {
                    return false;
                }
            }
        }

        if (row + column == mSize-1) {
            for (int i = 0; i < mSize; i++) {
                if (row != i && tmpSudoku[i][mSize-1-i] == value) {
                    return false;
                }
            }
        }

        return true;
    }

    public boolean isWindowValid(int[][] tmpSudoku, int row, int column, int value) {
        if (row == 0 || row == 4 || row == 8 || column == 0 || column == 4 || column == 8) {
            return true;
        }

        int beginX = row > 4 ? 5 : 1;
        int beginY = column > 4 ? 5 : 1;

        for (int i = beginX; i < beginX + 3; i++) {
            for (int j = beginY; j < beginY + 3; j++) {
                if (i != row && j != column && tmpSudoku[i][j] == value) {
                    return false;
                }
            }
        }

        return true;
    }

    public boolean isPercentageValid(int[][] tmpSudoku, int row, int column, int value) {
        if (row + column == mSize-1) {
            for (int i = 0; i < mSize; i++) {
                if (tmpSudoku[i][mSize - 1 - i] == value && row != i) {
                    return false;
                }
            }
            return true;
        } else {
            int beginX = 0, beginY = 0;
            if (row >= 1 && row <= 3 && column >= 1 && column <= 3) {
                beginX = beginY = 1;
            } else if (row >= 5 && row <= 7 && column >= 5 && column <= 7) {
                beginX = beginY = 5;
            } else {
                return true;
            }

            for (int i = beginX; i < beginX + 3; i++) {
                for (int j = beginY; j < beginY + 3; j++) {
                    if (i != row && j != column && tmpSudoku[i][j] == value) {
                        return false;
                    }
                }
            }
            return true;
        }
    }

    private boolean deleteCandidate(int[][] candidate, int index, int key) {
        if (candidate[index][0] > 0 && candidate[index][key] == 1) {
            candidate[index][key] = 0;
            candidate[index][0] -= 1;
            if (candidate[index][0] <= 0) {
                return false;
            }
        }
        return true;
    }

    private void calculateCandidate(int[][] tmpSudoku, int[][] tmpCandidate,
                                    int row, int column) {
        int index = row * mSize + column;

        //如果这一格已经填入数字，就没有候选数子，清空set，直接返回
        if (tmpSudoku[row][column] != 0) {
            tmpCandidate[index][0] = 0;
            return;
        }

        //先放入所有的候选数，然后有的就删除
        for (int i = 1; i <= mSize; i++) {
            tmpCandidate[index][i] = 1;
        }
        tmpCandidate[index][0] = mSize;

        //单元格还没有填入数字，开始计算可选数，这里有部分单元格重复检测了
        for (int k = 0; k < mSize; k++) {
            //同列出现，删除
            if (tmpSudoku[row][k] != 0) {
                deleteCandidate(tmpCandidate, index, tmpSudoku[row][k]);
            }

            //同行出现，删除
            if (tmpSudoku[k][column] != 0) {
                deleteCandidate(tmpCandidate, index, tmpSudoku[k][column]);
            }
        }

        //小宫内出现
        if (mMode == MODE_IRREGULAR) { //锯齿宫内出现
//            int groupId = mIrregularArray[row][column];
//            for (int i = 0; i < mSize; i++) {
//                for (int j = 0; j < mSize; j++) {
//                    if (mIrregularArray[i][j] == groupId && tmpSudoku[i][j] != 0) {
//                        deleteCandidate(tmpCandidate, index, tmpSudoku[i][j]);
//                    }
//                }
//            }

            int groupId = mIrregularArray[row][column];
            for (int i = 0; i < mSize; i++) {
                int x = mIrregularGroupIds[groupId-1][i][0];
                int y = mIrregularGroupIds[groupId-1][i][1];
                if (tmpSudoku[x][y] != 0) {
                    deleteCandidate(tmpCandidate, index, tmpSudoku[x][y]);
                }
            }

        } else { //规则宫内出现
            int startX = (row / mSXSize) * mSXSize;
            int startY = (column / mSYSize) * mSYSize;
            for (int i = startX; i < startX + mSXSize; i++) {
                for (int j = startY; j < startY + mSYSize; j++) {
                    if (tmpSudoku[i][j] != 0) {
                        deleteCandidate(tmpCandidate, index, tmpSudoku[i][j]);
                    }
                }
            }
        }

        //额外区域出现
        switch (mLimitType) {
            case LIMIT_TYPE_DIAGONAL:
                if (row == column) {
                    for (int i = 0; i < mSize; i++) {
                        if (tmpSudoku[i][i] != 0) {
                            deleteCandidate(tmpCandidate, index, tmpSudoku[i][i]);
                        }
                    }
                }

                if (row + column == mSize-1) {
                    for (int i = 0; i < mSize; i++) {
                        if (tmpSudoku[i][mSize-1-i] != 0) {
                            deleteCandidate(tmpCandidate, index, tmpSudoku[i][mSize-1-i]);
                        }
                    }
                }
                break;
            case LIMIT_TYPE_WINDOW:
                if (row == 0 || row == 4 || row == 8
                        || column == 0 || column == 4 || column == 8) {
                    break;
                } else {
                    int beginX = row > 4 ? 5 : 1;
                    int beginY = column > 4 ? 5 : 1;

                    for (int i = beginX; i < beginX + 3; i++) {
                        for (int j = beginY; j < beginY + 3; j++) {
                            if (tmpSudoku[i][j] != 0) {
                                deleteCandidate(tmpCandidate, index, tmpSudoku[i][j]);
                            }
                        }
                    }
                }

                break;
            case LIMIT_TYPE_PERCENTAGE:
                if (row + column == mSize-1) {
                    for (int i = 0; i < mSize; i++) {
                        if (tmpSudoku[i][mSize-1-i] != 0) {
                            deleteCandidate(tmpCandidate, index, tmpSudoku[i][mSize-1-i]);
                        }
                    }
                } else {
                    int beginX, beginY;
                    if (row >= 1 && row <= 3 && column >= 1 && column <= 3) {
                        beginX = beginY = 1;
                    } else if (row >= 5 && row <= 7 && column >= 5 && column <= 7) {
                        beginX = beginY = 5;
                    } else {
                        break;
                    }

                    for (int i = beginX; i < beginX + 3; i++) {
                        for (int j = beginY; j < beginY + 3; j++) {
                            if (tmpSudoku[i][j] != 0) {
                                deleteCandidate(tmpCandidate, index, tmpSudoku[i][j]);
                            }
                        }
                    }
                }
                break;
        }
    }

    private void updateAllCandidate(int[][] tmpSudoku, int[][] tmpCandidate) {
        for (int i = 0; i < mSize; i++) {
            for (int j = 0; j < mSize; j++) {
                calculateCandidate(tmpSudoku, tmpCandidate, i, j);
            }
        }
    }

    private boolean updateRegionCandidate(int[][] tmpCandidate, int row, int column, int num) {
        //清除本单元格所有候选数
        for (int i = 0; i < mSize; i++) {
            tmpCandidate[row*mSize+column][0] = 0;
        }

        //清除行候选数
        for (int i = 0; i < mSize; i++) {
            if (!deleteCandidate(tmpCandidate, row*mSize+i, num)) {
                return false;
            }
        }

        //清除列候选数
        for (int i = 0; i < mSize; i++) {
            if (!deleteCandidate(tmpCandidate, i*mSize+column, num)) {
                return false;
            }
        }

        //清除小宫候选数
        if (mMode == MODE_IRREGULAR) {//锯齿宫
//            int groupId = mIrregularArray[row][column];
//            for (int i = 0; i < mSize; i++) {
//                for (int j = 0; j < mSize; j++) {
//                    if (mIrregularArray[i][j] == groupId
//                            && !deleteCandidate(tmpCandidate, i * mSize + j, num)) {
//                            return false;
//                    }
//                }
//            }

            int groupId = mIrregularArray[row][column];
            for (int i = 0; i < mSize; i++) {
                int x = mIrregularGroupIds[groupId-1][i][0];
                int y = mIrregularGroupIds[groupId-1][i][1];
                if (!deleteCandidate(tmpCandidate, x * mSize + y, num)) {
                    return false;
                }
            }

        } else { //规则宫
            int startX = (row / mSXSize) * mSXSize;
            int startY = (column / mSYSize) * mSYSize;
            for (int i = startX; i < startX + mSXSize; i++) {
                for (int j = startY; j < startY + mSYSize; j++) {
                    if (!deleteCandidate(tmpCandidate, i * mSize + j, num)) {
                        return false;
                    }
                }
            }
        }

        //更新额外区域
        switch (mLimitType) {
        case LIMIT_TYPE_DIAGONAL:
            if (row == column) {
                for (int i = 0; i < mSize; i++) {
                    if (!deleteCandidate(tmpCandidate, i*mSize+i, num)) {
                        return false;
                    }
                }
            }

            if (row + column == mSize-1) {
                for (int i = 0; i < mSize; i++) {
                    if (!deleteCandidate(tmpCandidate, i*mSize+mSize-1-i, num)) {
                        return false;
                    }
                }
            }
            break;
        case LIMIT_TYPE_WINDOW:
            if (row == 0 || row == 4 || row == 8
                    || column == 0 || column == 4 || column == 8) {
                break;
            } else {
                int beginX = row > 4 ? 5 : 1;
                int beginY = column > 4 ? 5 : 1;

                for (int i = beginX; i < beginX + 3; i++) {
                    for (int j = beginY; j < beginY + 3; j++) {
                        if (!deleteCandidate(tmpCandidate, i*mSize+j, num)) {
                            return false;
                        }
                    }
                }
            }

            break;
        case LIMIT_TYPE_PERCENTAGE:
            if (row + column == mSize-1) {
                for (int i = 0; i < mSize; i++) {
                    if (!deleteCandidate(tmpCandidate, i*mSize+mSize-1-i, num)) {
                        return false;
                    }
                }
            } else {
                int beginX, beginY;
                if (row >= 1 && row <= 3 && column >= 1 && column <= 3) {
                    beginX = beginY = 1;
                } else if (row >= 5 && row <= 7 && column >= 5 && column <= 7) {
                    beginX = beginY = 5;
                } else {
                    break;
                }

                for (int i = beginX; i < beginX + 3; i++) {
                    for (int j = beginY; j < beginY + 3; j++) {
                        if (!deleteCandidate(tmpCandidate, i*mSize+j, num)) {
                            return false;
                        }
                    }
                }
            }
            break;
        }

        return true;
    }

    private int refreshSudokuByCandidate(int[][] tmpSudoku, int[][]tmpCandidate) {
        int index = 0;
        while (index < mTotal) {
            if (tmpCandidate[index][0] == 1) {

                for (int key = 1; key <= mSize; key++) {
                    if (tmpCandidate[index][key] == 1) {
                        int row = index / mSize;
                        int column = index % mSize;

                        tmpSudoku[row][column] = key;
                        if (!updateRegionCandidate(tmpCandidate, row, column, key)) {
                            return -1;
                        }

                        index = 0;
                        break;
                    }
                }
            } else {
                index++;
            }
        }

        return 0;
    }

    private int recursion_solve(int[][] tmpSudoku, int[][] tmpCandidate, int index, int[][] result) {
        //index越界时，所有数据都生成，可以结束。结束时将最后一个状态拷贝到result中去，就是终盘
        if (index >= mTotal) {
            for (int i = 0; i < mSize; i++) {
                System.arraycopy(tmpSudoku[i], 0, result[i], 0, mSize);
            }
            return 1;
        }

        if (mTimeLimited && System.currentTimeMillis() > mTimeOut) {
            //System.out.println("timeLimited exit.");
            return -1;
        }

        int row = index / mSize;
        int column = index % mSize;

        //坐标格非空，不用测试
        if (tmpSudoku[row][column] != 0) {
            return recursion_solve(tmpSudoku, tmpCandidate, index+1, result);
        }

        int[][] newSudoku = new int[mSize][mSize];
        int[][] newCandidate = new int[mTotal][mSize+1];

        //选取一个候选数
        for (int key = 1; key <= mSize; key++) {
            if (tmpCandidate[index][key] != 1) {
                continue;
            }

            for (int i = 0; i < mSize; i++) {
                System.arraycopy(tmpSudoku[i], 0, newSudoku[i], 0, mSize);
            }
            for (int i = 0; i < mTotal; i++) {
                System.arraycopy(tmpCandidate[i], 0, newCandidate[i], 0, mSize+1);
            }

            newSudoku[row][column] = key;
            if (!updateRegionCandidate(newCandidate, row, column, key)) {
                continue;
            }

            //剪枝，更新所有只有一个候选数的单元格
            if (refreshSudokuByCandidate(newSudoku, newCandidate) == -1) {
                continue;
            }

            int ret = recursion_solve(newSudoku, newCandidate, index+1, result);
            if (ret != 0) {
                return ret;
            }
        }

        return 0;
    }

    private boolean recursionSolve(int[][] tmpSudoku) {
        //候选数candidate[index][0]存放候选数个数
        int[][] candidate = new int[mTotal][mSize+1];
        updateAllCandidate(tmpSudoku, candidate);

        mTimeOut = System.currentTimeMillis() + mLimitTime;
        return recursion_solve(tmpSudoku, candidate, 0, tmpSudoku) == 1;
    }

    private boolean backtraceSolveByArray(int[][] tmpSudoku) {
        //所有需要尝试的单元格下标都重新放进这个索引数组
        int[][] tmp = new int[mSize*mSize][2];
        int index = 0;
        for(int i = 0; i < mSize; i++) {
            for (int j = 0; j < mSize; j++) {
                if (tmpSudoku[i][j] == 0) {
                    tmp[index][0] = i;
                    tmp[index][1] = j;
                    index++;
                }
            }
        }

        //压缩一下
        int[][] mDataIndex = new int[index][2];
        for (int i = 0; i < mDataIndex.length; i++) {
            mDataIndex[i][0] = tmp[i][0];
            mDataIndex[i][1] = tmp[i][1];
        }

        //回溯
        int num = 0;
        mTimeOut = System.currentTimeMillis() + mLimitTime;
        while (num < mDataIndex.length) {
            if (mTimeLimited && System.currentTimeMillis() > mTimeOut) {
                System.out.println("timeLimited exit.");
                return false;
            }

            int row = mDataIndex[num][0];
            int column = mDataIndex[num][1];

            //尝试合法值
            int k = tmpSudoku[row][column] + 1; //k表示从1开始需要尝试的值，上轮已经尝试到k了，现在需要尝试下一个
            while (k <= mSize) {
                if (isValid(tmpSudoku, row, column, k)) { //测试合格压栈跳出，说明这个[i][j]出已经尝试到k了
                    tmpSudoku[row][column] = k; //k合法，填入
                    break;
                }
                k++;
            }

            //没有合法值，需要回溯
            if (k > mSize) {
                tmpSudoku[row][column] = 0;//需要回溯情况下，原来填过的可能的合法置需要清空
                if (--num < 0) { //回溯，到头了则返回失败
                    return false;
                } else {
                    continue;
                }
            }

            num++;
        }
        return true;
    }

    private boolean backtraceSolveByStack(int[][] tmpSudoku) {
        Stack<Integer> mStack = new Stack<>();

        int num = 0;
        boolean flag = false;
        mTimeOut = System.currentTimeMillis() + mLimitTime;
        while (num < mTotal) {
            if (mTimeLimited && System.currentTimeMillis() > mTimeOut) {
                System.out.println("timeLimited exit.");
                return false;
            }

            int i = num / mSize;
            int j = num % mSize;

            if (tmpSudoku[i][j] == 0 || flag) { //需要尝试
                flag = false;

                //尝试合法值
                int k = tmpSudoku[i][j] + 1; //k表示从1开始需要尝试的值，上轮已经尝试到k了，现在需要尝试下一个
                while (k <= mSize) {
                    tmpSudoku[i][j] = k; //假定k合法，填入

                    if (isValid(tmpSudoku, i, j)) { //测试合格压栈跳出，说明这个[i][j]出已经尝试到k了
                        mStack.push(num);
                        break;
                    }
                    k++;
                }

                //没有合法值,需要回溯
                if (k > mSize) {
                    tmpSudoku[i][j] = 0; //清空
                    if (mStack.isEmpty()) {
                        return false;
                    }
                    num = mStack.pop();
                    flag = true;
                    continue;
                }
            }

            num++;
        }

        return true;
    }

    /**
     * 判断数独 (x,y)处挖洞后是否有其他解
     * @param x 挖洞位置的横坐标
     * @param y 挖洞位置的纵坐标
     * @return true： 有唯一解;  false： 没有
     */
    private boolean haveUniqueSolutionAfterHole(int x, int y) {
        int[][] tempSudoku = new int[mSize][mSize];

        for (int i = 0; i < mSize; i++) {
            System.arraycopy(mSudokuArray[i] , 0, tempSudoku[i], 0, mSize);
        }

        for (int k = 1; k <= mSize; k++) {
            if (k != mSudokuArray[x][y]){
                //填入与原坐标处数字不一样的一个k, 得到新数独
                tempSudoku[x][y] = k;

                //检测新填入数字是否有效, 新数独是否有解，有解说明(x,y)处解不唯一
                if (isValid(tempSudoku, x, y) && recursionSolve(tempSudoku)) {
                    return false;
                }
            }
        }
        return true;
    }

    private int nextIndexByRandom(boolean[] holed) {
        int[] temp = new int[holed.length];
        int k = 0;

        for (int i = 0; i < holed.length; i++) {
            if (!holed[i]) {
                temp[k++] = i;//将i放进temp中
            }
        }

        return (k == 0) ? -1 : temp[random.nextInt(k)];
    }

    private int nextIndexByMaxLineRandom(boolean[] holed) {
        int[] rowExists = new int[mSize];
        int[] columnExists = new int[mSize];
        int[] temp = new int[mSize];

        for (int i = 0; i < holed.length; i++) {
            if (!holed[i]) {
                rowExists[i/9]++;
                columnExists[i%9]++;
            }
        }

        int max, row, column, k;
        switch (random.nextInt(2)) {
            case 0:
                max = rowExists[0];
                row = 0;
                k = 0;
                for (int i = 1; i < mSize; i++) {
                    if (rowExists[i] > max) {
                        max = rowExists[i];
                        row = i;
                    }
                }

                for (int i = 0; i < mSize; i++) {
                    int index = row * mSize + i;
                    if (!holed[index]) {
                        temp[k++] = index;
                    }
                }

                return (k == 0) ? -1 : temp[random.nextInt(k)];
            case 1:
                max = columnExists[0];
                column = 0;
                k = 0;
                for (int i = 1; i < mSize; i++) {
                    if (columnExists[i] > max) {
                        max = columnExists[i];
                        column = i;
                    }
                }

                for (int i = 0; i < mSize; i++) {
                    int index = i * mSize + column;
                    if (!holed[index]) {
                        temp[k++] = index;
                    }
                }

                return (k == 0) ? -1 : temp[random.nextInt(k)];
        }

        return -1;
    }

    private int nextSnakeIndex(int index) {
        int i = index / mSize;
        int j = index % mSize;

        int x,y;
        if (i%2 == 0) {
            if (j == mSize-1) {
                if (i == mSize-1) {
                    x = y = 0;
                } else {
                    x = i + 1;
                    y = j;
                }
            } else {
                x = i;
                y = j+1;
            }
        } else {
            if (j == 0) {
                x = i+1;
                y = j;
            } else {
                x = i;
                y = j-1;
            }
        }

        return x*9+y;
    }

    private int nextIndexBySnake(boolean[] holed, int index) {
        int next = index;

        do {
            next = nextSnakeIndex(next);
            if (!holed[next]) {
                return next;
            }
        } while (next != index);

        return -1;
    }

    private int nextIndexByIndex(boolean[] holed, int index) {
        for (int i = index+1; i < holed.length; i++) {
            if (!holed[i]) {
                return i;
            }
        }

        for (int i = 0; i < index; i++) {
            if (!holed[i]) {
                return i;
            }
        }

        return -1;
    }

    private int nextIndex(boolean[] holed, int index, int holeNum, int needHoleNum) {
        if (mSize == SIZE_NINE && mLevel > LEVEL_MID) {
            if (holeNum % 8 == 0) {
                return nextIndexByRandom(holed);
            } else if ((holeNum >= needHoleNum*2/8 && holeNum < needHoleNum*3/8)
                    || (holeNum >= needHoleNum*6/8 && holeNum < needHoleNum*7/8)) {
                return nextIndexByMaxLineRandom(holed);
            }
        }

        switch (mHoleStrategy) {
            case ORDER_INDEX:
                return nextIndexByIndex(holed, index);
            case ORDER_SNAKE:
                return nextIndexBySnake(holed, index);
            default:
                return nextIndexByRandom(holed);
        }
    }

    private boolean canHole(int i, int j) {
        int rowExist = 0;
        int columnExist = 0;
        for (int k = 0; k < mSize; k++) {
            if (mSudokuArray[i][k] != 0) {
                rowExist++;
            }
            if (mSudokuArray[k][j] != 0) {
                columnExist++;
            }
        }
        return rowExist >= mHoleLimitNum && columnExist >= mHoleLimitNum;
    }

    private void holeSudokuBoard() {
        long time = System.currentTimeMillis();

        boolean[] holed = new boolean[mSize*mSize];
        int needHoleNum = getNeedHoleNum();
        int holeNum = 0;

        int x = random.nextInt(mSize);
        int y = random.nextInt(mSize);
        int next = x * mSize + y;

        for(;;) {
            holed[next] = true; //表示此洞已经检测过是否可挖，做个标记

            if (canHole(x, y) && haveUniqueSolutionAfterHole(x, y)) {
                mSudokuArray[x][y] = 0;
                if (++holeNum >= needHoleNum) {//挖洞个数已达到要求跳出
                    System.out.println("holeNum(" + holeNum + ") >= needHoleNum("
                            + needHoleNum + ") break");
                    break;
                }
            }

            //得到下一个挖洞位置，无洞可挖则index==-1
            next = nextIndex(holed, next, holeNum, needHoleNum);
            if (next == -1) {
                System.out.println("can not find next hole, nextIndex == -1");
                break;
            }
            x = next / mSize;
            y = next % mSize;
        }

        System.out.println("needHoleNum = " + needHoleNum + ", holeNum = " + holeNum
                + ", time = " + (System.currentTimeMillis() - time));
    }

    /**
     * 生成数独使用，空数独中开始随机填入若干个数字
     */
    private void randomFillEmptySudoku() {
        Util.initArray(mSudokuArray);

        int x, y;
        int k = 0;

        while (k < getInitNum()) {
            x = random.nextInt(mSize);
            y = random.nextInt(mSize);

            if (mSudokuArray[x][y] != 0) {
                continue;
            }

            ArrayList<Integer> temp = new ArrayList<>(mSize);
            for (int i = 0; i < mSize; i++) {
                temp.add(i+1);
            }

            for (int limit = mSize; limit > 0; limit--) {
                int v = temp.remove(random.nextInt(limit));

                if (isValid(mSudokuArray, x, y, v)) {
                    mSudokuArray[x][y] = v;
                    k++;
                    break;
                }
            }
        }
    }

    private void LasVegas() {
        boolean solved;
        long time;
        int count = 0;

        do {
            time = System.currentTimeMillis();

            if (count%10 == 0) {
                attachRandomIrregular();
            }

            randomFillEmptySudoku();
            //Util.printArray(mSudokuArray);
            solved = recursionSolve(mSudokuArray);
            //solved = backtraceSolveByArray(mSudokuArray);
            count++;
        } while (!solved);

        System.out.println("LasVegas end. count = " + count
                + ", time = " + (System.currentTimeMillis() - time));
    }

    public boolean isAllValid() {
        int k = 0;
        for (int i = 0; i < mSize; i++) {
            for (int j = 0; j < mSize; j++) {
                if (mSudokuArray[i][j] != 0) {
                    if (!isValid(mSudokuArray, i, j)) {
                        return false;
                    }
                    k++;
                }
            }
        }

        if (mSize == SIZE_NINE && k < 17) {
            System.out.println("at least need 17, k = " + k);
            return false;
        }

        return solve() != null;
    }

    /**
     * 生成一个数独
     * @return 生成的数独字符串
     */
    public String producePuzzle() {
        System.out.println("producePuzzle() mMode = " + mMode
                + ", mSize = " + mSize
                + ", mLimitType = " + mLimitType
                + ", mLevel = " + mLevel);

        mTimeLimited = true;
        LasVegas();
        Util.printArray(mSudokuArray);

        mTimeLimited = false;
        holeSudokuBoard();
        Util.printArray(mSudokuArray);

        mPuzzle = Util.intArray2String(mSudokuArray);
        return mPuzzle;
    }

    /**
     * 求解一个数独
     * @return 求解过后的数独字符串
     */
    public String solve() {
        long time = System.currentTimeMillis();
        String result = null;

        mTimeLimited = false;
        if (recursionSolve(mSudokuArray)) {
            result = Util.intArray2String(mSudokuArray);
        }

        System.out.println("solve() result = " + result + ", time = " + (System.currentTimeMillis() - time));
        Util.printArray(mSudokuArray);
        return result;
    }

    public static void main(String[] args) throws IOException {

        for (int limitType = 1; limitType <= 4; limitType++) {
            for (int level = 3; level < 5; level++) {

                SudokuGenerator sudokuGenerator = new SudokuGenerator(MODE_NORMAL, SIZE_NINE, limitType);
                sudokuGenerator.setLevel(level);

                FileOutputStream out = new FileOutputStream("/home/wsh/sudokuData/" + "sudoku_" + sudokuGenerator.mMode
                        + "_" + sudokuGenerator.mSize + "_" + sudokuGenerator.mLimitType + "_" + sudokuGenerator.mLevel,
                        true);

                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out));

                for (int i = 0; i < 200; i++) {
                    sudokuGenerator.producePuzzle();
                    writer.write("\"" + sudokuGenerator.mPuzzle + "\",\n");
                }

                writer.close();
            }
        }





//        SudokuGenerator sudokuGenerator = new SudokuGenerator(MODE_NORMAL, SIZE_NINE, LIMIT_TYPE_NORMAL);
//        for (int i = 92; i < 100; i++) {
//            sudokuGenerator.attachPuzzle(Data17.getData_17_16[i], null);
//            long time1 = System.currentTimeMillis();
//            boolean result1 = sudokuGenerator.recursionSolve(sudokuGenerator.mSudokuArray);
//            System.out.println("result: " + result1 + ", time: " + (System.currentTimeMillis() - time1));
//            Util.printArray(sudokuGenerator.mSudokuArray);
//
//
//            sudokuGenerator.attachPuzzle(Data17.getData_17_16[i], null);
//            long time2 = System.currentTimeMillis();
//            boolean result = sudokuGenerator.backtraceSolveByArray(sudokuGenerator.mSudokuArray);
//            System.out.println("result: " + result + ", time: " + (System.currentTimeMillis() - time2));
//            Util.printArray(sudokuGenerator.mSudokuArray);
//
//            System.out.println("------------------------------------------------------------");
//        }
    }
}