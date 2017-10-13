package com.example.test.test.ui;

import android.app.ActivityManager;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Debug;
import android.os.Process;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.text.format.Formatter;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.example.test.test.Constants;
import com.example.test.test.manager.AppLockManager;
import com.example.test.test.R;
import com.jaredrummler.android.processes.ProcessManager;
import com.jaredrummler.android.processes.models.AndroidProcess;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private PackageManager pm;
    private ActivityManager am;
    private Button mButton;
    private AppLockManager mAppLockManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        pm = getPackageManager();
        mAppLockManager = AppLockManager.getInstance();
        mButton = (Button) findViewById(R.id.btn_app_lock);
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, UnlockActivity.class);
                String safeQuestionAnswer = mAppLockManager.getSafeQuestionAnswer();
                if (TextUtils.isEmpty(safeQuestionAnswer)) {
                    intent.putExtra(UnlockActivity.TYPE, UnlockActivity.TYPE_FIRST_ENTER);
                } else {
                    intent.putExtra(UnlockActivity.TYPE, UnlockActivity.TYPE_ENTER);
                }
                startActivity(intent);
            }
        });
    }


    private void getRunningProcess() {
        List<AndroidProcess> runningAppProcesses = ProcessManager.getRunningProcesses();
        for (AndroidProcess runningAppProcess : runningAppProcesses) {
            try {
//                if ((runningAppProcess.flags & ApplicationInfo.FLAG_SYSTEM) != 0) {
                PackageInfo packageInfo = pm.getPackageInfo(runningAppProcess.name, PackageManager.GET_META_DATA);
                ApplicationInfo applicationInfo = packageInfo.applicationInfo;
                String appName = pm.getApplicationLabel(applicationInfo).toString();
                Debug.MemoryInfo[] processMemoryInfo = am.getProcessMemoryInfo(new int[]{runningAppProcess.pid});
                Log.e("hhh", appName);
                Log.d("hhh", "PrivateDirty: " + Formatter.formatFileSize(this, processMemoryInfo[0].getTotalPrivateDirty() * 1024));
                Log.d("hhh", "Pss: " + Formatter.formatFileSize(this, processMemoryInfo[0].getTotalPss() * 1024));
                Log.d("hhh", "SharedClean: " + Formatter.formatFileSize(this, processMemoryInfo[0].getTotalSharedClean() * 1024));
                Log.d("hhh", "PrivateClean: " + Formatter.formatFileSize(this, processMemoryInfo[0].getTotalPrivateClean() * 1024));
                Log.d("hhh", "SharedDirty: " + Formatter.formatFileSize(this, processMemoryInfo[0].getTotalSharedDirty() * 1024));
                Log.d("hhh", "SwappablePss: " + Formatter.formatFileSize(this, processMemoryInfo[0].getTotalSwappablePss() * 1024));
                Log.d("hhh", "dalvik PrivateDirty: " + Formatter.formatFileSize(this, processMemoryInfo[0].dalvikPrivateDirty * 1024));
                Log.d("hhh", "dalvik Pss: " + Formatter.formatFileSize(this, processMemoryInfo[0].dalvikPss * 1024));
                Log.d("hhh", "dalvik SharedDirty: " + Formatter.formatFileSize(this, processMemoryInfo[0].dalvikSharedDirty * 1024));
                Log.d("hhh", "otherPrivateDirty: " + Formatter.formatFileSize(this, processMemoryInfo[0].otherPrivateDirty * 1024));
                Log.d("hhh", "otherPss: " + Formatter.formatFileSize(this, processMemoryInfo[0].otherPss * 1024));
                Log.d("hhh", "otherSharedDirty: " + Formatter.formatFileSize(this, processMemoryInfo[0].otherSharedDirty * 1024));
//                }
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    private long clearMemory() throws PackageManager.NameNotFoundException {
        List<ActivityManager.RunningAppProcessInfo> runningAppProcesses = ProcessManager.getRunningAppProcessInfo(this);
        long beforeMem = getAvailMemory();
        int count = 0;
        if (runningAppProcesses != null) {
            for (int i = 0; i < runningAppProcesses.size(); i++) {
                ActivityManager.RunningAppProcessInfo runningAppProcessInfo = runningAppProcesses.get(i);
                int pid = runningAppProcessInfo.pid;
                if (pid != Process.myPid()) ;
                Process.killProcess(pid);
            }
        }
        long afterMem = getAvailMemory();
        Log.d("hhh", "clear" + count + " process, " + (afterMem - beforeMem));
        return afterMem - beforeMem;
    }

    private long getAvailMemory() {
        ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
        am.getMemoryInfo(mi);
        return mi.availMem;
    }

}
