package com.example.test.test.presenter;

import android.content.Context;

import com.example.test.test.bean.AppLockInfo;
import com.example.test.test.interactor.AppLockInteractor;
import com.example.test.test.view.AppLockView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by hhj on 2017/08/23.
 */

public class AppLockPresenterImpl implements AppLockPresenter {

    private AppLockView mAppLockView;
    private AppLockInteractor mAppLockInteractor;
    private List<AppLockInfo> mDatas;
    private Context mContext;

    public AppLockPresenterImpl(AppLockView appLockView, AppLockInteractor appLockInteractor) {
        mAppLockView = appLockView;
        mAppLockInteractor = appLockInteractor;
        mContext = (Context) mAppLockView;
    }

    @Override
    public void onCreate() {
        mDatas = new ArrayList<>();
        mAppLockView.onInit(mDatas);
    }

    @Override
    public void queryAppLockInfos() {
        mAppLockInteractor.queryAppLockInfos(new AppLockInteractor.OnAppLockListener() {
            @Override
            public void onQueryFinished(List<AppLockInfo> datas) {
                mDatas.addAll(datas);
                mAppLockView.onQueryFinished();
            }
        });

    }

}
