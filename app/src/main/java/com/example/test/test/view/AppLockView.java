package com.example.test.test.view;

import com.example.test.test.bean.AppLockInfo;

import java.util.List;

/**
 * Created by hhj on 2017/08/23.
 */

public interface AppLockView {

    void onInit(List<AppLockInfo> appLockInfos);
    void onQueryFinished();
}
