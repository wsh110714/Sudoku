package com.example.wsh.sudoku;

import com.example.wsh.sudoku.Fragment.GameFragment;
import com.example.wsh.sudoku.model.SudokuGenerator;

/**
 * Created by wsh on 16-7-16.
 */
public class Constants {
    //数独的fragmentTAG
    public static String gameFragmentObjTAG(SudokuGenerator.MinState state) {
        return GameFragment.TAG + "_" + state.mMode + "_" + state.mSize + "_" + state.mLimitType;
    }

    public static int parseModeFromTag(String tag) {
        String pre = GameFragment.TAG + "_";
        return Integer.parseInt(tag.substring(pre.length(),pre.length()+1));
    }

    public static int parseSizeTag(String tag) {
        String pre = GameFragment.TAG + "_1_";
        return Integer.parseInt(tag.substring(pre.length(),pre.length()+1));
    }

    public static int parseLimitTypeFromTag(String tag) {
        String pre = GameFragment.TAG + "_1_9_";
        return Integer.parseInt(tag.substring(pre.length()));
    }




    //生成数独action
    public final static String ACTION_PRODUCE_SUDOKU_AND_INSERTDB
            = "com.example.wsh.sudoku.action.produceSudokuAndInsertDb";

    //求解数独的action
    public final static String ACTION_SOLVE_SUDOKU
            = "com.example.wsh.sudoku.action.solveSudoku";

    //action附带参数
    public final static String EXTRA_SUDOKU_MIN_STATE
            = "com.example.wsh.sudoku.extra.sudokuMinState";



    //更新数独
    public static final String ACTION_UPDATE_SUDOKU
            = "com.example.wsh.sudoku.action.updateSudoku";

    public final static String EXTRA_SUDOKU_GAME_STATE
            = "com.example.wsh.sudoku.extra.sudokuGameState";


    //广播数独答案
    public static String ACTION_broadcastAnswer(SudokuGenerator.MinState state) {
        return "com.example.wsh.sudoku.action_broadcastPuzzleAnswer_" + state.mMode
                + "_" + state.mSize + "_" + state.mLimitType;
    }

    public final static String EXTRA_SUDOKU_PUZZLE_ANSWER
            = "com.example.wsh.sudoku.extra.sudokuPuzzleAnswer";


}
