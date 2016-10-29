package com.example.wsh.sudoku.model;

import android.graphics.Point;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import com.example.wsh.sudoku.util.Mylog;
import com.example.wsh.sudoku.util.Util;

import java.util.HashMap;

/**
 * Created by wsh on 16-7-13.
 */
public class SudokuGame {
    //基本状态，需要用于intent传递
    public static class GameState implements Parcelable {
        public int mId;
        public int mMode;
        public int mSize;
        public int mLimitType;
        public int mLevel;
        public String mIrregular;
        public String mPuzzle;
        public String mPlayingPuzzle;
        public boolean mChallenged;
        public int mChallengedTime;
        public int mChallengedRealTime;

        public GameState() {

        }

        public String toString() {
             return mId + ", " + mMode + ", " + mSize + ", " + mLimitType
                     + ", " + mLevel + ", " + mIrregular + ", " + mPuzzle + ", " + mPlayingPuzzle
                     + ", " + mChallenged + ", " + mChallengedTime + ", " + mChallengedRealTime;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(mId);
            dest.writeInt(mMode);
            dest.writeInt(mSize);
            dest.writeInt(mLimitType);
            dest.writeInt(mLevel);
            dest.writeString(mIrregular);
            dest.writeString(mPuzzle);
            dest.writeString(mPlayingPuzzle);
            dest.writeInt(mChallenged ? 1 : 0);
            dest.writeInt(mChallengedTime);
            dest.writeInt(mChallengedRealTime);
        }

        public static final Creator<GameState> CREATOR
                = new Creator<GameState>() {
            @Override
            public GameState createFromParcel(Parcel source) {
                return new GameState(source);
            }

            @Override
            public GameState[] newArray(int size) {
                return new GameState[size];
            }
        };

        private GameState(Parcel in) {
            mId = in.readInt();
            mMode = in.readInt();
            mSize = in.readInt();
            mLimitType = in.readInt();
            mLevel = in.readInt();
            mIrregular = in.readString();
            mPuzzle = in.readString();
            mPlayingPuzzle = in.readString();
            mChallenged = in.readInt() == 1;
            mChallengedTime = in.readInt();
            mChallengedRealTime = in.readInt();
        }
    }

    private static final String TAG = "SudokuGame";

    private static final String STATE_BUNDLE = "SimpleGame_STATE_BUNDLE";
    private static final String STATE_PUZZLE = "SimpleGame_STATE_PUZZLE";
    private static final String STATE_SUDOKU_DATA = "SimpleGame_STATE_SUDOKU_DATA";
    private static final String STATE_ANSWER = "SimpleGame_STATE_ANSWER";

    //minState
    public int mMode;
    public int mSize;
    public int mLimitType;
    private int mLevel = SudokuGenerator.LEVEL_MID; //only for 9宫
    private boolean mIrregularFixed = false;
    public String mIrregular;  //for mMinState.mMode == MODE_IRREGULAR
    public String mPuzzle;

    //game state
    public int mId;
    public String mPlayingPuzzle;
    public boolean mChallenged;
    public int mChallengedTime;
    public int mChallengedRealTime;

    //run state
    private int[][] mSudokuData;
    private int[][] mAnswer;





    public SudokuGenerator.MinState getMinState() {
        SudokuGenerator.MinState state = new SudokuGenerator.MinState();
        state.mMode = mMode;
        state.mSize = mSize;
        state.mLimitType = mLimitType;
        state.mLevel = mLevel;
        state.mIrregular = mIrregular;
        state.mIrregularFixed = mIrregularFixed;
        state.mPuzzle = mPuzzle;
        return state;
    }

    public GameState getGameState() {
        GameState state = new GameState();
        state.mId = mId;
        state.mMode = mMode;
        state.mSize = mSize;
        state.mLimitType = mLimitType;
        state.mLevel = mLevel;
        state.mIrregular = mIrregular;
        state.mPuzzle = mPuzzle;
        state.mPlayingPuzzle = mPlayingPuzzle;
        state.mChallenged = mChallenged;
        state.mChallengedTime = mChallengedTime;
        state.mChallengedRealTime = mChallengedRealTime;
        return state;
    }

    public SudokuGame(SudokuGenerator.MinState minState) {
        if (!SudokuGenerator.paramsIsMatched(minState.mMode, minState.mSize, minState.mLimitType)) {
            throw new RuntimeException("paramsIsMatched false");
        }

        mMode = minState.mMode;
        mSize = minState.mSize;
        mLimitType = minState.mLimitType;

        mLevel = SudokuGenerator.modifyLevel(minState.mSize, mLevel);

        attachData(minState.mPuzzle, null, minState.mIrregular);
    }

    public SudokuGame(GameState state) {
        mId = state.mId;

        if (!SudokuGenerator.paramsIsMatched(state.mMode, state.mSize, state.mLimitType)) {
            throw new RuntimeException("paramsIsMatched false");
        }

        mMode = state.mMode;
        mSize = state.mSize;
        mLimitType = state.mLimitType;

        mLevel = SudokuGenerator.modifyLevel(state.mSize, mLevel);

        attachData(state.mPuzzle, state.mPlayingPuzzle, state.mIrregular);

        mChallenged = state.mChallenged;
        mChallengedTime = state.mChallengedTime;
        mChallengedRealTime = state.mChallengedRealTime;
    }

    /**
     * 指定谜题，当锯齿模式下，指定谜题同时必须指定irregular;
     * @param puzzle 谜题
     * @param irregular 锯齿宫连通数据
     * @return true: 成功
     */
    public boolean attachData(String puzzle, String playingPuzzle, String irregular) {
        Mylog.d(TAG, "puzzle = " + puzzle
                + ", playingPuzzle = " + playingPuzzle
                + ", irregular = " + irregular);

        if (puzzle != null && puzzle.length() == mSize*mSize) {
            mPuzzle = puzzle;

            if (playingPuzzle != null && playingPuzzle.length() == mSize*mSize) {
                mPlayingPuzzle = playingPuzzle;
            } else {
                mPlayingPuzzle = puzzle;
            }

            if (mSudokuData == null) {
                mSudokuData = Util.string2IntArray(mPlayingPuzzle);
            } else {
                Util.string2IntArray(mPlayingPuzzle, mSudokuData);
            }
        }

        if (mMode == SudokuGenerator.MODE_IRREGULAR) {
            if (mPuzzle != null) { //必须赋值，否则扔出异常
                if (irregular != null && irregular.length() == mSize*mSize) {
                    mIrregular = irregular;
                    mIrregularFixed = true;
                } else {
                    throw new RuntimeException("irregular mode, puzzle have not valid irregular");
                }
            } else {
                if (irregular != null && irregular.length() == mSize*mSize) {
                    mIrregular = irregular;
                    mIrregularFixed = true;
                }
            }
        }
        return true;
    }

    public int getLevel() {
        return mLevel;
    }

    public void setLevel(int level) {
        mLevel = SudokuGenerator.modifyLevel(mSize, mLevel);
    }

    public boolean setSudokuData(int x, int y, int value) {
        if (mSudokuData == null
                || x < 0 || x >= mSudokuData.length
                || y < 0 || y >= mSudokuData[0].length ) {
            return false;
        }
        mSudokuData[x][y] = value;
        return true;
    }

    public String getPlayingPuzzle() {
        return Util.intArray2String(mSudokuData);
    }

    public void setAnswer(String puzzleString) {
        mAnswer = Util.string2IntArray(puzzleString);
    }

    /**
     * 玩家数据和答案进行一次检查，找出所有不一样的
     * @return 返回所有和答案不一致的单元格
     */
    public HashMap<Point, Integer> check() {
        if (mAnswer == null || mSudokuData == null) {
            return null;
        }

        HashMap<Point, Integer> map = new HashMap<>();

        for (int i = 0; i < mSize; i++) {
            for (int j = 0; j < mSize; j++) {
                if (mSudokuData[i][j] == 0 || mAnswer[i][j] == 0) { //not check
                    continue;
                }

                if (mSudokuData[i][j] != mAnswer[i][j]) {
                    map.put(new Point(i,j), mSudokuData[i][j]);
                }
            }
        }
        return map;
    }

    /**
     * 判断数独是否正确完成，通过和答案比较的方式
     * @return true：正确完成
     */
    private boolean isSolvedByCompareAnswer() {
        if (mAnswer == null || mSudokuData == null) {
            return false;
        }

        for (int i = 0; i < mSize; i++) {
            for (int j = 0; j < mSize; j++) {
                if (mSudokuData[i][j] != mAnswer[i][j] || mSudokuData[i][j] == 0) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     *包裹函数
     */
    public boolean isSolved() {
        return isSolvedByCompareAnswer();
    }
}
