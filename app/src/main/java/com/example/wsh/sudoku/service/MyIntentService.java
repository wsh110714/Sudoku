package com.example.wsh.sudoku.service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import com.example.wsh.sudoku.Constants;
import com.example.wsh.sudoku.db.DbHelper;
import com.example.wsh.sudoku.model.SudokuGame;
import com.example.wsh.sudoku.model.SudokuGenerator;
import com.example.wsh.sudoku.util.Mylog;

/**
 * Created by wsh on 16-7-16.
 */
public class MyIntentService extends IntentService {
    private final static String TAG = "MyIntentService";

    public MyIntentService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent == null) {
            return;
        }

        final String action = intent.getAction();
        Mylog.d(TAG, "onHandleIntent(), action: " + action);

        if (action.equals(Constants.ACTION_SOLVE_SUDOKU)) {
            SudokuGenerator.MinState minState =
                    intent.getParcelableExtra(Constants.EXTRA_SUDOKU_MIN_STATE);
            SudokuGenerator sudoku = new SudokuGenerator(minState);
            String result = sudoku.solve();

            Intent i = new Intent(Constants.ACTION_broadcastAnswer(minState));
            i.putExtra(Constants.EXTRA_SUDOKU_PUZZLE_ANSWER, result);
            LocalBroadcastManager.getInstance(this).sendBroadcast(i);

        } else if (action.equals(Constants.ACTION_PRODUCE_SUDOKU_AND_INSERTDB)) {
            SudokuGenerator.MinState minState =
                    intent.getParcelableExtra(Constants.EXTRA_SUDOKU_MIN_STATE);
            SudokuGenerator sudoku = new SudokuGenerator(minState);
            String result = sudoku.producePuzzle();
            Mylog.d(TAG, "onHandleIntent, produce puzzleStr: " + result);
            DbHelper.insertSudoku(minState);

        } else if (action.equals(Constants.ACTION_UPDATE_SUDOKU)) {
            SudokuGame.GameState gameState =
                    intent.getParcelableExtra(Constants.EXTRA_SUDOKU_GAME_STATE);
            DbHelper.updateSudoku(gameState);
        }
    }

    /**
     * 包装函数。便于外部调用
     */
    public static void startSolveSudokuService(Context context, SudokuGenerator.MinState state) {
        Intent i = new Intent(context, MyIntentService.class);
        i.setAction(Constants.ACTION_SOLVE_SUDOKU);
        i.putExtra(Constants.EXTRA_SUDOKU_MIN_STATE, state);
        context.startService(i);
    }

    /**
     * 包装函数，算法产生一个数独，并且插入数据库
     */
    public static void startProduceSudokuService(Context context, SudokuGenerator.MinState state) {
        Intent i = new Intent(context, MyIntentService.class);
        i.setAction(Constants.ACTION_PRODUCE_SUDOKU_AND_INSERTDB);
        i.putExtra(Constants.EXTRA_SUDOKU_MIN_STATE, state);
        context.startService(i);
    }

    public static void startUpdateSudokuService(Context context, SudokuGame.GameState state) {
        Intent i = new Intent(context, MyIntentService.class);
        i.setAction(Constants.ACTION_UPDATE_SUDOKU);
        i.putExtra(Constants.EXTRA_SUDOKU_GAME_STATE, state);
        context.startService(i);
    }
}
