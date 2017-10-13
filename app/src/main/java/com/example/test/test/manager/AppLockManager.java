package com.example.test.test.manager;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

import com.example.test.test.Constants;
import com.example.test.test.MyApplication;
import com.example.test.test.bean.AppLockInfo;
import com.example.test.test.dao.AppLockDao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by hhj on 2017/08/24.
 */

public class AppLockManager {

    private volatile static AppLockManager instance;
    private List<AppLockInfo> mAppLockInfos;
    private Map<String, AppLockInfo> mAppLockMatches;

    //手势密码
    private String gesturePassword;
    //数字密码
    private String digitPassword;

    private boolean isAppLockOn;
    private boolean isDelayLockOn;
    private int delayLockTime;
    private boolean isScreenOffLock;
    private boolean antiUninstall;
    private boolean newAppDetect;
    private String screenRotateOrientation;
    private String lockType;
    private boolean isPatternTrackShow;
    private boolean isVibrationOn;
    private boolean isIntruderSelfieOn;
    private int safeQuestionIndex;

    private boolean hasUnlock;
    private long lastUnlockTime;
    private String safeQuestionAnswer;

    private AppLockManager() {
        mAppLockMatches = new HashMap<>();
    }

    public static AppLockManager getInstance() {
        if (instance == null) {
            synchronized (AppLockManager.class) {
                if (instance == null) {
                    instance = new AppLockManager();
                }
            }
        }
        return instance;
    }

    public List<AppLockInfo> getAppLockInfos() {
        return mAppLockInfos;
    }

    public AppLockInfo getAppLockInfo(String pkgName) {
        return mAppLockMatches.get(pkgName);
    }

    public void insertAppLockInfo(AppLockInfo appLockInfo) {
        mAppLockInfos.add(appLockInfo);
        mAppLockMatches.put(appLockInfo.getPkgName(), appLockInfo);
    }

    public List<AppLockInfo> queryAppLockInfos(PackageManager pm) {
        List<ApplicationInfo> appInfos = pm.getInstalledApplications(PackageManager.MATCH_UNINSTALLED_PACKAGES);
        List<AppLockInfo> appLockInfos = new ArrayList<>();
        for (ApplicationInfo appInfo : appInfos) {
            //过滤系统应用 和本应用
            if ((appInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0 || MyApplication.getContext().getPackageName().equals(appInfo.packageName))
                continue;
            //查询数据库
            AppLockInfo appLockInfo = AppLockDao.queryAppLockInfo(appInfo.packageName);
            //数据库没有这条数据，新建数据
            if (appLockInfo == null) {
                appLockInfo = new AppLockInfo();
                appLockInfo.setPkgName(appInfo.packageName);
                appLockInfo.setAppName(appInfo.loadLabel(pm).toString());
                //默认不锁
                appLockInfo.setIsLock(false);
                appLockInfo.setIsBrightnessLock(false);
                appLockInfo.setIsRotateLock(false);
                AppLockDao.insertAppLockInfo(appLockInfo);
            }
            appLockInfo.setIcon(appInfo.loadIcon(pm));
            appLockInfos.add(appLockInfo);
        }
        //缓存数据
        mAppLockInfos = appLockInfos;
        for (AppLockInfo data : appLockInfos) {
            mAppLockMatches.put(data.getPkgName(), data);
        }
        return appLockInfos;
    }

    //应用锁开关
    public boolean isAppLockOn() {
        return isAppLockOn;
    }

    public void setAppLockOn(Context context, boolean state) {
        getSharedPreferences(context).edit().putBoolean(Constants.AppLock.IS_APP_LOCK_ON, state).commit();
        isAppLockOn = state;
    }

    //应用锁手势密码
    public String getGesturePassword() {
        return gesturePassword;
    }

    public void setAppLockGesturePassword(Context context, String password) {
        getSharedPreferences(context).edit().putString(Constants.AppLock.APP_LOCK_GESTURE_PASSWORD, password).commit();
        this.gesturePassword = password;
    }

    //应用锁数字密码
    public String getDigitPassword() {
        return digitPassword;
    }

    public void setAppLockDigitPassword(Context context, String password) {
        getSharedPreferences(context).edit().putString(Constants.AppLock.APP_LOCK_DIGIT_PASSWORD, password).commit();
        this.digitPassword = password;
    }

    //延迟锁定
    public boolean isDelayLock() {
        return isDelayLockOn;
    }

    public void setDelayLock(Context context, boolean state) {
        getSharedPreferences(context).edit().putBoolean(Constants.AppLock.DELAY_LOCK, state).commit();
        isDelayLockOn = state;
    }

    //延迟锁定时间
    public long getDelayLockTime() {
        return delayLockTime;
    }

    public void setDelayLockTime(Context context, int time) {
        getSharedPreferences(context).edit().putInt(Constants.AppLock.DELAY_LOCK_TIME, time).commit();
        delayLockTime = time;
    }

    //关屏锁定
    public boolean isScreenOffLock() {
        return isScreenOffLock;
    }

    public void setScreenOffLock(Context context, boolean state) {
        getSharedPreferences(context).edit().putBoolean(Constants.AppLock.SCREEN_OFF_LOCK, state).commit();
        isScreenOffLock = state;
    }

    //防卸载
    public boolean isAntiUninstall() {
        return antiUninstall;
    }

    public void setAntiUninstall(Context context, boolean state) {
        getSharedPreferences(context).edit().putBoolean(Constants.AppLock.ANTI_UNINSTALL, state).commit();
        antiUninstall = state;
    }

    //新应用安装检测
    public boolean isNewAppDetectOn() {
        return newAppDetect;
    }

    public void setNewAppDetect(Context context, boolean state) {
        getSharedPreferences(context).edit().putBoolean(Constants.AppLock.NEW_APP_DETECT, state).commit();
        newAppDetect = state;
    }

    //屏幕保护旋转方向
    public String getScreenRotateOrientation() {
        return screenRotateOrientation;
    }

    public void setScreenRotateOrientation(Context context, String orientation) {
        getSharedPreferences(context).edit().putString(Constants.AppLock.SCREEN_ORIENTATION, orientation).commit();
        screenRotateOrientation = orientation;
    }

    //锁类型
    public String getLockType() {
        return lockType;
    }

    public void setLockType(Context context, String type) {
        getSharedPreferences(context).edit().putString(Constants.AppLock.LOCK_TYPE, type).commit();
        lockType = type;
    }

    //图案路径可见
    public boolean isPatternTrackShow() {
        return isPatternTrackShow;
    }

    public void setPatternTrackShow(Context context, boolean state) {
        getSharedPreferences(context).edit().putBoolean(Constants.AppLock.PATTERN_TRACK_SHOW, state).commit();
        isPatternTrackShow = state;
    }

    //震动反馈
    public boolean isVibrationOn() {
        return isVibrationOn;
    }

    public void setVibration(Context context, boolean state) {
        getSharedPreferences(context).edit().putBoolean(Constants.AppLock.VIBRATION, state).commit();
        isVibrationOn = state;
    }

    //入侵者拍照
    public boolean isIntruderSelfieOn() {
        return isIntruderSelfieOn;
    }

    public void setIntruderSelfieOn(Context context, boolean state) {
        getSharedPreferences(context).edit().putBoolean(Constants.AppLock.INTRUDER_SELFIE, state).commit();
        isIntruderSelfieOn = state;
    }

    //安全问题
    public int getSafeQuestionIndex() {
        return safeQuestionIndex;
    }

    public void setSafeQuestionIndex(Context context, int index) {
        getSharedPreferences(context).edit().putInt(Constants.AppLock.SAFE_QUESTION, index).commit();
        safeQuestionIndex = index;
    }

    //安全问题答案
    public String getSafeQuestionAnswer() {
        return safeQuestionAnswer;
    }

    public void setSafeQuestionAnswer(Context context, String answer) {
        getSharedPreferences(context).edit().putString(Constants.AppLock.SAFE_QUESTION_ANSWER, answer).commit();
        safeQuestionAnswer = answer;
    }

    //上次解锁时间
    public long getLastUnlockTime() {
        return lastUnlockTime;
    }

    public void setLastUnlockTime(long time) {
        lastUnlockTime = time;
    }


    private SharedPreferences getSharedPreferences(Context context) {
        return context.getSharedPreferences("cleanmaster", Context.MODE_PRIVATE);
    }

    //解锁状态
    public boolean hasUnlock() {
        return hasUnlock;
    }

    public void setHasUnlock(boolean hasUnlock) {
        this.hasUnlock = hasUnlock;
    }

}
