package com.example.wsh.sudoku;

import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.example.wsh.sudoku.Fragment.BaseFragment;
import com.example.wsh.sudoku.Fragment.FragmentCallback;
import com.example.wsh.sudoku.Fragment.GameCustomFragment;
import com.example.wsh.sudoku.Fragment.GameFragment;
import com.example.wsh.sudoku.model.SudokuGenerator;
import com.example.wsh.sudoku.util.Mylog;

public class MainActivity extends AppCompatActivity
        implements FragmentCallback {
    private static final String TAG = "MainActivity";

    private static final String STATE_CURRENT_FRAGMENT_TAG = "STATE_CURRENT_FRAGMENT_TAG";

    private DrawerLayout mDrawerLayout;

    private ActionBarDrawerToggle mDrawerToggle;

    private Toolbar mToolbar;

    private BaseFragment mCurrentFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Mylog.d(TAG, "onCreate()");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        initActionbar(mToolbar);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        mDrawerToggle = new ActionBarDrawerToggle(
                this,
                mDrawerLayout,
                mToolbar,
                R.string.slide_menu_open,
                R.string.slide_menu_close) {

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                Mylog.d(TAG, "onDrawerOpened");
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                Mylog.d(TAG, "onDrawerClosed");
            }
        };
        mDrawerToggle.syncState();
        mDrawerLayout.addDrawerListener(mDrawerToggle);

        NavigationView navigationView = (NavigationView)
                mDrawerLayout.findViewById(R.id.navigation_view);
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem item) {
                        Mylog.d(TAG, "onNavigationItemSelected, menuItem id = " + item.getItemId());
                        Bundle bundle = new Bundle();
                        SudokuGenerator.MinState state = new SudokuGenerator.MinState();

                        switch (item.getItemId()) {
                            case R.id.item_start_game_nine:
                                state.mMode = SudokuGenerator.MODE_NORMAL;
                                state.mSize = SudokuGenerator.SIZE_NINE;
                                state.mLimitType = SudokuGenerator.LIMIT_TYPE_NORMAL;
                                bundle.putParcelable(GameFragment.EXTRA_SUDOKU_MIN_STATE, state);
                                toggleFragment(null, bundle, Constants.gameFragmentObjTAG(state));
                                break;
                            case R.id.item_start_game_nine_diagonal:
                                state.mMode = SudokuGenerator.MODE_NORMAL;
                                state.mSize = SudokuGenerator.SIZE_NINE;
                                state.mLimitType = SudokuGenerator.LIMIT_TYPE_DIAGONAL;
                                bundle.putParcelable(GameFragment.EXTRA_SUDOKU_MIN_STATE, state);
                                toggleFragment(null, bundle, Constants.gameFragmentObjTAG(state));
                                break;
                            case R.id.item_start_game_nine_window:
                                state.mMode = SudokuGenerator.MODE_NORMAL;
                                state.mSize = SudokuGenerator.SIZE_NINE;
                                state.mLimitType = SudokuGenerator.LIMIT_TYPE_WINDOW;
                                bundle.putParcelable(GameFragment.EXTRA_SUDOKU_MIN_STATE, state);
                                toggleFragment(null, bundle, Constants.gameFragmentObjTAG(state));
                                break;
                            case R.id.item_start_game_nine_percentage:
                                state.mMode = SudokuGenerator.MODE_NORMAL;
                                state.mSize = SudokuGenerator.SIZE_NINE;
                                state.mLimitType = SudokuGenerator.LIMIT_TYPE_PERCENTAGE;
                                bundle.putParcelable(GameFragment.EXTRA_SUDOKU_MIN_STATE, state);
                                toggleFragment(null, bundle, Constants.gameFragmentObjTAG(state));
                                break;
                            case R.id.item_start_game_eight:
                                state.mMode = SudokuGenerator.MODE_NORMAL;
                                state.mSize = SudokuGenerator.SIZE_EIGHT;
                                state.mLimitType = SudokuGenerator.LIMIT_TYPE_NORMAL;
                                bundle.putParcelable(GameFragment.EXTRA_SUDOKU_MIN_STATE, state);
                                toggleFragment(null, bundle, Constants.gameFragmentObjTAG(state));
                                break;
                            case R.id.item_start_game_eight_diagonal:
                                state.mMode = SudokuGenerator.MODE_NORMAL;
                                state.mSize = SudokuGenerator.SIZE_EIGHT;
                                state.mLimitType = SudokuGenerator.LIMIT_TYPE_DIAGONAL;
                                bundle.putParcelable(GameFragment.EXTRA_SUDOKU_MIN_STATE, state);
                                toggleFragment(null, bundle, Constants.gameFragmentObjTAG(state));
                                break;
                            case R.id.item_start_game_six:
                                state.mMode = SudokuGenerator.MODE_NORMAL;
                                state.mSize = SudokuGenerator.SIZE_SIX;
                                state.mLimitType = SudokuGenerator.LIMIT_TYPE_NORMAL;
                                bundle.putParcelable(GameFragment.EXTRA_SUDOKU_MIN_STATE, state);
                                toggleFragment(null, bundle, Constants.gameFragmentObjTAG(state));
                                break;
                            case R.id.item_start_game_six_diagonal:
                                state.mMode = SudokuGenerator.MODE_NORMAL;
                                state.mSize = SudokuGenerator.SIZE_SIX;
                                state.mLimitType = SudokuGenerator.LIMIT_TYPE_DIAGONAL;
                                bundle.putParcelable(GameFragment.EXTRA_SUDOKU_MIN_STATE, state);
                                toggleFragment(null, bundle, Constants.gameFragmentObjTAG(state));
                                break;
                            case R.id.item_start_game_four:
                                state.mMode = SudokuGenerator.MODE_NORMAL;
                                state.mSize = SudokuGenerator.SIZE_FOUR;
                                state.mLimitType = SudokuGenerator.LIMIT_TYPE_NORMAL;
                                bundle.putParcelable(GameFragment.EXTRA_SUDOKU_MIN_STATE, state);
                                toggleFragment(null, bundle, Constants.gameFragmentObjTAG(state));
                                break;

                            //锯齿模式
                            case R.id.item_start_game_irregular_nine:
                                state.mMode = SudokuGenerator.MODE_IRREGULAR;
                                state.mSize = SudokuGenerator.SIZE_NINE;
                                state.mLimitType = SudokuGenerator.LIMIT_TYPE_NORMAL;
                                bundle.putParcelable(GameFragment.EXTRA_SUDOKU_MIN_STATE, state);
                                toggleFragment(null, bundle, Constants.gameFragmentObjTAG(state));
                                break;
                            case R.id.item_start_game_irregular_eight:
                                state.mMode = SudokuGenerator.MODE_IRREGULAR;
                                state.mSize = SudokuGenerator.SIZE_EIGHT;
                                state.mLimitType = SudokuGenerator.LIMIT_TYPE_NORMAL;
                                bundle.putParcelable(GameFragment.EXTRA_SUDOKU_MIN_STATE, state);
                                toggleFragment(null, bundle, Constants.gameFragmentObjTAG(state));
                                break;
                            case R.id.item_start_game_irregular_seven:
                                state.mMode = SudokuGenerator.MODE_IRREGULAR;
                                state.mSize = SudokuGenerator.SIZE_SEVEN;
                                state.mLimitType = SudokuGenerator.LIMIT_TYPE_NORMAL;
                                bundle.putParcelable(GameFragment.EXTRA_SUDOKU_MIN_STATE, state);
                                toggleFragment(null, bundle, Constants.gameFragmentObjTAG(state));
                                break;
                            case R.id.item_start_game_irregular_six:
                                state.mMode = SudokuGenerator.MODE_IRREGULAR;
                                state.mSize = SudokuGenerator.SIZE_SIX;
                                state.mLimitType = SudokuGenerator.LIMIT_TYPE_NORMAL;
                                bundle.putParcelable(GameFragment.EXTRA_SUDOKU_MIN_STATE, state);
                                toggleFragment(null, bundle, Constants.gameFragmentObjTAG(state));
                                break;
                            case R.id.item_start_game_irregular_five:
                                state.mMode = SudokuGenerator.MODE_IRREGULAR;
                                state.mSize = SudokuGenerator.SIZE_FIVE;
                                state.mLimitType = SudokuGenerator.LIMIT_TYPE_NORMAL;
                                bundle.putParcelable(GameFragment.EXTRA_SUDOKU_MIN_STATE, state);
                                toggleFragment(null, bundle, Constants.gameFragmentObjTAG(state));
                                break;
                            case R.id.item_start_game_irregular_four:
                                state.mMode = SudokuGenerator.MODE_IRREGULAR;
                                state.mSize = SudokuGenerator.SIZE_FOUR;
                                state.mLimitType = SudokuGenerator.LIMIT_TYPE_NORMAL;
                                bundle.putParcelable(GameFragment.EXTRA_SUDOKU_MIN_STATE, state);
                                toggleFragment(null, bundle, Constants.gameFragmentObjTAG(state));
                                break;
                            case R.id.item_start_custom_game_fragment:
                                toggleFragment(null, null, GameCustomFragment.TAG);
                                break;
                            case R.id.item_settings:
                                mDrawerLayout.closeDrawer(GravityCompat.START);
                                Intent i = new Intent(MainActivity.this, SettingsActivity.class);
                                startActivity(i);
                                return true;
                            default:
                                break;
                        }

                        mDrawerLayout.closeDrawers();
                        return true;
                    }
                }
        );

        if (savedInstanceState != null) {
            String tag = savedInstanceState.getString(STATE_CURRENT_FRAGMENT_TAG, null);
            Mylog.d(TAG, "tag: " + tag);
            if (tag != null) {
                FragmentManager fm = getSupportFragmentManager();
                mCurrentFragment = (BaseFragment) fm.findFragmentByTag(tag);
            }
        } else {
            startFragment();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mCurrentFragment != null) {
            outState.putString(STATE_CURRENT_FRAGMENT_TAG, mCurrentFragment.getTag());
            Mylog.d(TAG, "onSaveInstanceState(), tag = " + mCurrentFragment.getTag());
        }
    }

    public void initActionbar(Toolbar toolbar) {
        toolbar.setNavigationIcon(R.drawable.ic_menu);
        toolbar.setTitle(R.string.app_name);
        setSupportActionBar(toolbar);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onPostCreate(Bundle savedInstanceState, PersistableBundle persistentState) {
        super.onPostCreate(savedInstanceState, persistentState);
        mDrawerToggle.syncState();
    }

    public void startFragment() {
        SudokuGenerator.MinState state = new SudokuGenerator.MinState();
        state.mMode = SudokuGenerator.MODE_NORMAL;
        state.mSize = SudokuGenerator.SIZE_NINE;
        state.mLimitType = SudokuGenerator.LIMIT_TYPE_NORMAL;
        Bundle bundle = new Bundle();
        bundle.putParcelable(GameFragment.EXTRA_SUDOKU_MIN_STATE, state);
        toggleFragment(null, bundle, Constants.gameFragmentObjTAG(state));
    }

    /**
     * 切换 fragment
     * @param toTag 需要切换到的fragment的tag
     * @param from 需要隐藏的framgnet的引用
     * @param toArgs 同时需要传递给fragment的参数，如果是新建，作为新建参数; 如果已存在，则直接根据参数更新ui或者数据
     */
    @Override
    public void toggleFragment(@Nullable BaseFragment from,
                               @Nullable Bundle toArgs, String toTag) {
        FragmentManager fm = getSupportFragmentManager();

        BaseFragment to = (BaseFragment) fm.findFragmentByTag(toTag);

        if (to == null) {
            if (toTag.equals(GameCustomFragment.TAG)) {
                to = new GameCustomFragment();

            } else if (toTag.substring(0, GameFragment.TAG.length()).equals(GameFragment.TAG)){
                if (toArgs != null) {
                    to = GameFragment.newInstance(toArgs);
                }
            }
        } else {
            if (toArgs != null) {
                to.updateUIDataFromArguments(toArgs);
            }
        }

        if (to == null) {
            Mylog.d(TAG, "toggleFragment(), to == null");
            return;
        }

        if (from == null && mCurrentFragment != null) {
            from = mCurrentFragment;
        }

        if (from != null) { //隐藏方式替换
            if (!to.isAdded()) {
                Mylog.d(TAG, "toggleFragment add " + toTag);
                fm.beginTransaction()
                        .remove(from)
                        .add(R.id.fragmentContainer, to, toTag) //必须按照tag方式添加
                        .commit();
            } else if (to.isHidden()) {
                Mylog.d(TAG, "toggleFragment show " + toTag);
                fm.beginTransaction()
                        .remove(from)
                        .show(to)
                        .commit();
            }
        } else { //直接增加
            if (!to.isAdded()) {
                fm.beginTransaction()
                        .add(R.id.fragmentContainer, to, toTag)
                        .commit();
            } else if (to.isHidden()){
                fm.beginTransaction()
                        .show(to)
                        .commit();
            }
        }
        mCurrentFragment = to;
    }

    public void setToolbarTitle(String title) {
        mToolbar.setTitle(title);
    }

    @Override
    protected void onPause() {
        Mylog.d(TAG, "onPause");
        super.onPause();
    }

    @Override
    protected void onStop() {
        Mylog.d(TAG, "onStop");
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        Mylog.d(TAG, "onDestroy");
        super.onDestroy();
    }
}
