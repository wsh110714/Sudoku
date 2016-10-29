package com.example.wsh.sudoku.Fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.content.LocalBroadcastManager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Chronometer;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.wsh.sudoku.Constants;
import com.example.wsh.sudoku.DataLoader;
import com.example.wsh.sudoku.R;
import com.example.wsh.sudoku.model.SudokuGame;
import com.example.wsh.sudoku.customView.KeyboardView;
import com.example.wsh.sudoku.db.DbHelper;
import com.example.wsh.sudoku.model.SudokuGenerator;
import com.example.wsh.sudoku.service.MyIntentService;
import com.example.wsh.sudoku.util.ChronometerWrap;
import com.example.wsh.sudoku.util.Mylog;
import com.example.wsh.sudoku.customView.SudokuView;
import com.example.wsh.sudoku.util.Util;


/**
 * Created by wsh on 16-6-27.
 * 数独谜题主界面
 */
public class GameFragment extends BaseFragment
        implements LoaderManager.LoaderCallbacks<SudokuGame.GameState> {
    public static final String TAG = "GameFragment";

    public static final String EXTRA_SUDOKU_MIN_STATE
            = "com.example.wsh.soduku.EXTRA_SUDOKU_MIN_STATE";

    private FragmentCallback mCallbacks;

    private KeyboardView mKeyboardView;

    private SudokuView mSudokuView;

    private MenuItem mHintItem;

    private Loader<SudokuGame.GameState> mLoader;
    private boolean firstLoader = true;

    private static final int LOAD_ID = 0;

    private BroadcastReceiver mPuzzleAnswerReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //每个对象只接受自己注册的广播，只有一个，不用区分
            //String action = intent.getAction();

            String answer = intent.getStringExtra(Constants.EXTRA_SUDOKU_PUZZLE_ANSWER);
            if (answer != null) {
                if (mSudokuGame != null) {
                    mSudokuGame.setAnswer(answer);
                }
                Mylog.d(getTag(), "setAnswer: " + answer);
            }
        }
    };

    private ChronometerWrap mChronometerWrap;

    SudokuGenerator.MinState mMinState;
    SudokuGame mSudokuGame;





    public static GameFragment newInstance(Bundle bundle) {
        GameFragment fragment = new GameFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Mylog.d(getTag(), "onAttach()");

        if (context instanceof FragmentCallback) {
            mCallbacks = (FragmentCallback) context;
        } else {
            throw new ClassCastException(context.toString()
                    + " must implements Callbacks");
        }
    }

    @Override
    public void onDetach() {
        Mylog.d(getTag(), "onDetach");
        super.onDetach();
        mCallbacks = null;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Mylog.d(getTag(), "onCreate");

        mMinState = getArguments().getParcelable(EXTRA_SUDOKU_MIN_STATE);
        mSudokuGame = new SudokuGame(mMinState);

        IntentFilter filter = new IntentFilter(Constants.ACTION_broadcastAnswer(mMinState));
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(
                mPuzzleAnswerReceiver, filter);

        setHasOptionsMenu(true);
    }

    private int getGameLevel() {
        //加载设置
        SharedPreferences sharePref =
                PreferenceManager.getDefaultSharedPreferences(getActivity());
        String levelStr = sharePref.getString(SettingsFragment.PREF_LEVEL_LIST, null);
        int level = levelStr == null ? SudokuGenerator.LEVEL_MID : Integer.parseInt(levelStr);
        Mylog.d(TAG, "getGameLevel, level = " + level);
        return level;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        Mylog.d(getTag(), "onCreateView");
        LinearLayout v = (LinearLayout) inflater.inflate(R.layout.layout_game, container, false);

        //计时器
        if (mChronometerWrap != null) {
            Mylog.d(TAG, "savedInstanceState == null && mChronometerWrap != null");
            mChronometerWrap.setupChronometer((Chronometer) v.findViewById(R.id.chronometer));
        } else {
            Mylog.d(TAG, "savedInstanceState == null && mChronometerWrap == null");
            mChronometerWrap = new ChronometerWrap((Chronometer) v.findViewById(R.id.chronometer));
        }

        //键盘
        mKeyboardView = new KeyboardView(getActivity(), mSudokuGame.mSize);
        mKeyboardView.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        mKeyboardView.setGravity(Gravity.CENTER);
        mKeyboardView.setClickListener(new KeyboardView.ClickListener() {
            @Override
            public void onNumClick(int num) {
                if (mKeyboardView.isEditMode()) {
                    mSudokuView.setCellCandidateNum(num);
                } else {
                    mSudokuView.setSelectedCellNum(num);
                }
            }

            @Override
            public void onClearClick() {
                Mylog.d(TAG, "onClearClick");
                mSudokuView.clearCurrentCellNum();
                mSudokuView.clearCurrentCellAllCandidate();
            }

            @Override
            public void onEndEditMode() {
                //mSudokuView.setSelectedTag(false);
            }
        });

        //数独面板
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.gravity = Gravity.CENTER;
        layoutParams.setMargins(2,1,1,1);

        mSudokuView = new SudokuView(getActivity(), mSudokuGame.getMinState());
        mSudokuView.setLayoutParams(layoutParams);
        mSudokuView.setPadding(16,16,16,16);
        mSudokuView.setListener(new SudokuView.Listener() {
            @Override
            public void onFirstClickWhenNewState() {
                if (mChronometerWrap != null) {
                    mChronometerWrap.start();
                }
            }

            @Override
            public void onClick() {
            }

            @Override
            public void onDataSetChanged(int x, int y, int num) {
                if (mSudokuGame != null) {
                    mSudokuGame.setSudokuData(x, y, num);
                }
            }

            @Override
            public void onFilled() {
            }
        });

        v.addView(mSudokuView);
        v.addView(mKeyboardView);

        return v;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Mylog.d(getTag(), "onActivityCreated, puzzle = " + mSudokuGame.mPuzzle);

        if (mSudokuGame.mPuzzle == null) {
            getLoaderManager().initLoader(LOAD_ID, null, this);
        } else {
            startGame();
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        Mylog.d(getTag(), "onCreateOptionsMenu");
        inflater.inflate(R.menu.menu_game, menu);

        mHintItem = menu.findItem(R.id.action_hint);
        if (mSudokuGame.mSize < SudokuGenerator.SIZE_SEVEN) { //4,5,6宫不提示
            mHintItem.setVisible(false);
        }

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Mylog.d(getTag(), "onOptionsItemSelected");
        switch (item.getItemId()) {
            case R.id.action_check:
                if (mSudokuGame != null) {
                    mSudokuView.setWrongCell(mSudokuGame.check());
                }
                break;
            case R.id.action_clearColor:
                mSudokuView.clearColorAndSelected();
                break;
            case R.id.action_hint:
                mSudokuView.switchShowCandidateNumTag();
                switchHintItem(!item.isChecked());
                break;
            case R.id.action_newGame:
                if (mLoader != null) {
                    mLoader.onContentChanged();
                } else {
                    getLoaderManager().initLoader(LOAD_ID, null, this);
                }
                break;
            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    public void switchHintItem(boolean checked) {
        if (mHintItem == null) {
            return;
        }

        mHintItem.setChecked(checked);
        if (mHintItem.isChecked()) {
            mHintItem.setIcon(R.drawable.ic_full);
        } else {
            mHintItem.setIcon(R.drawable.ic_empty);
        }
    }

    @Override
    public void onResume() {
        Mylog.d(getTag(), "onResume");
        super.onResume();
        if (mChronometerWrap != null) {
            mChronometerWrap.start();
        }
    }

    @Override
    public void onPause() {
        Mylog.d(getTag(), "onPause");
        super.onPause();
        pauseGame();
    }

    @Override
    public void onStop() {
        Mylog.d(getTag(), "onStop");
        super.onStop();
    }

    @Override
    public void onDestroyView() {
        Mylog.d(getTag(), "onDestroyView");
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        Mylog.d(getTag(), "onDestroy");
        super.onDestroy();
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mPuzzleAnswerReceiver);
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        Mylog.d(getTag(), "onHiddenChanged()");
        if (hidden) {
            pauseGame();
        } else {
            if (mChronometerWrap != null) {
                mChronometerWrap.start();
            }
        }
    }

    /**
     * 更新数据
     * @param args 参数
     */
    @Override
    public void updateUIDataFromArguments(final Bundle args) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mMinState = args.getParcelable(EXTRA_SUDOKU_MIN_STATE);
                if (mMinState != null) {
                    mSudokuGame = new SudokuGame(mMinState);
                }
                if (mSudokuGame.mPuzzle != null) {
                    startGame();
                }
            }
        });
    }

    /**
     * 设置toolbar标题
     */
    protected void setTitle() {
        String str = null;

        if (mSudokuGame.mMode == SudokuGenerator.MODE_IRREGULAR) {
            switch(mSudokuGame.mSize) {
                case SudokuGenerator.SIZE_FOUR:
                    str = getResources().getString(R.string.game_irregular_four);
                    break;
                case SudokuGenerator.SIZE_FIVE:
                    str = getResources().getString(R.string.game_irregular_five);
                    break;
                case SudokuGenerator.SIZE_SIX:
                    str = getResources().getString(R.string.game_irregular_six);
                    break;
                case SudokuGenerator.SIZE_SEVEN:
                    str = getResources().getString(R.string.game_irregular_seven);
                    break;
                case SudokuGenerator.SIZE_EIGHT:
                    str = getResources().getString(R.string.game_irregular_eight);
                    break;
                case SudokuGenerator.SIZE_NINE:
                    switch(mSudokuGame.mLimitType) {
                        case SudokuGenerator.LIMIT_TYPE_NORMAL:
                            str = getResources().getString(R.string.game_irregular_nine);
                            break;
                        default:
                            break;
                    }
                    break;
                default:
                    break;
            }
        } else {
            switch(mSudokuGame.mSize) {
                case SudokuGenerator.SIZE_FOUR:
                    str = getResources().getString(R.string.game_four);
                    break;
                case SudokuGenerator.SIZE_SIX:
                    if (mSudokuGame.mLimitType == SudokuGenerator.LIMIT_TYPE_DIAGONAL) {
                        str = getResources().getString(R.string.game_six_diagonal);
                    } else {
                        str = getResources().getString(R.string.game_six);
                    }
                    break;
                case SudokuGenerator.SIZE_EIGHT:
                    if (mSudokuGame.mLimitType == SudokuGenerator.LIMIT_TYPE_DIAGONAL) {
                        str = getResources().getString(R.string.game_eight_diagonal);
                    } else {
                        str = getResources().getString(R.string.game_eight);
                    }
                    break;
                case SudokuGenerator.SIZE_NINE:
                    switch(mSudokuGame.mLimitType) {
                        case SudokuGenerator.LIMIT_TYPE_DIAGONAL:
                            str = getResources().getString(R.string.game_nine_diagonal);
                            break;
                        case SudokuGenerator.LIMIT_TYPE_WINDOW:
                            str = getResources().getString(R.string.game_nine_window);
                            break;
                        case SudokuGenerator.LIMIT_TYPE_PERCENTAGE:
                            str = getResources().getString(R.string.game_nine_percentage);
                            break;
                        case SudokuGenerator.LIMIT_TYPE_NORMAL:
                        default:
                            str = getResources().getString(R.string.game_nine);
                            break;
                    }
                    break;
                default:
                    break;
            }
        }

        if (str != null) {
            mCallbacks.setToolbarTitle(str);
        }
    }

    /**
     * 开始游戏
     */
    public void startGame() {
        Mylog.d(getTag(), "startGame()");

        mSudokuView.initState();
        mSudokuView.attachPuzzle(mSudokuGame.mPuzzle, mSudokuGame.mIrregular, mSudokuGame.mPlayingPuzzle);

        if (mChronometerWrap != null) {
            mChronometerWrap.init(mSudokuGame.mChallengedTime);
        }

        switchHintItem(false);

        //求解答案，备用
        MyIntentService.startSolveSudokuService(getActivity(), mSudokuGame.getMinState());
    }

    public void pauseGame() {
        if (mChronometerWrap != null) {
            mChronometerWrap.stop();
        }
        SudokuGame.GameState gameState = mSudokuGame.getGameState();
        gameState.mPlayingPuzzle = mSudokuGame.getPlayingPuzzle();
        gameState.mChallengedTime = (int) mChronometerWrap.getTime();
        gameState.mChallengedRealTime = (int)System.currentTimeMillis();

        Mylog.d(getTag(), "pauseGame(), mPlayingPuzzle = " + gameState.mPlayingPuzzle
                + ", mChallengedTime = " + gameState.mChallengedTime
                + ", mChallengedRealTime = " + gameState.mChallengedRealTime);

        MyIntentService.startUpdateSudokuService(getActivity(), gameState);
    }

    /**
     * 游戏结束
     */
    public void endGame() {
        Mylog.d(getTag(), "endGame()");
        mSudokuView.lockState();
        if (mChronometerWrap != null) {
            mChronometerWrap.stop();
        }
        Toast.makeText(getActivity(), "Well Done", Toast.LENGTH_LONG).show();
    }

    @Override
    public Loader<SudokuGame.GameState> onCreateLoader(int id, Bundle args) {
        mLoader = new DataLoader<SudokuGame.GameState>(getActivity()) {

            //do in work thread
            @Override
            public SudokuGame.GameState loadInBackground() {
                Mylog.d(TAG, "loadInBackground(),  producePuzzle, mode("
                        + mSudokuGame.mMode + ") size(" + mSudokuGame.mSize
                        + ") limitType(" + mSudokuGame.mLimitType + ")");

                SudokuGenerator.MinState minState = mSudokuGame.getMinState();
                minState.mLevel = SudokuGenerator.modifyLevel(minState.mSize, getGameLevel());

                SudokuGame.GameState gameState;

                if (firstLoader) {
                    gameState = DbHelper.selectLastSudoku(minState);
                    firstLoader = false;
                } else {
                    gameState = DbHelper.selectNewSudoku(minState);
                }

                return gameState;
            }
        };

        return mLoader;
    }

    @Override
    public void onLoadFinished(Loader<SudokuGame.GameState> loader, SudokuGame.GameState data) {
        Mylog.d(TAG, "onLoadFinished(), data: " + data.toString());

        mSudokuGame = new SudokuGame(data);

        startGame();
    }

    @Override
    public void onLoaderReset(Loader loader) {

    }
}
