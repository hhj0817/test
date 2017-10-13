package com.example.test.test.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.test.test.Constants;
import com.example.test.test.R;
import com.example.test.test.lock.GestureLockLayout;
import com.example.test.test.lock.QQLockView;
import com.example.test.test.manager.AppLockManager;
import com.example.test.test.presenter.UnlockPresenter;
import com.example.test.test.presenter.UnlockPresenterImpl;
import com.example.test.test.view.UnlockView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class UnlockActivity extends AppCompatActivity implements UnlockView {

    public static final String TYPE = "type";
    public static final int TYPE_FIRST_ENTER = 1;
    public static final int TYPE_ENTER = 2;
    public static final int TYPE_UNLOCK = 3;
    //设置里改密码
    public static final int TYPE_CHANGE = 4;
    //忘记密码
    public static final int TYPE_RESET = 5;

    @BindView(R.id.gesture_lock)
    GestureLockLayout mGestureLock;
    @BindView(R.id.hint)
    TextView mHint;
    @BindView(R.id.btn_confirm)
    TextView mBtnConfirm;
    @BindView(R.id.iv_switcher)
    ImageView mIvSwitcher;
    @BindView(R.id.iv_forget_password)
    ImageView mIvForgetPassword;

    private int mType;
    private int mLastType;
    private String mLockType;
    private UnlockPresenter mUnlockPresenter;
    private Handler mHandler = new Handler();
    private AppLockManager mAppLockManager;
    private Vibrator mVibrator;
    private boolean mIsVibrationOn;
    private String mCurrentPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_unlock);
        ButterKnife.bind(this);
        Intent intent = getIntent();
        mType = intent.getIntExtra(TYPE, TYPE_FIRST_ENTER);
        Log.d("hhh", "onCreate--type:" + mType);
        mUnlockPresenter = new UnlockPresenterImpl(this);
        mUnlockPresenter.onCreate();
    }

    //点击忘记密码,并且验证过安全问题的时候会走这里
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        mLastType = mType;
        mType = intent.getIntExtra(TYPE, TYPE_FIRST_ENTER);
        Log.d("hhh", "onNewIntent--type:" + mType);
        mUnlockPresenter.onNewIntent();
    }

    @Override
    public void onInit() {
        mAppLockManager = AppLockManager.getInstance();
        //震动反馈
        mIsVibrationOn = mAppLockManager.isVibrationOn();
        if (mIsVibrationOn) {
            mVibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        }
        //切换锁类型按钮
        String gesturePassword = mAppLockManager.getGesturePassword();
        String digitPassword = mAppLockManager.getDigitPassword();
        if (!TextUtils.isEmpty(gesturePassword) && !TextUtils.isEmpty(digitPassword)) {
            mIvSwitcher.setVisibility(View.VISIBLE);
        }
        //忘记密码按钮
        if(mType==TYPE_ENTER||mType==TYPE_UNLOCK){
            mIvForgetPassword.setVisibility(View.VISIBLE);
        }else{
            mIvForgetPassword.setVisibility(View.GONE);
        }
        //锁类型
        mLockType = mAppLockManager.getLockType();
        if (mLockType.equals(Constants.AppLock.TYPE_GESTURE)) {
            initGestureLock();
        } else {

        }
    }

    private void initGestureLock() {
        //图案路径
        boolean patternTrackShow = mAppLockManager.isPatternTrackShow();
        if (!patternTrackShow) {
            mGestureLock.setPatternShow(false);
        }
        //3 x 3
        mGestureLock.setDotCount(3);
        //最小连接数
        mGestureLock.setMinCount(4);
        //样式
        mGestureLock.setLockView(new QQLockView(this));
        //最大尝试次数
        mGestureLock.setTryTimes(Integer.MAX_VALUE);
        if (mType == TYPE_FIRST_ENTER) {
            mGestureLock.setMode(GestureLockLayout.RESET_MODE);
            mHint.setText(R.string.first_photo);
            mIvSwitcher.setVisibility(View.VISIBLE);
        } else if (mType == TYPE_RESET || mType == TYPE_CHANGE) {
            mGestureLock.setMode(GestureLockLayout.RESET_MODE);
            mHint.setText(R.string.new_photo);
        } else {
            //ENTER  UNLOCK
            mGestureLock.setMode(GestureLockLayout.VERIFY_MODE);
            String password = mAppLockManager.getGesturePassword();
            Log.d("hhh", "手势密码：" + password);
            mGestureLock.setAnswer(password);
        }
        mGestureLock.setOnLockVerifyListener(new GestureLockLayout.OnLockVerifyListener() {
            @Override
            public void onGestureSelected(int id) {
                if (mIsVibrationOn) {
                    mVibrator.vibrate(50);
                }
            }

            @Override
            public void onGestureFinished(boolean isMatched) {
                if (isMatched) {
                    if (mType == TYPE_ENTER) {
                        gotoAppLockActivity();
                        finish();
                    } else if (mType == TYPE_UNLOCK) {
                        mAppLockManager.setHasUnlock(true);
                        mAppLockManager.setLastUnlockTime(System.currentTimeMillis());
                        finish();
                    }
                } else {
                    Toast.makeText(UnlockActivity.this, R.string.failure, Toast.LENGTH_SHORT).show();
                    resetGesture();
                }
            }

            @Override
            public void onGestureTryTimesBoundary() {
//                mHint.setText("超过最大输入次数");
//                mGestureLock.setTouchable(false);
            }
        });
        mGestureLock.setOnLockResetListener(new GestureLockLayout.OnLockResetListener() {

            @Override
            public void onConnectCountUnmatched(int connectCount, int minCount) {
                mHint.setText(R.string.pic_count_error);
                resetGesture();
            }

            @Override
            public void onFirstPasswordFinished(List<Integer> answerList) {
                resetGesture();
                if (mType == TYPE_FIRST_ENTER) {
                    mHint.setText(R.string.second_photo);
                } else {
                    mHint.setText(R.string.new_second);
                }
            }

            @Override
            public void onSetPasswordFinished(boolean isMatched, List<Integer> answerList) {
                if (isMatched) {
                    mCurrentPassword = answerList.toString();
                    //保存密码
                    if (mType == TYPE_FIRST_ENTER) {
                        mHint.setText(R.string.ok_btn_confirm);
                        mBtnConfirm.setVisibility(View.VISIBLE);
                        mIvSwitcher.setVisibility(View.GONE);
                    } else if (mType == TYPE_CHANGE) {
                        mBtnConfirm.setVisibility(View.VISIBLE);
                        mHint.setText(R.string.ok_btn_confirm);
                    } else if (mType == TYPE_RESET) {
                        mBtnConfirm.setVisibility(View.VISIBLE);
                        mHint.setText(R.string.ok_btn_confirm);
                    }
                } else {
                    Toast.makeText(UnlockActivity.this, R.string.pwdnotequal, Toast.LENGTH_SHORT).show();
                    mHint.setText(R.string.new_photo);
                    mGestureLock.setAnswer();
                    resetGesture();
                }
            }
        });
    }

    @OnClick({R.id.btn_confirm, R.id.iv_switcher, R.id.iv_forget_password})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_confirm:
                mAppLockManager.setAppLockGesturePassword(this, mCurrentPassword);
                Log.d("hhh", "保存的密码:" + mCurrentPassword);
                if (mType == TYPE_FIRST_ENTER) {
                    setSafeQuestionAnswer();
                    finish();
                } else if (mType == TYPE_CHANGE) {
                    finish();
                } else if (mType == TYPE_RESET) {
                    //解锁界面忘记密码
                    if (mLastType == TYPE_UNLOCK) {
                        finish();
                    } else if (mLastType == TYPE_ENTER) {
                        //进入应用锁时忘记密码
                        gotoAppLockActivity();
                        finish();
                    }
                }
                break;
            case R.id.iv_switcher:

                break;
            case R.id.iv_forget_password:
                resetPassword();
                break;
        }
    }


    private void resetGesture() {
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mGestureLock.resetGesture();
            }
        }, 200);
    }

    private void gotoAppLockActivity() {
        Intent intent = new Intent(UnlockActivity.this, AppLockActivity.class);
        startActivity(intent);
    }

    private void resetPassword() {
        Intent intent = new Intent(UnlockActivity.this, SafeQuestionActivity.class);
        if (mType == TYPE_UNLOCK) {
            intent.putExtra(SafeQuestionActivity.FROM_WHERE, SafeQuestionActivity.FROM_UNLOCK);
        } else if (mType == TYPE_ENTER) {
            intent.putExtra(SafeQuestionActivity.FROM_WHERE, SafeQuestionActivity.FROM_ENTER);
        }
        startActivity(intent);
    }

    private void setSafeQuestionAnswer() {
        Intent intent = new Intent(UnlockActivity.this, SafeQuestionActivity.class);
        intent.putExtra(SafeQuestionActivity.FROM_WHERE, SafeQuestionActivity.FROM_FIRST_ENTER);
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        if (mType == TYPE_UNLOCK) {
            gotoLauncher();
        } else {
            super.onBackPressed();
        }
    }

    private void gotoLauncher() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        startActivity(intent);
        finish();
    }

}
