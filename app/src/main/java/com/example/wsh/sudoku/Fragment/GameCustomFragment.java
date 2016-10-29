package com.example.wsh.sudoku.Fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.example.wsh.sudoku.Constants;
import com.example.wsh.sudoku.R;
import com.example.wsh.sudoku.customView.KeyBoardPopupWindow;
import com.example.wsh.sudoku.customView.SudokuView;
import com.example.wsh.sudoku.model.SudokuGenerator;
import com.example.wsh.sudoku.util.Mylog;


/**
 * Created by wsh on 16-7-16.
 */
public class GameCustomFragment extends BaseFragment {
    public static final String TAG = "GameCustomFragment";

    private FragmentCallback mCallbacks;

    private SudokuView mSudokuView;

    private KeyBoardPopupWindow mKeyBoardPopupWindow;




    @Override
    public void onAttach(Context context) {
        Mylog.d(TAG, "onAttach");
        super.onAttach(context);
        Mylog.d(TAG, "onAttach()");

        try {
            mCallbacks = (FragmentCallback) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implements Callbacks");
        }
    }

    @Override
    public void onDetach() {
        Mylog.d(TAG, "onDetach");
        super.onDetach();
        mCallbacks = null;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        Mylog.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        Mylog.d(TAG, "onCreateOptionsMenu");
        inflater.inflate(R.menu.menu_game_custom, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Mylog.d(TAG, "onOptionsItemSelected");
        if (item.getItemId() == R.id.action_complete_custom_puzzle) {
            SudokuGenerator.MinState minState = new SudokuGenerator.MinState();
            minState.mMode = SudokuGenerator.MODE_NORMAL;
            minState.mSize = SudokuGenerator.SIZE_NINE;
            minState.mLimitType = SudokuGenerator.LIMIT_TYPE_NORMAL;
            minState.mPuzzle = mSudokuView.getText();
            SudokuGenerator sudokuGenerator = new SudokuGenerator(minState);

            if (sudokuGenerator.isAllValid()) {
                Bundle bundle = new Bundle();
                bundle.putParcelable(GameFragment.EXTRA_SUDOKU_MIN_STATE, minState);

                mCallbacks.toggleFragment(this, bundle, Constants.gameFragmentObjTAG(minState));
                mSudokuView.initState();
            } else {
                Snackbar.make(mSudokuView, "puzzle is invalid", Snackbar.LENGTH_LONG).show();
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Mylog.d(TAG, "onCreateView");
        View v = inflater.inflate(R.layout.layout_game_custom, container, false);

        mSudokuView = (SudokuView) v.findViewById(R.id.sudokuView);
        mSudokuView.setListener(new SudokuView.Listener() {
            @Override
            public void onFirstClickWhenNewState() {
            }

            @Override
            public void onClick() {
                showKeyBoardPopupWindow();
            }

            @Override
            public void onDataSetChanged(int x, int y, int num) {
            }

            @Override
            public void onFilled() {
            }
        });

        return v;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mKeyBoardPopupWindow != null) {
            mKeyBoardPopupWindow.dismiss();
            mKeyBoardPopupWindow = null;
        }
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (hidden) {
            if (mKeyBoardPopupWindow != null) {
                mKeyBoardPopupWindow.dismiss();
                mKeyBoardPopupWindow = null;
            }
        }
    }

    private void showKeyBoardPopupWindow() {
        if (mKeyBoardPopupWindow != null) {
            if (!mKeyBoardPopupWindow.isShowing()) {
                mKeyBoardPopupWindow.showAtCustomerLocation(getView(),
                        Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM, true, true);
            }
            return;
        }

        Mylog.d(TAG, "showKeyBoardPopupWindow need new");

        mKeyBoardPopupWindow = new KeyBoardPopupWindow(getActivity());

        mKeyBoardPopupWindow.setListener(new KeyBoardPopupWindow.Listener() {
            @Override
            public void onNumClick(int num) {
                if (mKeyBoardPopupWindow.isEditMode()) {
                    mSudokuView.setCellCandidateNum(num);
                } else {
                    mSudokuView.setSelectedCellNum(num);
                }
            }

            @Override
            public void onClearClick() {
                mSudokuView.clearCurrentCellNum();
                mSudokuView.clearCurrentCellAllCandidate();
            }

            @Override
            public boolean notDismissedWhenOutSideEventOccur(MotionEvent event) {
                final int rawX = (int) event.getRawX();
                final int rawY = (int) event.getRawY();
                final int[] location = new int[2];
                mSudokuView.getLocationOnScreen(location);

                if (rawX >= location[0] && rawX <= location[0] + mSudokuView.getWidth()
                        && rawY >= location[1] && rawY <= location[1] + mSudokuView.getHeight()) {

                    //单元格可编辑，则固定键盘窗口
                    if ( ! mSudokuView.isReadOnly(rawX-location[0], rawY-location[1]) ) {
                        return true;
                    }
                }
                return false;
            }
        });

        //方向变化更换布局位置
        mKeyBoardPopupWindow.showAtCustomerLocation(getView(),
                Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM, true, true);
    }

    @Override
    protected void setTitle() {
        String str = getResources().getString(R.string.game_custom_new);
        mCallbacks.setToolbarTitle(str);
    }
}
