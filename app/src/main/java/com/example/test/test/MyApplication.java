package com.example.test.test;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.test.test.bean.DaoMaster;
import com.example.test.test.bean.DaoSession;
import com.example.test.test.manager.AppLockManager;

/**
 * Created by hhj on 2017/08/24.
 */

public class MyApplication extends Application {

    private static Context mApplicationContext;
    private static DaoSession mDaoSession;

    @Override
    public void onCreate() {
        super.onCreate();
        mApplicationContext = getApplicationContext();
        setupDatabase();
        initAppLockSetting();
    }

    public static Context getContext() {
        return mApplicationContext;
    }

    private void initAppLockSetting() {
        AppLockManager appLockManager = AppLockManager.getInstance();
        SharedPreferences sp = getSharedPreferences("cleanmaster", MODE_PRIVATE);
        appLockManager.setAppLockGesturePassword(mApplicationContext, sp.getString(Constants.AppLock.APP_LOCK_GESTURE_PASSWORD, ""));
        appLockManager.setAppLockDigitPassword(mApplicationContext, sp.getString(Constants.AppLock.APP_LOCK_DIGIT_PASSWORD, ""));
        appLockManager.setAppLockOn(mApplicationContext, sp.getBoolean(Constants.AppLock.IS_APP_LOCK_ON, false));
        appLockManager.setDelayLock(mApplicationContext, sp.getBoolean(Constants.AppLock.DELAY_LOCK, true));
        appLockManager.setDelayLockTime(mApplicationContext, sp.getInt(Constants.AppLock.DELAY_LOCK_TIME, 15000));
        appLockManager.setScreenOffLock(mApplicationContext, sp.getBoolean(Constants.AppLock.SCREEN_OFF_LOCK, true));
        appLockManager.setAntiUninstall(mApplicationContext, sp.getBoolean(Constants.AppLock.ANTI_UNINSTALL, false));
        appLockManager.setNewAppDetect(mApplicationContext, sp.getBoolean(Constants.AppLock.NEW_APP_DETECT, true));
        appLockManager.setScreenRotateOrientation(mApplicationContext, sp.getString(Constants.AppLock.SCREEN_ORIENTATION, Constants.AppLock.ORIENTATION_VERTICAL));
        appLockManager.setLockType(mApplicationContext, sp.getString(Constants.AppLock.LOCK_TYPE, Constants.AppLock.TYPE_GESTURE));
        appLockManager.setPatternTrackShow(mApplicationContext, sp.getBoolean(Constants.AppLock.PATTERN_TRACK_SHOW, true));
        appLockManager.setVibration(mApplicationContext, sp.getBoolean(Constants.AppLock.VIBRATION, true));
        appLockManager.setIntruderSelfieOn(mApplicationContext, sp.getBoolean(Constants.AppLock.INTRUDER_SELFIE, false));
        appLockManager.setSafeQuestionIndex(mApplicationContext, sp.getInt(Constants.AppLock.SAFE_QUESTION, 0));
        appLockManager.setSafeQuestionAnswer(mApplicationContext, sp.getString(Constants.AppLock.SAFE_QUESTION_ANSWER, ""));
    }

    private void setupDatabase() {
        DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(this, "applock.db", null);
        SQLiteDatabase db = helper.getWritableDatabase();
        DaoMaster daoMaster = new DaoMaster(db);
        mDaoSession = daoMaster.newSession();
        if (mDaoSession == null) {
            Log.d("hhh", "DaoSession为null");
        }
    }

    public static DaoSession getDaoSession() {
        return mDaoSession;
    }
}
