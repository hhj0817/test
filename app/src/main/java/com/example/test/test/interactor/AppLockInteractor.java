package com.example.test.test.interactor;

import android.support.annotation.NonNull;

import com.example.test.test.bean.AppLockInfo;

import java.util.List;

/**
 * Created by hhj on 2017/08/24.
 */

public interface AppLockInteractor {

    interface OnAppLockListener{
        void onQueryFinished(List<AppLockInfo> datas);
    }

    void queryAppLockInfos(@NonNull OnAppLockListener onAppLockListener);
}
