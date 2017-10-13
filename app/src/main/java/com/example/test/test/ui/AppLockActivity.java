package com.example.test.test.ui;

import android.Manifest;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.test.test.R;
import com.example.test.test.adapter.AppLockAdapter;
import com.example.test.test.bean.AppLockInfo;
import com.example.test.test.interactor.AppLockInteractorImpl;
import com.example.test.test.manager.AppLockManager;
import com.example.test.test.presenter.AppLockPresenter;
import com.example.test.test.presenter.AppLockPresenterImpl;
import com.example.test.test.service.AppLockService;
import com.example.test.test.view.AppLockView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class AppLockActivity extends AppCompatActivity implements AppLockView {

    private static final int REQ_USAGE_ACCESS = 100;

    public static final String LOCK_TYPE = "lock_type";
    public static final int NORMAL = 1;
    public static final int BRIGHTNESS = 2;
    public static final int ROTATE = 3;

    @BindView(R.id.tool_bar)
    Toolbar mToolBar;
    @BindView(R.id.recycler_view)
    RecyclerView mRecyclerView;
    @BindView(R.id.progress_bar)
    ProgressBar mProgressBar;

    private AppLockPresenter mAppLockPresenter;
    private List<AppLockInfo> mDatas;
    private AppLockAdapter mAdapter;
    private int mLockType;
    private AppLockManager mAppLockManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_lock);
        ButterKnife.bind(this);
        initToolBar();
        init();
        //5.0以上需要这个权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (hasUsageAccessOption()) {
                if (!hasUsageAccessPermission()) {
                    showPermissionRequestDialog();
                } else {

                }
            }
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_SETTINGS) != PackageManager.PERMISSION_GRANTED) {
            //用户上次拒绝了权限申请
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_SETTINGS)) {
                Log.d("hhh", "用户拒绝了权限申请");
            } else {
                //用户勾选了“不再询问”
                Log.d("hhh", "用户勾选了“不再询问”");
            }
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_SETTINGS}, 0);
        } else {//有权限
            Log.d("hhh", "有WRITE SETTINGS权限");
        }
    }


    private void init() {
        mLockType = getIntent().getIntExtra(LOCK_TYPE, NORMAL);
        mAppLockManager = AppLockManager.getInstance();
        switch (mLockType) {
            case NORMAL:
                mAppLockPresenter = new AppLockPresenterImpl(this, new AppLockInteractorImpl(this));
                mAppLockPresenter.onCreate();
                break;
            case BRIGHTNESS:
            case ROTATE:
                mDatas = mAppLockManager.getAppLockInfos();
                initView();
                mProgressBar.setVisibility(View.GONE);
                mRecyclerView.setVisibility(View.VISIBLE);
                break;
        }
    }

    @Override
    public void onInit(List<AppLockInfo> appLockInfos) {
        mDatas = appLockInfos;
        initView();
        mAppLockPresenter.queryAppLockInfos();
    }

    @Override
    public void onQueryFinished() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mProgressBar.setVisibility(View.GONE);
                mRecyclerView.setVisibility(View.VISIBLE);
                //第一次查询回来的数据先排序
                mAdapter.sort();
                mAdapter.notifyDataSetChanged();
                /////
                Intent intent = new Intent(AppLockActivity.this, AppLockService.class);
                startService(intent);
            }
        });
    }

    private void initView() {
        mAdapter = new AppLockAdapter(mDatas, mLockType);
        mAdapter.sort();
        mAdapter.setOnItemClickListener(new AppLockAdapter.OnItemClickListener() {
            @Override
            public void onItemClick() {
                mAdapter.notifyDataSetChanged();
            }
        });
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setAdapter(mAdapter);
    }

    /**
     * 弹出 申请USAGE ACCESS权限 对话框
     */
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


    private void initToolBar() {
        setSupportActionBar(mToolBar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mToolBar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.item_setting:
                        startActivity(new Intent(AppLockActivity.this, AppLockSettingActivity.class));
                        break;
                }
                return true;
            }
        });
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (mLockType == NORMAL) {
            getMenuInflater().inflate(R.menu.app_lock_setting, menu);
        }
        return true;
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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 0: {
                // If request is cancelled, the result arrays are empty.
                //权限申请成功
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d("hhh", "申请WRITE SETTINGS权限成功");
                } else {//权限申请失败
                    Log.d("hhh", "申请WRITE SETTINGS权限失败");
//                    finish();
                }
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode==REQ_USAGE_ACCESS){
            if(hasUsageAccessPermission()){
                mAppLockManager.setAppLockOn(this,true);
            }else{
                mAppLockManager.setAppLockOn(this,false);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
