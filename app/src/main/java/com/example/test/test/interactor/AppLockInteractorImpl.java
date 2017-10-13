package com.example.test.test.interactor;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.support.annotation.NonNull;

import com.example.test.test.manager.AppLockManager;
import com.example.test.test.bean.AppLockInfo;

import java.util.List;

/**
 * Created by hhj on 2017/08/24.
 */

public class AppLockInteractorImpl implements AppLockInteractor {

    private Context mContext;
    private PackageManager pm;
    private OnAppLockListener mOnAppLockListener;
    private List<AppLockInfo> mDatas;

    public AppLockInteractorImpl(Context context) {
        mContext = context;
        pm = mContext.getPackageManager();
    }

    @Override
    public void queryAppLockInfos(@NonNull OnAppLockListener onAppLockListener) {
        mOnAppLockListener = onAppLockListener;
        AppScanTask task = new AppScanTask();
        task.execute();
    }

    class AppScanTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            mDatas = AppLockManager.getInstance().queryAppLockInfos(pm);
            return null;
        }

        @Override
        protected void onPostExecute(Void Void) {
            if (mOnAppLockListener != null) {
                mOnAppLockListener.onQueryFinished(mDatas);
            }
        }
    }
}
