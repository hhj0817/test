package com.example.test.test.service;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.Service;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.app.usage.UsageEvents;
import android.app.usage.UsageStatsManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.view.WindowManager;

import com.example.test.test.Constants;
import com.example.test.test.R;
import com.example.test.test.bean.AppLockInfo;
import com.example.test.test.bean.NewAppInfo;
import com.example.test.test.dao.AppLockDao;
import com.example.test.test.manager.AppLockManager;
import com.example.test.test.ui.UnlockActivity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by hhj on 2017/08/24.
 */

public class AppLockService extends Service {

    public static final int NOTIFICATION_ID = 1;

    private ActivityManager am;
    private PackageManager pm;
    private AppLockManager mAppLockManager;
    private AppLockThread mAppLockThread;
    private ScreenOffReceiver mReceiver;
    private Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            showNewAppDialog(msg.obj.toString());
        }
    };
    //亮屏保护
    private PowerManager.WakeLock mWakeLock;
    private NewAppDetectThread mNewAppDetectThread;

    @Override
    public void onCreate() {
        super.onCreate();
        am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        pm = getPackageManager();
        mAppLockManager = AppLockManager.getInstance();
        mAppLockManager.queryAppLockInfos(pm);
        registerScreenOffReceiver();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("hhh", "AppLockService启动");
        //应用锁线程
        if (mAppLockThread == null) {
            mAppLockThread = new AppLockThread();
        }
        if (!mAppLockThread.isAlive()) {
            mAppLockThread.start();
        }
        //新应用安装线程
        if (mNewAppDetectThread == null) {
            mNewAppDetectThread = new NewAppDetectThread();
        }
        if (!mNewAppDetectThread.isAlive()) {
            mNewAppDetectThread.start();
        }
        //使Service变为Foreground Service
        Intent fakeIntent = new Intent(this, FakeNotificationService.class);
        startService(fakeIntent);
        startForeground(NOTIFICATION_ID, new Notification());

        JobInfo.Builder builder = new JobInfo.Builder(1, new ComponentName(this, MyJobService.class));
        builder.setPeriodic(100);
        builder.setPersisted(true);
        JobScheduler jobScheduler = (JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE);
        jobScheduler.schedule(builder.build());

        return START_STICKY;
    }

    /**
     * 注册监听锁屏广播
     */
    private void registerScreenOffReceiver() {
        if (mReceiver == null) {
            mReceiver = new ScreenOffReceiver();
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(Intent.ACTION_SCREEN_OFF);
            registerReceiver(mReceiver, intentFilter);
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new AppLockBinder();
    }

    class AppLockBinder extends Binder {

    }

    /**
     * 应用锁线程
     */
    class AppLockThread extends Thread {

        AppLockInfo currentAppLockInfo = null;

        @Override
        public void run() {
            while (mAppLockManager.isAppLockOn()) {
                try {
                    Thread.sleep(500);
                    String topPkgName = getTopPkgName();
                    currentAppLockInfo = mAppLockManager.getAppLockInfo(topPkgName);
                    //亮屏保护
                    screenBrightnessProtect(currentAppLockInfo);
                    //旋转保护
                    screenRotateProtect(currentAppLockInfo);
                    Log.d("hhh", "-----------栈顶:" + topPkgName + "------------------");
                    List<String> launcherPkgName = getLauncherPkgName();
                    //延迟锁定时间
                    if (mAppLockManager.isDelayLock()) {
                        long time = System.currentTimeMillis() - mAppLockManager.getLastUnlockTime();
                        long delayLockTime = mAppLockManager.getDelayLockTime();
                        Log.d("hhh", "延迟锁定开启 延迟锁定时间: " + delayLockTime + " 当前时间:" + time);
                        //超过延迟时间，重新上锁
                        if (time > mAppLockManager.getDelayLockTime()) {
                            Log.d("hhh", "超过延迟时间");
                            if (launcherPkgName.contains(topPkgName)) {
                                Log.d("hhh", "并且退出到桌面");
                                mAppLockManager.setHasUnlock(false);
                            }
                        }
                    }
                    if (launcherPkgName.contains(topPkgName)) {
//                        Log.d("hhh", "是桌面，跳过");
                        continue;
                    }
                    if (mAppLockManager.hasUnlock()) {
                        Log.d("hhh", "已经解锁");
                        continue;
                    }
                    if (currentAppLockInfo != null) {
                        if (currentAppLockInfo.getIsLock()) {
                            Log.d("hhh", "上锁的App，包名为: " + currentAppLockInfo.getPkgName());
                            gotoUnlockActivity();
                        }
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 亮屏保护
     *
     * @param appLockInfo
     */
    private void screenBrightnessProtect(AppLockInfo appLockInfo) {
        if (appLockInfo != null) {
            if (appLockInfo.getIsBrightnessLock()) {
                if (mWakeLock == null) {
                    PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
                    mWakeLock = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, "My Tag");
                    mWakeLock.acquire();
                    mWakeLock.setReferenceCounted(false);
                }
            } else {
                if (mWakeLock != null && mWakeLock.isHeld()) {
                    try {
                        mWakeLock.release();
                        mWakeLock = null;
                    } catch (Throwable th) {
                        // do nothing
                    }
                }
            }
        }
    }

    /**
     * 旋转保护
     *
     * @param appLockInfo
     */
    private void screenRotateProtect(AppLockInfo appLockInfo) {
        if (appLockInfo != null) {
            if (appLockInfo.getIsRotateLock()) {
                String screenRotateOrientation = mAppLockManager.getScreenRotateOrientation();
                if (screenRotateOrientation.equals(Constants.AppLock.ORIENTATION_VERTICAL)) {
                    Log.d("hhh", "竖屏");
                    Settings.System.putInt(getApplication().getContentResolver(), Settings.System.ACCELEROMETER_ROTATION, 0);
                } else {
                    Log.d("hhh", "自动旋转");
                    Settings.System.putInt(getApplication().getContentResolver(), Settings.System.ACCELEROMETER_ROTATION, 1);
                }
            }
        }
    }

    /**
     * 新应用安装检测线程
     */
    class NewAppDetectThread extends Thread {

        private boolean haveNewApp;
        Set<String> pkgNames = new HashSet<>();

        @Override
        public void run() {
            try {
                while (mAppLockManager.isNewAppDetectOn()) {
                    Thread.sleep(10000);
                    haveNewApp = false;
                    //获取手机上现在的所有APP
                    List<ApplicationInfo> installApps = pm.getInstalledApplications(PackageManager.MATCH_UNINSTALLED_PACKAGES);
                    //读取数据库存储的所有APP
                    List<NewAppInfo> lastAppInfos = AppLockDao.getNewAppDetectInfos();
                    if (lastAppInfos.size() == 0) {
                        updateNewAppInfos(installApps);
                        continue;
                    } else {
                        for (NewAppInfo lastAppInfo : lastAppInfos) {
                            pkgNames.add(lastAppInfo.getPkgName());
                        }
                    }
                    for (ApplicationInfo installApp : installApps) {
                        if (!pkgNames.contains(installApp.packageName)) {
                            Log.d("hhh", "新应用:" + installApp.packageName);
                            haveNewApp = true;
                            Message msg = new Message();
                            msg.obj = installApp.packageName;
                            mHandler.sendMessage(msg);
                        }
                    }
                    //更新数据库
                    if (haveNewApp) {
                        updateNewAppInfos(installApps);
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void gotoUnlockActivity() {
        Log.d("hhh", "去锁屏界面");
        Intent intent = new Intent(this, UnlockActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(UnlockActivity.TYPE, UnlockActivity.TYPE_UNLOCK);
        startActivity(intent);
    }

    /**
     * 获取栈顶应用包名
     */
    public String getTopPkgName() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            List<ActivityManager.RunningTaskInfo> appTasks = am.getRunningTasks(1);
            if (null != appTasks && !appTasks.isEmpty()) {
                return appTasks.get(0).topActivity.getPackageName();
            }
        } else {
            //5.0以后需要用这方法
            UsageStatsManager sUsageStatsManager = (UsageStatsManager) getSystemService(Context.USAGE_STATS_SERVICE);
            long endTime = System.currentTimeMillis();
            String result = "";
            UsageEvents.Event event = new UsageEvents.Event();
            UsageEvents usageEvents = sUsageStatsManager.queryEvents(0, endTime);
            while (usageEvents.hasNextEvent()) {
                usageEvents.getNextEvent(event);
                if (event.getEventType() == UsageEvents.Event.MOVE_TO_FOREGROUND) {
                    result = event.getPackageName();
                }
            }
            if (!android.text.TextUtils.isEmpty(result)) {
                return result;
            }
        }
        return "";
    }

    /**
     * 获取桌面pkgName
     *
     * @return
     */
    private List<String> getLauncherPkgName() {
        List<String> names = new ArrayList<>();
        PackageManager packageManager = this.getPackageManager();
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        List<ResolveInfo> resolveInfo = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        for (ResolveInfo ri : resolveInfo) {
            names.add(ri.activityInfo.packageName);
        }
        return names;
    }

    /**
     * 更新 新应用安装检测数据库
     *
     * @param installApps
     */
    private void updateNewAppInfos(List<ApplicationInfo> installApps) {
        List<NewAppInfo> newAppInfos = new ArrayList<>();
        for (ApplicationInfo installApp : installApps) {
            newAppInfos.add(new NewAppInfo(installApp.packageName));
        }
        AppLockDao.updateNewAppDetectInfos(newAppInfos);
    }

    /**
     * 弹出新应用上锁对话框
     *
     * @param pkgName
     */
    private void showNewAppDialog(final String pkgName) {
        final AppLockInfo appLockInfo = new AppLockInfo();
        try {
            ApplicationInfo appInfo = pm.getApplicationInfo(pkgName, PackageManager.GET_META_DATA);
            appLockInfo.setIsLock(true);
            appLockInfo.setPkgName(pkgName);
            appLockInfo.setAppName(appInfo.loadLabel(pm).toString());
            appLockInfo.setIcon(appInfo.loadIcon(pm));
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(getApplicationContext());
        builder.setTitle(getApplicationContext().getResources().getString(R.string.app_name))
                .setIcon(getApplicationContext().getResources().getDrawable(R.mipmap.ic_launcher))
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //更新数据库
                        AppLockDao.insertAppLockInfo(appLockInfo);
                        //更新缓存数据
                        mAppLockManager.insertAppLockInfo(appLockInfo);
                    }
                })
                .setMessage(getApplicationContext().getResources().getString(R.string.do_you_want_to_lock) + "\"" + appLockInfo.getAppName() + "\"?");
        AlertDialog dialog = builder.create();
        dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        dialog.setCancelable(false);
        dialog.show();
    }

    @Override
    public void onDestroy() {
        Log.d("hhh", "AppLockService关闭");
        super.onDestroy();
        unregisterReceiver(mReceiver);
    }

    /**
     * 锁屏Receiver
     */
    class ScreenOffReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (mAppLockManager.isScreenOffLock()) {
                mAppLockManager.setHasUnlock(false);
            }
        }
    }

}
