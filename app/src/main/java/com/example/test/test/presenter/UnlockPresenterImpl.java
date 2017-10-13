package com.example.test.test.presenter;

import com.example.test.test.view.UnlockView;

/**
 * Created by hhj on 2017/08/23.
 */

public class UnlockPresenterImpl implements UnlockPresenter {

    private UnlockView mUnlockView;

    public UnlockPresenterImpl(UnlockView unlockView){
        mUnlockView = unlockView;
    }

    @Override
    public void onCreate() {
        mUnlockView.onInit();
    }

    @Override
    public void onNewIntent() {
        mUnlockView.onInit();
    }
}
