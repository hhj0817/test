package com.example.test.test.service;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;

/**
 * Created by hhj on 2017/08/31.
 */

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class MyJobService extends JobService {
    @Override
    public boolean onStartJob(JobParameters params) {
        Log.d("hhh", "onStartJob");
        return false;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        Log.d("hhh", "onStopJob");
        return false;
    }
}
