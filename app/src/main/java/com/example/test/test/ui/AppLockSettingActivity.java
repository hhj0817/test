package com.example.test.test.ui;

import android.app.admin.DevicePolicyManager;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.Window;

import com.example.test.test.Constants;
import com.example.test.test.R;
import com.example.test.test.adapter.AppLockSettingAdapter;
import com.example.test.test.bean.AppLockSettingBean;
import com.example.test.test.manager.AppLockManager;
import com.example.test.test.receiver.AntiUninstallReceiver;
import com.example.test.test.service.AppLockService;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class AppLockSettingActivity extends AppCompatActivity {

    private static final int REQ_USAGE_ACCESS = 100;
    private static final int REQ_ANTI_UNINSTALL = 200;

    @BindView(R.id.tool_bar)
    Toolbar mToolBar;
    @BindView(R.id.recycler_view)
    RecyclerView mRecyclerView;

    private AppLockSettingAdapter mAdapter;
    private List<AppLockSettingBean> mDatas;
    private AppLockManager mAppLockManager;
    private int[] mDelayLockTimeStrings = {R.string.second15, R.string.second30, R.string.minute1, R.string.minute2, R.string.minute5};
    private int[] mDelayLockTimes = {15000, 30000, 60000, 120000, 300000};
    private int[] mScreenRotateStrings = {R.string.rotate_direction_portrait, R.string.rotate_direction_auto};
    private String[] mScreenRotateOrientation = {Constants.AppLock.ORIENTATION_VERTICAL, Constants.AppLock.ORIENTATION_AUTO};
    private int[] mLockTypeStrings = {R.string.digital_lock, R.string.gesture_lock};
    private String[] mLockType = {Constants.AppLock.TYPE_DIGIT, Constants.AppLock.TYPE_GESTURE};

    private AppLockSettingBean appLockOnBean;
    private AppLockSettingBean antiUninstallBean;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_lock_setting);
        ButterKnife.bind(this);
        initToolBar();
        initData();
        initView();
    }

    private void initData() {
        mDatas = new ArrayList<>();
        mAppLockManager = AppLockManager.getInstance();
        //0、基本设置
        mDatas.add(new AppLockSettingBean(AppLockSettingBean.TYPE_TITLE, getResources().getString(R.string.base_settings)));
        //1、应用锁开关
        mDatas.add(new AppLockSettingBean(AppLockSettingBean.TYPE_SWITCH, getResources().getString(R.string.app_lock), mAppLockManager.isAppLockOn()));
        //2、延迟锁定
        mDatas.add(new AppLockSettingBean(AppLockSettingBean.TYPE_SWITCH, getResources().getString(R.string.lock_delay), mAppLockManager.isDelayLock()));
        //3、延迟时间
        mDatas.add(new AppLockSettingBean(AppLockSettingBean.TYPE_OPTION, getResources().getString(R.string.lock_delay_time), transformMillis(mAppLockManager.getDelayLockTime())));
        if (!mAppLockManager.isDelayLock()) {
            mDatas.get(3).enable = false;
        }
        //4、关屏时锁定
        mDatas.add(new AppLockSettingBean(AppLockSettingBean.TYPE_SWITCH, getResources().getString(R.string.screen_lock_off), mAppLockManager.isScreenOffLock()));
        //5、防卸载
        mDatas.add(new AppLockSettingBean(AppLockSettingBean.TYPE_SWITCH, getResources().getString(R.string.anti_uninstall), mAppLockManager.isAntiUninstall()));
        //6、新应用安装检测
        mDatas.add(new AppLockSettingBean(AppLockSettingBean.TYPE_SWITCH, getResources().getString(R.string.detect_new_app), mAppLockManager.isNewAppDetectOn()));
        //7、亮屏保护
        mDatas.add(new AppLockSettingBean(AppLockSettingBean.TYPE_NORMAL, getResources().getString(R.string.brightness_protect)));
        //8、旋转保护
        mDatas.add(new AppLockSettingBean(AppLockSettingBean.TYPE_NORMAL, getResources().getString(R.string.rotate_protect)));
        //9、旋转保护屏幕方向
        if (mAppLockManager.getScreenRotateOrientation().equals(Constants.AppLock.ORIENTATION_VERTICAL)) {
            mDatas.add(new AppLockSettingBean(AppLockSettingBean.TYPE_OPTION, getResources().getString(R.string.rotate_protect_direction), getResources().getString(R.string.current) + ":" + getResources().getString(R.string.rotate_direction_portrait)));
        } else {
            mDatas.add(new AppLockSettingBean(AppLockSettingBean.TYPE_OPTION, getResources().getString(R.string.rotate_protect_direction), getResources().getString(R.string.current) + ":" + getResources().getString(R.string.rotate_direction_auto)));
        }

        //10、安全设置
        mDatas.add(new AppLockSettingBean(AppLockSettingBean.TYPE_TITLE, getResources().getString(R.string.safe_setting)));
        //11、更改锁类型
        if (mAppLockManager.getLockType().equals(Constants.AppLock.TYPE_GESTURE)) {
            mDatas.add(new AppLockSettingBean(AppLockSettingBean.TYPE_OPTION, getResources().getString(R.string.change_lock_type), getResources().getString(R.string.current) + ":" + getResources().getString(R.string.gesture_lock)));
        } else {
            mDatas.add(new AppLockSettingBean(AppLockSettingBean.TYPE_OPTION, getResources().getString(R.string.change_lock_type), getResources().getString(R.string.current) + ":" + getResources().getString(R.string.digital_lock)));
        }
        //12、修改密码
        mDatas.add(new AppLockSettingBean(AppLockSettingBean.TYPE_NORMAL, getResources().getString(R.string.change_password)));
        //13、图案路径可见
        mDatas.add(new AppLockSettingBean(AppLockSettingBean.TYPE_SWITCH, getResources().getString(R.string.show_pattern_track), mAppLockManager.isPatternTrackShow()));
        //14、震动反馈
        mDatas.add(new AppLockSettingBean(AppLockSettingBean.TYPE_SWITCH, getResources().getString(R.string.vibrate), mAppLockManager.isVibrationOn()));
        //15、入侵者拍照
        mDatas.add(new AppLockSettingBean(AppLockSettingBean.TYPE_SWITCH, getResources().getString(R.string.intruder_selfie), mAppLockManager.isIntruderSelfieOn()));
        //16、安全问题
        mDatas.add(new AppLockSettingBean(AppLockSettingBean.TYPE_NORMAL, getResources().getString(R.string.safe_ques_security_ques)));
    }

    private void initView() {
        mAdapter = new AppLockSettingAdapter(mDatas);
        mAdapter.setOnItemClickListener(new AppLockSettingAdapter.OnItemClickListener() {
            @Override
            public void onClick(int position) {
                handleClick(position);
            }
        });
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setAdapter(mAdapter);
    }

    private void initToolBar() {
        setSupportActionBar(mToolBar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * 处理设置界面点击事件
     *
     * @param position
     */
    private void handleClick(int position) {
        AppLockSettingBean bean = mDatas.get(position);
        Intent intent;
        switch (position) {
            case 1://应用锁开关
                bean.switchOn = !bean.switchOn;
                appLockOnBean = bean;
                mAppLockManager.setAppLockOn(this, bean.switchOn);
                if (bean.switchOn) {
                    //开启AppLockService
                    intent = new Intent(this, AppLockService.class);
                    startService(intent);
                    //5.0以上需要这个权限
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        if (hasUsageAccessOption()) {
                            if (!hasUsageAccessPermission()) {
                                showPermissionRequestDialog();
                            }
                        }
                    }
                }
                break;
            case 2://延迟锁定
                bean.switchOn = !bean.switchOn;
                mAppLockManager.setDelayLock(this, bean.switchOn);
                AppLockSettingBean delayLockTimeBean = mDatas.get(3);
                if (bean.switchOn) {
                    delayLockTimeBean.enable = true;
                } else {
                    delayLockTimeBean.enable = false;
                }
                break;
            case 3://延迟锁定时间
                int delayLockTimeCheckItem = getDelayLockTimeCheckItem(mAppLockManager.getDelayLockTime());
                showDelayLockTimeDialog(delayLockTimeCheckItem, bean);
                break;
            case 4://关屏锁定
                bean.switchOn = !bean.switchOn;
                mAppLockManager.setScreenOffLock(this, bean.switchOn);
                break;
            case 5://防卸载
                bean.switchOn = !bean.switchOn;
                antiUninstallBean = bean;
                enableAntiUninstall(bean.switchOn);
                break;
            case 6://新应用检测
                bean.switchOn = !bean.switchOn;
                mAppLockManager.setNewAppDetect(this, bean.switchOn);
                break;
            case 7://亮屏保护
                intent = new Intent(this, AppLockActivity.class);
                intent.putExtra(AppLockActivity.LOCK_TYPE, AppLockActivity.BRIGHTNESS);
                startActivity(intent);
                break;
            case 8://旋转保护
                intent = new Intent(this, AppLockActivity.class);
                intent.putExtra(AppLockActivity.LOCK_TYPE, AppLockActivity.ROTATE);
                startActivity(intent);
                break;
            case 9://旋转保护屏幕方向
                int screenRotateCheckItem = getScreenRotateCheckItem(mAppLockManager.getScreenRotateOrientation());
                showScreenRotateDialog(screenRotateCheckItem, bean);
                break;
            case 11://锁类型
                int lockTypeCheckItem = getLockTypeCheckItem(mAppLockManager.getLockType());
                showLockTypeDialog(lockTypeCheckItem, bean);
                break;
            case 12://修改密码
                intent = new Intent(this,UnlockActivity.class);
                intent.putExtra(UnlockActivity.TYPE,UnlockActivity.TYPE_CHANGE);
                startActivity(intent);
                break;
            case 13://图案路径可见
                bean.switchOn = !bean.switchOn;
                mAppLockManager.setPatternTrackShow(this, bean.switchOn);
                break;
            case 14://震动反馈
                bean.switchOn = !bean.switchOn;
                mAppLockManager.setVibration(this, bean.switchOn);
                break;
            case 15://入侵者拍照
                bean.switchOn = !bean.switchOn;
                mAppLockManager.setIntruderSelfieOn(this, bean.switchOn);
                break;
            case 16://安全问题
                intent = new Intent(this,SafeQuestionActivity.class);
                intent.putExtra(SafeQuestionActivity.FROM_WHERE,SafeQuestionActivity.FROM_SETTING);
                startActivity(intent);
                break;
        }
        mAdapter.notifyDataSetChanged();
    }

    /**
     * 将毫秒转换为String
     *
     * @param millis
     * @return
     */
    public String transformMillis(long millis) {
        if (millis == 15000)
            return getResources().getString(R.string.second15);
        else if (millis == 30000)
            return getResources().getString(R.string.second30);
        else if (millis == 60000)
            return getResources().getString(R.string.minute1);
        else if (millis == 120000)
            return getResources().getString(R.string.minute2);
        else if (millis == 300000)
            return getResources().getString(R.string.minute5);
        return "";
    }

    /**
     * 获取对话框的checkItem
     */
    private int getDelayLockTimeCheckItem(long millis) {
        if (millis == 15000)
            return 0;
        else if (millis == 30000)
            return 1;
        else if (millis == 60000)
            return 2;
        else if (millis == 120000)
            return 3;
        else if (millis == 300000)
            return 4;
        return 0;
    }

    private int getScreenRotateCheckItem(String orientation) {
        if (orientation.equals(Constants.AppLock.ORIENTATION_VERTICAL))
            return 0;
        else
            return 1;
    }

    private int getLockTypeCheckItem(String lockType) {
        if (lockType.equals(Constants.AppLock.TYPE_DIGIT))
            return 0;
        else
            return 1;
    }

    /**
     * 显示对话框
     */
    private void showDelayLockTimeDialog(int checkItem, final AppLockSettingBean bean) {
        final String[] strings = new String[mDelayLockTimeStrings.length];
        for (int i = 0; i < mDelayLockTimeStrings.length; i++) {
            strings[i] = getResources().getString(mDelayLockTimeStrings[i]);
        }
        AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setSingleChoiceItems(strings, checkItem, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        bean.subText = strings[which];
                        mAdapter.notifyDataSetChanged();
                        mAppLockManager.setDelayLockTime(AppLockSettingActivity.this, mDelayLockTimes[which]);
                        dialog.dismiss();
                    }
                }).create();
        Window window = alertDialog.getWindow();
        window.setGravity(Gravity.BOTTOM);
        alertDialog.show();
    }

    private void showScreenRotateDialog(int checkItem, final AppLockSettingBean bean) {
        final String[] strings = new String[mScreenRotateStrings.length];
        for (int i = 0; i < mScreenRotateStrings.length; i++) {
            strings[i] = getResources().getString(mScreenRotateStrings[i]);
        }
        AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setSingleChoiceItems(strings, checkItem, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        bean.subText = getResources().getString(R.string.current) + ":" + strings[which];
                        mAdapter.notifyDataSetChanged();
                        mAppLockManager.setScreenRotateOrientation(AppLockSettingActivity.this, mScreenRotateOrientation[which]);
                        dialog.dismiss();
                    }
                }).create();
        Window window = alertDialog.getWindow();
        window.setGravity(Gravity.BOTTOM);
        alertDialog.show();
    }

    private void showLockTypeDialog(int checkItem, final AppLockSettingBean bean) {
        final String[] strings = new String[mLockTypeStrings.length];
        for (int i = 0; i < mLockTypeStrings.length; i++) {
            strings[i] = getResources().getString(mLockTypeStrings[i]);
        }
        AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setSingleChoiceItems(strings, checkItem, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        bean.subText = getResources().getString(R.string.current) + ":" + strings[which];
                        mAdapter.notifyDataSetChanged();
                        mAppLockManager.setLockType(AppLockSettingActivity.this, mLockType[which]);
                        dialog.dismiss();
                    }
                }).create();
        Window window = alertDialog.getWindow();
        window.setGravity(Gravity.BOTTOM);
        alertDialog.show();
    }

    private void showPermissionRequestDialog() {
        String[] items = new String[]{getResources().getString(R.string.open_permission), getResources().getString(R.string.permission_cancel)};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        AlertDialog alertDialog = builder.setTitle(R.string.request_permisson)
                .setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 0) {
                            Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
                            startActivityForResult(intent, REQ_USAGE_ACCESS);
                        } else {
                            dialog.dismiss();
                        }
                    }
                }).create();
        alertDialog.show();
    }

    /**
     * 判断手机有没有“有权查看使用情况的应用”开关
     *
     * @return
     */
    private boolean hasUsageAccessOption() {
        PackageManager pm = getPackageManager();
        Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
        List<ResolveInfo> list = pm.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        return list.size() > 0;
    }

    /**
     * 判断是否有权限
     *
     * @return
     */
    private boolean hasUsageAccessPermission() {
        long ts = System.currentTimeMillis();
        UsageStatsManager usageStatsManager = (UsageStatsManager) getSystemService("usagestats");
        List<UsageStats> queryUsageStats = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_BEST, 0, ts);
        if (queryUsageStats == null || queryUsageStats.isEmpty()) {
            return false;
        }
        return true;
    }

    /**
     * 开启或关闭防卸载
     *
     * @param enable
     */
    public void enableAntiUninstall(boolean enable) {
        DevicePolicyManager policyManager = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
        ComponentName componentName = new ComponentName(this, AntiUninstallReceiver.class);
        if (enable && !policyManager.isAdminActive(componentName)) {
            Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
            //权限列表
            intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, componentName);
            //描述(additional explanation)
            intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, getResources().getString(R.string.protectapp_summary));
            startActivityForResult(intent, REQ_ANTI_UNINSTALL);
        } else if (!enable && policyManager.isAdminActive(componentName)) {
            policyManager.removeActiveAdmin(componentName);
            mAppLockManager.setAntiUninstall(this, false);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            //防卸载
            case REQ_ANTI_UNINSTALL:
                if (resultCode == RESULT_OK) {
                    mAppLockManager.setAntiUninstall(this, true);
                } else {
                    mAppLockManager.setAntiUninstall(this, false);
                    antiUninstallBean.switchOn = false;
                    mAdapter.notifyDataSetChanged();
                }
                break;
            //申请USAGE ACCESS权限
            case REQ_USAGE_ACCESS:
                if (hasUsageAccessPermission()) {
                    mAppLockManager.setAppLockOn(this, true);
                } else {
                    mAppLockManager.setAppLockOn(this, false);
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
