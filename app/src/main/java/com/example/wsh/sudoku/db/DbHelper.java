package com.example.wsh.sudoku.db;


import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.Nullable;

import com.example.wsh.sudoku.App;
import com.example.wsh.sudoku.model.SudokuGame;
import com.example.wsh.sudoku.model.SudokuGenerator;
import com.example.wsh.sudoku.service.MyIntentService;
import com.example.wsh.sudoku.util.Mylog;

import java.util.Random;

/**
 * Created by wsh on 16-7-9.
 */
public class DbHelper extends SQLiteOpenHelper {
    private static final String TAG = "DbHelper";

    public static final String DATABASE_NAME = "sudoku";

    public static final int DATABASE_VERSION = 2;

    //db version 1
    public static final String SQL_CREATE_SUDOKU_TABLE_VERSION1
            = " create table sudoku ( "
            + " id integer primary key autoincrement, "
            + " mode integer not null, "
            + " size integer not null, "
            + " limit_type integer not null, "
            + " level integer, "
            + " irregular Text, "
            + " puzzle Text )";

    public static final String SQL_CREATE_PLAYING_TABLE_VERSION1
            = " create table playing ( "
            + " mode integer not null, "
            + " size integer not null, "
            + " limit_type integer not null, "
            + " id integer ) ";

    public static final String SQL_CREATE_PLAYING_TABLE_INDEX_VERSION1
            = " create unique index playing_index on playing (mode, size, limit_type) ";



    //db version2
    public static final String SQL_CREATE_SUDOKU_TABLE_VERSION2
            = " create table sudoku ( "
            + " id integer primary key autoincrement, "
            + " mode integer not null, "
            + " size integer not null, "
            + " limit_type integer not null, "
            + " level integer, "
            + " irregular Text, "
            + " puzzle Text, "
            + " playing_puzzle Text, "
            + " challenged integer, "
            + " challenged_time integer, "
            + " challenged_real_time integer ) ";

    public static final String SQL_ALTER_SUDOKU_TABLE_VERSION2
            = " alter table sudoku add column "
            + " playing_puzzle Text, challenged integer, "
            + " challenged_time integer, challenged_real_time integer ";





    public DbHelper() {
        super(App.getContext(), DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        switch (oldVersion) {
            case 1:
                db.execSQL(SQL_ALTER_SUDOKU_TABLE_VERSION2);
            default:
        }
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_SUDOKU_TABLE_VERSION2);
        db.execSQL(SQL_CREATE_PLAYING_TABLE_VERSION1);
        db.execSQL(SQL_CREATE_PLAYING_TABLE_INDEX_VERSION1);

        //irregular mode
        for (int i = 0; i < IrregularSudokuData.sudoku_2_4_1_0.length; i++) {
            String puzzle = IrregularSudokuData.sudoku_2_4_1_0[i][0].toString();
            String irregular = IrregularSudokuData.sudoku_2_4_1_0[i][1].toString();
            insertSudoku(db, SudokuGenerator.MODE_IRREGULAR, SudokuGenerator.SIZE_FOUR, SudokuGenerator.LIMIT_TYPE_NORMAL,
                    SudokuGenerator.LEVEL_NONE, irregular, puzzle);
        }

        for (int i = 0; i < IrregularSudokuData.sudoku_2_5_1_0.length; i++) {
            String puzzle = IrregularSudokuData.sudoku_2_5_1_0[i][0].toString();
            String irregular = IrregularSudokuData.sudoku_2_5_1_0[i][1].toString();
            insertSudoku(db, SudokuGenerator.MODE_IRREGULAR, SudokuGenerator.SIZE_FIVE, SudokuGenerator.LIMIT_TYPE_NORMAL,
                    SudokuGenerator.LEVEL_NONE, irregular, puzzle);
        }

        for (int i = 0; i < IrregularSudokuData.sudoku_2_6_1_0.length; i++) {
            String puzzle = IrregularSudokuData.sudoku_2_6_1_0[i][0].toString();
            String irregular = IrregularSudokuData.sudoku_2_6_1_0[i][1].toString();
            insertSudoku(db, SudokuGenerator.MODE_IRREGULAR, SudokuGenerator.SIZE_SIX, SudokuGenerator.LIMIT_TYPE_NORMAL,
                    SudokuGenerator.LEVEL_NONE, irregular, puzzle);
        }

        for (int i = 0; i < IrregularSudokuData.sudoku_2_7_1_0.length; i++) {
            String puzzle = IrregularSudokuData.sudoku_2_7_1_0[i][0].toString();
            String irregular = IrregularSudokuData.sudoku_2_7_1_0[i][1].toString();
            insertSudoku(db, SudokuGenerator.MODE_IRREGULAR, SudokuGenerator.SIZE_SEVEN, SudokuGenerator.LIMIT_TYPE_NORMAL,
                    SudokuGenerator.LEVEL_NONE, irregular, puzzle);
        }

        for (int i = 0; i < IrregularSudokuData.sudoku_2_8_1_0.length; i++) {
            String puzzle = IrregularSudokuData.sudoku_2_8_1_0[i][0].toString();
            String irregular = IrregularSudokuData.sudoku_2_8_1_0[i][1].toString();
            insertSudoku(db, SudokuGenerator.MODE_IRREGULAR, SudokuGenerator.SIZE_EIGHT, SudokuGenerator.LIMIT_TYPE_NORMAL,
                    SudokuGenerator.LEVEL_NONE, irregular, puzzle);
        }

        //2-9-1
        for (int i = 0; i < IrregularSudokuData.sudoku_2_9_1_1.length; i++) {
            String puzzle = IrregularSudokuData.sudoku_2_9_1_1[i][0].toString();
            String irregular = IrregularSudokuData.sudoku_2_9_1_1[i][1].toString();
            insertSudoku(db, SudokuGenerator.MODE_IRREGULAR, SudokuGenerator.SIZE_NINE, SudokuGenerator.LIMIT_TYPE_NORMAL,
                    SudokuGenerator.LEVEL_EASY, irregular, puzzle);
        }

        for (int i = 0; i < IrregularSudokuData.sudoku_2_9_1_2.length; i++) {
            String puzzle = IrregularSudokuData.sudoku_2_9_1_2[i][0].toString();
            String irregular = IrregularSudokuData.sudoku_2_9_1_2[i][1].toString();
            insertSudoku(db, SudokuGenerator.MODE_IRREGULAR, SudokuGenerator.SIZE_NINE, SudokuGenerator.LIMIT_TYPE_NORMAL,
                    SudokuGenerator.LEVEL_MID, irregular, puzzle);
        }

        for (int i = 0; i < IrregularSudokuData.sudoku_2_9_1_3.length; i++) {
            String puzzle = IrregularSudokuData.sudoku_2_9_1_3[i][0].toString();
            String irregular = IrregularSudokuData.sudoku_2_9_1_3[i][1].toString();
            insertSudoku(db, SudokuGenerator.MODE_IRREGULAR, SudokuGenerator.SIZE_NINE, SudokuGenerator.LIMIT_TYPE_NORMAL,
                    SudokuGenerator.LEVEL_HARD, irregular, puzzle);
        }

        for (int i = 0; i < IrregularSudokuData.sudoku_2_9_1_4.length; i++) {
            String puzzle = IrregularSudokuData.sudoku_2_9_1_4[i][0].toString();
            String irregular = IrregularSudokuData.sudoku_2_9_1_4[i][1].toString();
            insertSudoku(db, SudokuGenerator.MODE_IRREGULAR, SudokuGenerator.SIZE_NINE, SudokuGenerator.LIMIT_TYPE_NORMAL,
                    SudokuGenerator.LEVEL_VERY_HARD, irregular, puzzle);
        }


        //------------------------- normal mode ----------------------------------------------------
        //4
        for (int i = 0; i < SudokuData.sudoku_1_4_1_0.length; i++) {
            String puzzle = SudokuData.sudoku_1_4_1_0[i].toString();
            insertSudoku(db, SudokuGenerator.MODE_NORMAL, SudokuGenerator.SIZE_FOUR, SudokuGenerator.LIMIT_TYPE_NORMAL,
                    SudokuGenerator.LEVEL_NONE, null, puzzle);
        }

        //6
        for (int i = 0; i < SudokuData.sudoku_1_6_1_0.length; i++) {
            String puzzle = SudokuData.sudoku_1_6_1_0[i].toString();
            insertSudoku(db, SudokuGenerator.MODE_NORMAL, SudokuGenerator.SIZE_SIX, SudokuGenerator.LIMIT_TYPE_NORMAL,
                    SudokuGenerator.LEVEL_NONE, null, puzzle);
        }

        for (int i = 0; i < SudokuData.sudoku_1_6_2_0.length; i++) {
            String puzzle = SudokuData.sudoku_1_6_2_0[i].toString();
            insertSudoku(db, SudokuGenerator.MODE_NORMAL, SudokuGenerator.SIZE_SIX, SudokuGenerator.LIMIT_TYPE_DIAGONAL,
                    SudokuGenerator.LEVEL_NONE, null, puzzle);
        }

        //8
        for (int i = 0; i < SudokuData.sudoku_1_8_1_0.length; i++) {
            String puzzle = SudokuData.sudoku_1_8_1_0[i].toString();
            insertSudoku(db, SudokuGenerator.MODE_NORMAL, SudokuGenerator.SIZE_EIGHT, SudokuGenerator.LIMIT_TYPE_NORMAL,
                    SudokuGenerator.LEVEL_NONE, null, puzzle);
        }

        for (int i = 0; i < SudokuData.sudoku_1_8_2_0.length; i++) {
            String puzzle = SudokuData.sudoku_1_8_2_0[i].toString();
            insertSudoku(db, SudokuGenerator.MODE_NORMAL, SudokuGenerator.SIZE_EIGHT, SudokuGenerator.LIMIT_TYPE_DIAGONAL,
                    SudokuGenerator.LEVEL_NONE, null, puzzle);
        }

        //1-9-1
        for (int i = 0; i < SudokuData.sudoku_1_9_1_1.length; i++) {
            String puzzle = SudokuData.sudoku_1_9_1_1[i].toString();
            insertSudoku(db, SudokuGenerator.MODE_NORMAL, SudokuGenerator.SIZE_NINE, SudokuGenerator.LIMIT_TYPE_NORMAL,
                    SudokuGenerator.LEVEL_EASY, null, puzzle);
        }

        for (int i = 0; i < SudokuData.sudoku_1_9_1_2.length; i++) {
            String puzzle = SudokuData.sudoku_1_9_1_2[i].toString();
            insertSudoku(db, SudokuGenerator.MODE_NORMAL, SudokuGenerator.SIZE_NINE, SudokuGenerator.LIMIT_TYPE_NORMAL,
                    SudokuGenerator.LEVEL_MID, null, puzzle);
        }

        for (int i = 0; i < SudokuData.sudoku_1_9_1_3.length; i++) {
            String puzzle = SudokuData.sudoku_1_9_1_3[i].toString();
            insertSudoku(db, SudokuGenerator.MODE_NORMAL, SudokuGenerator.SIZE_NINE, SudokuGenerator.LIMIT_TYPE_NORMAL,
                    SudokuGenerator.LEVEL_HARD, null, puzzle);
        }

        for (int i = 0; i < SudokuData.sudoku_1_9_1_4.length; i++) {
            String puzzle = SudokuData.sudoku_1_9_1_4[i].toString();
            insertSudoku(db, SudokuGenerator.MODE_NORMAL, SudokuGenerator.SIZE_NINE, SudokuGenerator.LIMIT_TYPE_NORMAL,
                    SudokuGenerator.LEVEL_VERY_HARD, null, puzzle);
        }

        //1-9-2
        for (int i = 0; i < SudokuData.sudoku_1_9_2_1.length; i++) {
            String puzzle = SudokuData.sudoku_1_9_2_1[i].toString();
            insertSudoku(db, SudokuGenerator.MODE_NORMAL, SudokuGenerator.SIZE_NINE, SudokuGenerator.LIMIT_TYPE_DIAGONAL,
                    SudokuGenerator.LEVEL_EASY, null, puzzle);
        }

        for (int i = 0; i < SudokuData.sudoku_1_9_2_2.length; i++) {
            String puzzle = SudokuData.sudoku_1_9_2_2[i].toString();
            insertSudoku(db, SudokuGenerator.MODE_NORMAL, SudokuGenerator.SIZE_NINE, SudokuGenerator.LIMIT_TYPE_DIAGONAL,
                    SudokuGenerator.LEVEL_MID, null, puzzle);
        }

        for (int i = 0; i < SudokuData.sudoku_1_9_2_3.length; i++) {
            String puzzle = SudokuData.sudoku_1_9_2_3[i].toString();
            insertSudoku(db, SudokuGenerator.MODE_NORMAL, SudokuGenerator.SIZE_NINE, SudokuGenerator.LIMIT_TYPE_DIAGONAL,
                    SudokuGenerator.LEVEL_HARD, null, puzzle);
        }

        for (int i = 0; i < SudokuData.sudoku_1_9_2_4.length; i++) {
            String puzzle = SudokuData.sudoku_1_9_2_4[i].toString();
            insertSudoku(db, SudokuGenerator.MODE_NORMAL, SudokuGenerator.SIZE_NINE, SudokuGenerator.LIMIT_TYPE_DIAGONAL,
                    SudokuGenerator.LEVEL_VERY_HARD, null, puzzle);
        }

        //1-9-3
        for (int i = 0; i < SudokuData.sudoku_1_9_3_1.length; i++) {
            String puzzle = SudokuData.sudoku_1_9_3_1[i].toString();
            insertSudoku(db, SudokuGenerator.MODE_NORMAL, SudokuGenerator.SIZE_NINE, SudokuGenerator.LIMIT_TYPE_WINDOW,
                    SudokuGenerator.LEVEL_EASY, null, puzzle);
        }

        for (int i = 0; i < SudokuData.sudoku_1_9_3_2.length; i++) {
            String puzzle = SudokuData.sudoku_1_9_3_2[i].toString();
            insertSudoku(db, SudokuGenerator.MODE_NORMAL, SudokuGenerator.SIZE_NINE, SudokuGenerator.LIMIT_TYPE_WINDOW,
                    SudokuGenerator.LEVEL_MID, null, puzzle);
        }

        for (int i = 0; i < SudokuData.sudoku_1_9_3_3.length; i++) {
            String puzzle = SudokuData.sudoku_1_9_3_3[i].toString();
            insertSudoku(db, SudokuGenerator.MODE_NORMAL, SudokuGenerator.SIZE_NINE, SudokuGenerator.LIMIT_TYPE_WINDOW,
                    SudokuGenerator.LEVEL_HARD, null, puzzle);
        }

        for (int i = 0; i < SudokuData.sudoku_1_9_3_4.length; i++) {
            String puzzle = SudokuData.sudoku_1_9_3_4[i].toString();
            insertSudoku(db, SudokuGenerator.MODE_NORMAL, SudokuGenerator.SIZE_NINE, SudokuGenerator.LIMIT_TYPE_WINDOW,
                    SudokuGenerator.LEVEL_VERY_HARD, null, puzzle);
        }

        //1-9-4
        for (int i = 0; i < SudokuData.sudoku_1_9_4_1.length; i++) {
            String puzzle = SudokuData.sudoku_1_9_4_1[i].toString();
            insertSudoku(db, SudokuGenerator.MODE_NORMAL, SudokuGenerator.SIZE_NINE, SudokuGenerator.LIMIT_TYPE_PERCENTAGE,
                    SudokuGenerator.LEVEL_EASY, null, puzzle);
        }

        for (int i = 0; i < SudokuData.sudoku_1_9_4_2.length; i++) {
            String puzzle = SudokuData.sudoku_1_9_4_2[i].toString();
            insertSudoku(db, SudokuGenerator.MODE_NORMAL, SudokuGenerator.SIZE_NINE, SudokuGenerator.LIMIT_TYPE_PERCENTAGE,
                    SudokuGenerator.LEVEL_MID, null, puzzle);
        }

        for (int i = 0; i < SudokuData.sudoku_1_9_4_3.length; i++) {
            String puzzle = SudokuData.sudoku_1_9_4_3[i].toString();
            insertSudoku(db, SudokuGenerator.MODE_NORMAL, SudokuGenerator.SIZE_NINE, SudokuGenerator.LIMIT_TYPE_PERCENTAGE,
                    SudokuGenerator.LEVEL_HARD, null, puzzle);
        }

        for (int i = 0; i < SudokuData.sudoku_1_9_4_4.length; i++) {
            String puzzle = SudokuData.sudoku_1_9_4_4[i].toString();
            insertSudoku(db, SudokuGenerator.MODE_NORMAL, SudokuGenerator.SIZE_NINE, SudokuGenerator.LIMIT_TYPE_PERCENTAGE,
                    SudokuGenerator.LEVEL_VERY_HARD, null, puzzle);
        }
    }

    /**
     * 给表sudoku增加一条数据信息
     */
    public void insertSudoku(SQLiteDatabase db, SudokuGenerator.MinState state) {
        insertSudoku(db, state.mMode, state.mSize, state.mLimitType,
                state.mLevel, state.mIrregular, state.mPuzzle);
    }

    public void insertSudoku(SQLiteDatabase db,
                             int mode, int size, int limitType,
                             int level, @Nullable String irregular, String puzzle) {
        if (puzzle == null) {
            throw new RuntimeException("puzzle is null");
        }

        String sql;
        if (irregular == null) {
            sql = "insert into sudoku(mode, size, limit_type, level, puzzle)"
                    + " values (" + mode + "," + size + "," + limitType
                    + "," + level + ",'" + puzzle + "')";
        } else {
            sql = "insert into sudoku(mode, size, limit_type, level, irregular, puzzle)" +
            " values (" + mode + "," + size + "," + limitType
                    + "," + level + ",'" + irregular + "','" + puzzle + "')";
        }

        db.execSQL(sql);
        Mylog.d(TAG, "insertSudoku(), sql: " + sql);
    }

    /**
     * 更新
     */
    public void updateSudoku(SQLiteDatabase db, SudokuGame.GameState state) {
        String sql = "update sudoku set playing_puzzle = '" + state.mPlayingPuzzle + "' "
                + ", challenged = " + (state.mChallenged ? 1 : 0) + " "
                + ", challenged_time = " + state.mChallengedTime + " "
                + ", challenged_real_time = " + state.mChallengedRealTime + " "
                + " where id = " + state.mId;

        db.execSQL(sql);

        Mylog.d(TAG, "updateSudoku(), sql: " + sql);
    }

    /**
     * 从sudoku表查询一条信息
     */
    public SudokuGame.GameState selectSudoku(SQLiteDatabase db, SudokuGenerator.MinState state) {
        String[] args = {
                String.valueOf(state.mMode),
                String.valueOf(state.mSize),
                String.valueOf(state.mLimitType),
                String.valueOf(state.mLevel)
        };

        Mylog.d(TAG, "selectSudoku(), mMode(" + state.mMode + "), mSize(" + state.mSize
                + "), mLimitType(" + state.mLimitType + "), mLevel(" + state.mLevel + ")");

        Cursor cursor = db.rawQuery("select * from sudoku"
                + " where mode = ? and size = ? "
                + " and limit_type = ? and level = ? and (challenged is null or challenged = 0)", args);

        SudokuGame.GameState gameState = null;

        int num = cursor.getCount();
        if (num != 0) {
            cursor.moveToPosition(new Random().nextInt(num));
            gameState = new SudokuGame.GameState();
            gameState.mId = cursor.getInt(0);
            gameState.mMode = cursor.getInt(1);
            gameState.mSize = cursor.getInt(2);
            gameState.mLimitType = cursor.getInt(3);
            gameState.mLevel = cursor.getInt(4);
            gameState.mIrregular = cursor.getString(5);
            gameState.mPuzzle = cursor.getString(6);
//            gameState.mPlayingPuzzle = cursor.getString(7);
//            gameState.mChallenged = cursor.getInt(8) == 1;
//            gameState.mChallengedTime = cursor.getInt(9);
//            gameState.mChallengedRealTime = cursor.getInt(10);

            Mylog.d(TAG, "selectSudoku(), num : " + num);
            if (num < 100) {
                MyIntentService.startProduceSudokuService(App.getContext(), state);
            }
        }

        cursor.close();

        Mylog.d(TAG, "selectSudoku(), state : " + gameState.toString());

        return gameState;
    }

    public SudokuGame.GameState selectSudoku(SQLiteDatabase db, int id) {
        SudokuGame.GameState gameState = null;
        Cursor cursor = db.rawQuery("select * from sudoku where id = " + id, null);

        if (cursor.moveToFirst()) {
            gameState = new SudokuGame.GameState();
            gameState.mId = cursor.getInt(0);
            gameState.mMode = cursor.getInt(1);
            gameState.mSize = cursor.getInt(2);
            gameState.mLimitType = cursor.getInt(3);
            gameState.mLevel = cursor.getInt(4);
            gameState.mIrregular = cursor.getString(5);
            gameState.mPuzzle = cursor.getString(6);
            gameState.mPlayingPuzzle = cursor.getString(7);
            gameState.mChallenged = cursor.getInt(8) == 1;
            gameState.mChallengedTime = cursor.getInt(9);
            gameState.mChallengedRealTime = cursor.getInt(10);
        }

        cursor.close();

        Mylog.d(TAG, "selectSudoku(), state : " + gameState.toString());

        return gameState;
    }

    /**
     * 从playing表查询一条信息
     */
    public int selectPlaying(SQLiteDatabase db, SudokuGenerator.MinState state) {
        String[] args = {
                String.valueOf(state.mMode),
                String.valueOf(state.mSize),
                String.valueOf(state.mLimitType),
        };

        Mylog.d(TAG, "selectPlaying(), mMode(" + state.mMode + "), mSize("
                + state.mSize + "), mLimitType(" + state.mLimitType + ")");

        Cursor cursor = db.rawQuery("select id from playing"
                + " where mode = ? and size = ? and limit_type = ?", args);

        int id = -1;
        if (cursor.moveToFirst()) {
            id = cursor.getInt(0);
        }

        cursor.close();

        Mylog.d(TAG, "selectPlaying(), id = " + id);
        return id;
    }

    public void replacePlaying(SQLiteDatabase db, SudokuGenerator.MinState state, int id) {
        db.execSQL("replace into playing values ("
                + state.mMode + "," + state.mSize + "," + state.mLimitType + "," + id + ")");
    }



    /**
     * 包装函数， 查询一条数据记录，返回字符串
     * @return null:没有查询到； 非null：数独谜题字符串
     */
    public static SudokuGame.GameState selectLastSudoku(SudokuGenerator.MinState minState) {
        DbHelper dbHelper = new DbHelper();
        SQLiteDatabase db= dbHelper.getReadableDatabase();

        SudokuGame.GameState gameState;

        int id = dbHelper.selectPlaying(db, minState);
        if (id == -1) {
            gameState = dbHelper.selectSudoku(db, minState);
            if (gameState != null) {
                dbHelper.replacePlaying(db, minState, gameState.mId);
            }
        } else {
            gameState = dbHelper.selectSudoku(db, id);
        }

        db.close();
        return gameState;
    }

    public static SudokuGame.GameState selectNewSudoku(SudokuGenerator.MinState minState) {
        DbHelper dbHelper = new DbHelper();
        SQLiteDatabase db= dbHelper.getReadableDatabase();

        SudokuGame.GameState gameState = dbHelper.selectSudoku(db, minState);
        if (gameState != null) {
            dbHelper.replacePlaying(db, minState, gameState.mId);
        }

        db.close();
        return gameState;
    }

    public static void insertSudoku(SudokuGenerator.MinState state) {
        DbHelper dbHelper = new DbHelper();
        SQLiteDatabase db= dbHelper.getReadableDatabase();
        dbHelper.insertSudoku(db, state);
        db.close();
    }

    public static void updateSudoku(SudokuGame.GameState state) {
        DbHelper dbHelper = new DbHelper();
        SQLiteDatabase db= dbHelper.getReadableDatabase();
        dbHelper.updateSudoku(db, state);
        db.close();
    }
}

